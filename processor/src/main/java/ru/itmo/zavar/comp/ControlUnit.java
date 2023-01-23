package ru.itmo.zavar.comp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.json.simple.JSONArray;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.alu.AluOperation;
import ru.itmo.zavar.base.mem.ProtectedMemory;
import ru.itmo.zavar.base.mux.AluOutputMux;
import ru.itmo.zavar.base.mux.LeftAluInputMux;
import ru.itmo.zavar.base.mux.RightAluInputMux;
import ru.itmo.zavar.base.register.Register;
import ru.itmo.zavar.exception.ControlUnitException;
import ru.itmo.zavar.exception.InvalidInstructionException;
import ru.itmo.zavar.exception.ReservedInstructionException;
import ru.itmo.zavar.log.TickLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ControlUnit {
    private final DataPath dataPath;
    private final Register<Integer> ip = new Register<>(16777215, 0); // [2^24 - 1; 0]
    private final Register<Integer> ar = new Register<>(16777215, 0); // [2^24 - 1; 0]
    private final ProtectedMemory programMemory;
    private final Register<Long> cr = new Register<>(4294967295L, 0L); // [2^32 - 1; 0]
    private final Register<Byte> tc = new Register<>((byte) 16, (byte) 0);
    private Long controlUnitTicks;
    private boolean stopped = true;
    private Stage stage;
    @SuppressFBWarnings("SS_SHOULD_BE_STATIC")
    private final int opcodeOffset = 24;
    private final ArrayList<TickLog> tickLogs = new ArrayList<>();
    private final boolean printDebug;

    public ControlUnit(final ArrayList<Long> program, final ArrayList<Long> data, final boolean debug) {
        this(program, data, new JSONArray(), debug);
    }

    public ControlUnit(final ArrayList<Long> program, final ArrayList<Long> data, final JSONArray input, final boolean debug) {
        final Byte dataBits = 31; // 32 bits, signed
        final Byte programBits = 32; // 32 bits, unsigned
        final Integer programMemorySize = 16777215; // [2^24 - 1; 0]
        final Integer dataMemorySize = 4096;
        final Integer inputAddress = 1;
        final Integer byteAddress = 0;
        final Integer charAddress = 2;
        ip.writeValue(0);
        ar.writeValue(0);
        cr.writeValue(InstructionCode.NOPE.getBinary().longValue() << opcodeOffset);
        resetTick();
        dataPath = new DataPath(data, ip, ar, inputAddress, byteAddress, charAddress, dataMemorySize, dataBits, input);
        programMemory = new ProtectedMemory(programMemorySize, programBits, program);
        printDebug = debug;
    }

    public void start() throws ControlUnitException {
        stopped = false;
        controlUnitTicks = 0L;
        while (!stopped) {
            resetTick();
            stage = Stage.FETCH;
            fetchNextInstruction();
            resetTick();
            String opcode = Long.toBinaryString(cr.readValue() >> opcodeOffset);
            if (opcode.charAt(opcode.length() - 1) == '1') {
                stage = Stage.ADDRESS;
                fetchAr();
                resetTick();
            }
            stage = Stage.EXECUTE;
            execute();
            if (printDebug) {
                System.out.println();
            }
        }
    }

    private void incTick() {
        tc.writeValue((byte) (tc.readValue() + 1));
        onEveryControlUnitTick();
    }

    private void resetTick() {
        tc.writeValue((byte) 0);
    }

    private Byte readTick() {
        return tc.readValue();
    }

    private void onEveryControlUnitTick() {
        controlUnitTicks++;
        TickLog tickLog = new TickLog(controlUnitTicks, readTick(), stage, cr.readValue(),
                InstructionCode.valueByBinary(Long.toBinaryString(cr.readValue() >> opcodeOffset)), ip.readValue(),
                ar.readValue(), dataPath.getTosValue(), dataPath.getDsValue(), dataPath.getRsValue(),
                dataPath.getOutputString(), dataPath.getInputToken());
        if (printDebug) {
            tickLog.print();
        }
        tickLogs.add(tickLog);
    }

    public List<TickLog> getTickLog() {
        return Collections.unmodifiableList(tickLogs);
    }

    /**
     * PMAR ← IP
     */
    private void writePmar() {
        programMemory.writeAR(ip.readValue());
    }

    /**
     * СR ← PMEMORY
     */
    private void fetch() {
        cr.writeValue(programMemory.read());
    }

    /**
     * IP ← IP + 1
     */
    private void incIp() {
        dataPath.selectRalu(RightAluInputMux.FROM_IP);
        dataPath.selectOut(AluOutputMux.TO_IP);
        dataPath.readIp();
        dataPath.selectOp(AluOperation.RIGHT_INC);
        dataPath.writeIp();
    }

    /**
     * AR ← CR (0..23)
     */
    private void fetchAr() {
        final int addressMask = 16777215;
        ar.writeValue((int) (cr.readValue() & addressMask));
        incTick();
    }

    /**
     * PMAR ← IP
     * СR ← PMEMORY
     * IP ← IP + 1
     */
    private void fetchNextInstruction() {
        writePmar(); // PMAR ← IP
        incTick();
        fetch(); // СR ← PMEMORY
        incTick();
        incIp(); // IP ← IP + 1
        incTick();
    }

    private void execute() {
        switch (InstructionCode.valueByBinary(Long.toBinaryString(cr.readValue() >> opcodeOffset))) {
            case HALT -> {
                stopped = true;
                incTick();
            }
            case ADD -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.readTos();
                dataPath.selectOp(AluOperation.PLUS); // TOS ← TOS + POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case SUB -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.readTos();
                dataPath.selectOp(AluOperation.MINUS); // TOS ← POP(DS) - TOS
                dataPath.writeTos();
                incTick();
            }
            case MUL -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.readTos();
                dataPath.selectOp(AluOperation.MULTIPLY); // TOS ← TOS * POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case DIV -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.readTos();
                dataPath.selectOp(AluOperation.DIVIDE); // TOS ← POP(DS) / TOS
                dataPath.writeTos();
                incTick();
            }
            case AND -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.readTos();
                dataPath.selectOp(AluOperation.AND); // TOS ← TOS and POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case OR -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.readTos();
                dataPath.selectOp(AluOperation.OR); // TOS ← TOS or POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case NEG -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT_NOT); // TOS ← -TOS
                dataPath.writeTos();
                incTick();
            }
            case NOT -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT_BNOT); // TOS ← not TOS
                dataPath.writeTos();
                incTick();
            }
            case XOR -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.readTos();
                dataPath.selectOp(AluOperation.XOR); // TOS ← TOS xor POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case INC -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT_INC); // TOS ← TOS + 1
                dataPath.writeTos();
                incTick();
            }
            case DEC -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT_DEC); // TOS ← TOS + 1
                dataPath.writeTos();
                incTick();
            }
            case NEQ -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.readTos();
                dataPath.selectOp(AluOperation.NEQ); // TOS ← TOS != POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case EQ -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.readTos();
                dataPath.selectOp(AluOperation.EQ); // TOS ← TOS == POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case GR -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.readTos();
                dataPath.selectOp(AluOperation.GR); // TOS ← TOS > POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case LE -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.readTos();
                dataPath.selectOp(AluOperation.LE); // TOS ← TOS < POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case DROP -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.selectOp(AluOperation.LEFT); // TOS ← POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case DUP -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_DS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT); // PUSH(DS) ← TOS
                dataPath.writeDs();
                incTick();
            }
            case OVER -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_RS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT); // PUSH(RS) ← TOS
                dataPath.writeRs();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.selectOp(AluOperation.LEFT); // TOS ← POP(DS)
                dataPath.writeTos();
                incTick();

                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_DS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT); // PUSH(DS) ← TOS
                dataPath.writeDs();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_RS);
                dataPath.selectOut(AluOutputMux.TO_DS);
                dataPath.readRs();
                dataPath.selectOp(AluOperation.LEFT); // PUSH(DS) ← POP(RS)
                dataPath.writeDs();
                incTick();
            }
            case SWAP -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_RS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT); // PUSH(RS) ← TOS
                dataPath.writeRs();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.selectOp(AluOperation.LEFT); // TOS ← POP(DS)
                dataPath.writeTos();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_RS);
                dataPath.selectOut(AluOutputMux.TO_DS);
                dataPath.readRs();
                dataPath.selectOp(AluOperation.LEFT); // PUSH(DS) ← POP(RS)
                dataPath.writeDs();
                incTick();
            }
            case MR -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_RS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT); // PUSH(RS) ← TOS
                dataPath.writeRs();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.selectOp(AluOperation.LEFT); // TOS ← POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case RM -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_DS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT); // PUSH(DS) ← TOS
                dataPath.writeDs();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_RS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readRs();
                dataPath.selectOp(AluOperation.LEFT); // TOS ← POP(RS)
                dataPath.writeTos();
                incTick();
            }
            case ST -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_DMAR);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT); // DMAR ← TOS
                dataPath.writeDmar();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectOut(AluOutputMux.TO_DATA);
                dataPath.readDs();
                dataPath.selectOp(AluOperation.LEFT); // DMEMORY ← POP(DS)
                dataPath.writeMem();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.selectOp(AluOperation.LEFT); // TOS ← POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case FT -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_DMAR);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT); // DMAR ← TOS
                dataPath.writeDmar();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_DATA);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.oeMem();
                dataPath.selectOp(AluOperation.LEFT); // TOS ← DMEMORY
                dataPath.writeTos();
                incTick();
            }
            case LIT -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_AR);
                dataPath.selectOut(AluOutputMux.TO_DMAR);
                dataPath.readAr();
                dataPath.selectOp(AluOperation.LEFT); // DMAR ← AR
                dataPath.writeDmar();
                incTick();

                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_DS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT); // PUSH(DS) ← TOS
                dataPath.writeDs();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_DATA);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.oeMem();
                dataPath.selectOp(AluOperation.LEFT); // TOS ← DMEMORY
                dataPath.writeTos();
                incTick();
            }
            case ADDR -> {
                dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                dataPath.selectOut(AluOutputMux.TO_DS);
                dataPath.readTos();
                dataPath.selectOp(AluOperation.RIGHT); // PUSH(DS) ← TOS
                dataPath.writeDs();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_AR);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readAr();
                dataPath.selectOp(AluOperation.LEFT); // TOS ← AR
                dataPath.writeTos();
                incTick();
            }
            case JMP -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_AR);
                dataPath.selectOut(AluOutputMux.TO_IP);
                dataPath.readAr();
                dataPath.selectOp(AluOperation.LEFT); // IP ← AR
                dataPath.writeIp();
                incTick();
            }
            case IF -> {
                if (dataPath.zeroFlag()) {
                    dataPath.selectLalu(LeftAluInputMux.FROM_AR);
                    dataPath.selectOut(AluOutputMux.TO_IP);
                    dataPath.readAr();
                    dataPath.selectOp(AluOperation.LEFT); // IP ← AR
                    dataPath.writeIp();
                    incTick();
                }
                dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                dataPath.selectOut(AluOutputMux.TO_TOS);
                dataPath.readDs();
                dataPath.selectOp(AluOperation.LEFT); // TOS ← POP(DS)
                dataPath.writeTos();
                incTick();
            }
            case CALL -> {
                dataPath.selectRalu(RightAluInputMux.FROM_IP);
                dataPath.selectOut(AluOutputMux.TO_RS);
                dataPath.readIp();
                dataPath.selectOp(AluOperation.RIGHT); // PUSH(RS) ← IP
                dataPath.writeRs();
                incTick();

                dataPath.selectLalu(LeftAluInputMux.FROM_AR);
                dataPath.selectOut(AluOutputMux.TO_IP);
                dataPath.readAr();
                dataPath.selectOp(AluOperation.LEFT); // IP ← AR
                dataPath.writeIp();
                incTick();
            }
            case LOOP -> {
                if (!dataPath.zeroFlag() && !dataPath.negativeFlag()) {
                    dataPath.selectRalu(RightAluInputMux.FROM_TOS);
                    dataPath.selectOut(AluOutputMux.TO_TOS);
                    dataPath.readTos();
                    dataPath.selectOp(AluOperation.RIGHT_DEC); // TOS ← TOS - 1
                    dataPath.writeTos();
                    incTick();

                    dataPath.selectLalu(LeftAluInputMux.FROM_AR);
                    dataPath.selectOut(AluOutputMux.TO_IP);
                    dataPath.readAr();
                    dataPath.selectOp(AluOperation.LEFT); // IP ← AR
                    dataPath.writeIp();
                    incTick();
                } else {
                    dataPath.selectLalu(LeftAluInputMux.FROM_DS);
                    dataPath.selectOut(AluOutputMux.TO_TOS);
                    dataPath.readDs();
                    dataPath.selectOp(AluOperation.LEFT); // TOS ← POP(DS)
                    dataPath.writeTos();
                    incTick();
                }
            }
            case NOPE -> incTick();
            case EXIT -> {
                dataPath.selectLalu(LeftAluInputMux.FROM_RS);
                dataPath.selectOut(AluOutputMux.TO_IP);
                dataPath.readRs();
                dataPath.selectOp(AluOperation.LEFT); // IP ← POP(RS)
                dataPath.writeIp();
                incTick();
            }
            case null -> throw new InvalidInstructionException("Instruction is null");
            default -> throw new ReservedInstructionException();
        }
    }

}
