package ru.itmo.zavar.comp;

import net.sf.saxon.expr.instruct.Instruction;
import org.checkerframework.checker.units.qual.A;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.alu.AluOperation;
import ru.itmo.zavar.base.mem.ProtectedMemory;
import ru.itmo.zavar.base.mux.AluOutputMux;
import ru.itmo.zavar.base.mux.LeftAluInputMux;
import ru.itmo.zavar.base.mux.RightAluInputMux;
import ru.itmo.zavar.base.register.Register;

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

    public ControlUnit(final ArrayList<Long> program) {
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
        dataPath = new DataPath(ip, ar, inputAddress, outputAddress, dataMemorySize, dataBits);
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
            if(Long.toBinaryString(cr.readValue() >> 25).charAt(0) == '1') {
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
        System.out.print("Tick: " + controlUnitTicks);
        System.out.print(", TC: " + readTick());
        System.out.print(", Stage: " + stage);
        System.out.print(", CR: " + (cr.readValue() >> 24) + " {" + InstructionCode.valueByBinary(Long.toBinaryString(cr.readValue() >> 24)) + "}");
        System.out.print(", IP: " + ip.readValue());
        System.out.print(", AR: " + ar.readValue());
        System.out.println(", TOS: " + dataPath.getTosValue());
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
        ar.writeValue((int) (cr.readValue() & 16777215));
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
        switch (InstructionCode.valueByBinary(Long.toBinaryString(cr.readValue() >> 24))) {
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

                incIp(); // IP ← IP + 1
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

    private enum Stage {
        FETCH, EXECUTE, ADDRESS
    }

}
