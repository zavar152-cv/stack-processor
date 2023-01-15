package ru.itmo.zavar.comp;

import net.sf.saxon.expr.instruct.Instruction;
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
    private final ProtectedMemory programMemory;
    private final Register<Short> cr = new Register<>((short) 255, (short) 0); // [2^8 - 1; 0]
    private final Register<Byte> tc = new Register<>((byte) 16, (byte) 0);
    private Long controlUnitTicks;
    private boolean stopped = true;

    public ControlUnit(final ArrayList<Long> program) {
        final Byte dataBits = 31; // 32 bits, signed
        final Byte programBits = 32; // 32 bits, unsigned
        final Integer programMemorySize = 16777215; // [2^24 - 1; 0]
        final Integer dataMemorySize = 2048;
        final Integer inputAddress = 1;
        final Integer outputAddress = 0;
        ip.writeValue(0);
        cr.writeValue(InstructionCode.NOPE.getBinary());
        resetTick();
        dataPath = new DataPath(ip, inputAddress, outputAddress, dataMemorySize, dataBits);
        programMemory = new ProtectedMemory(programMemorySize, programBits, program);
    }

    public void start() {
        stopped = false;
        controlUnitTicks = 0L;
        while (!stopped) {
            resetTick();
            fetchNextInstruction();
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
        System.out.println("Tick: " + controlUnitTicks);
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
        short commandCode = (short) (programMemory.read() >> 24);
        cr.writeValue(commandCode);
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
        dataPath.selectRalu(RightAluInputMux.FROM_IP);
        dataPath.selectOut(AluOutputMux.TO_IP);
        dataPath.readIp();
        dataPath.selectOp(AluOperation.RIGHT_INC); // IP ← IP + 1
        dataPath.writeIp();
        incTick();
    }

    private void execute() {
        switch (InstructionCode.valueByBinary(Integer.toBinaryString(cr.readValue()))) {
            case HALT -> stopped = true; // TODO incTick????
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

    }

}
