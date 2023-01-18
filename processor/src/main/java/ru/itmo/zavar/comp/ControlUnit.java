package ru.itmo.zavar.comp;

import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.alu.AluOperation;
import ru.itmo.zavar.base.mem.ProtectedMemory;
import ru.itmo.zavar.base.mux.AluOutputMux;
import ru.itmo.zavar.base.mux.LeftAluInputMux;
import ru.itmo.zavar.base.mux.RightAluInputMux;
import ru.itmo.zavar.base.register.Register;
import ru.itmo.zavar.log.TickLog;

import java.util.ArrayList;

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
    private final int opcodeOffset = 24;
    private final ArrayList<TickLog> tickLogs = new ArrayList<>();

    public ControlUnit(final ArrayList<Long> program, final ArrayList<Long> data) {
        final Byte dataBits = 31; // 32 bits, signed
        final Byte programBits = 32; // 32 bits, unsigned
        final Integer programMemorySize = 16777215; // [2^24 - 1; 0]
        final Integer dataMemorySize = 2048;
        final Integer inputAddress = 1;
        final Integer outputAddress = 0;
        ip.writeValue(0);
        ar.writeValue(0);
        cr.writeValue(InstructionCode.NOPE.getBinary().longValue());
        resetTick();
        dataPath = new DataPath(data, ip, ar, inputAddress, outputAddress, dataMemorySize, dataBits);
        programMemory = new ProtectedMemory(programMemorySize, programBits, program);
    }

    public void start() {
        stopped = false;
        controlUnitTicks = 0L;
        while (!stopped) {
            resetTick();
            stage = Stage.FETCH;
            fetchNextInstruction();
            resetTick();
            if (Long.toBinaryString(cr.readValue() >> opcodeOffset + 1).charAt(0) == '1') {
                stage = Stage.ADDRESS;
                fetchAr();
                resetTick();
            }
            stage = Stage.EXECUTE;
            execute();
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
        TickLog tickLog = new TickLog(controlUnitTicks, readTick(), stage, (cr.readValue() >> opcodeOffset),
                InstructionCode.valueByBinary(Long.toBinaryString(cr.readValue() >> opcodeOffset)), ip.readValue(),
                ar.readValue(), dataPath.getTosValue());
        tickLog.print();
        tickLogs.add(tickLog);
    }

    public ArrayList<TickLog> getTickLog() {
        return tickLogs;
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
            }
            case MUL -> {
            }
            case DIV -> {
            }
            case AND -> {
            }
            case OR -> {
            }
            case NOT -> {
            }
            case XOR -> {
            }
            case EQ -> {
            }
            case GR -> {
            }
            case LE -> {
            }
            case DROP -> {
            }
            case DUP -> {
            }
            case OVER -> {
            }
            case SWAP -> {
            }
            case MR -> {
            }
            case RM -> {
            }
            case ST -> {
            }
            case FT -> {
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
            case JMP -> {
            }
            case IF -> {
            }
            case CALL -> {
            }
            case LOOP -> {
            }
            case NOPE -> {
            }
            case EXIT -> {
            }
            case null -> {
            }
            default -> {
            }
        }
        System.out.println();
    }

}