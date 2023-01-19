package ru.itmo.zavar.comp;

import ru.itmo.zavar.alu.Alu;
import ru.itmo.zavar.alu.AluOperation;
import ru.itmo.zavar.base.mem.DataMemoryController;
import ru.itmo.zavar.base.mem.Memory;
import ru.itmo.zavar.base.mux.AluOutputMux;
import ru.itmo.zavar.base.mux.LeftAluInputMux;
import ru.itmo.zavar.base.mux.RightAluInputMux;
import ru.itmo.zavar.base.register.Register;
import ru.itmo.zavar.exception.InvalidMuxSelectionException;
import ru.itmo.zavar.io.InputDevice;
import ru.itmo.zavar.io.OutputDevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public final class DataPath {
    private final Stack<Long> dataStack = new Stack<>();
    private final Stack<Long> returnStack = new Stack<>();
    private final DataMemoryController dataMemoryController;
    private final Alu alu = new Alu();
    private LeftAluInputMux leftAluInputMux;
    private RightAluInputMux rightAluInputMux;
    private AluOutputMux aluOutputMux;
    private final Register<Long> tos = new Register<>(Long.MAX_VALUE, Long.MIN_VALUE);
    private final Register<Integer> ip;
    private final Register<Integer> ar;
    private boolean negativeFlag;
    private boolean zeroFlag;
    private final StringBuilder outputBuilder;
    private final Stack<Character> inputCharacters;

    public DataPath(final ArrayList<Long> data, final Register<Integer> ipRegister, final Register<Integer> arRegister, final Integer inputAddress,
                    final Integer outputAddress, final Integer memorySize, final Byte bits, final String input) {
        ip = ipRegister;
        ar = arRegister;
        Memory dataMemory = new Memory(memorySize, bits, data);
        tos.writeValue(0L);
        inputCharacters = new Stack<>();
        Arrays.stream(new StringBuilder(input).reverse().toString().split(" ")).forEach(s -> {
            if (!s.isEmpty()) {
                inputCharacters.push(s.charAt(0));
            }
        });
        outputBuilder = new StringBuilder();
        dataMemoryController = new DataMemoryController(dataMemory, new InputDevice(inputAddress, inputCharacters),
                new OutputDevice(outputAddress, outputBuilder));
    }

    public void selectOp(final AluOperation operation) {
        alu.setOperation(operation);
        alu.calculate();
    }

    public void selectOut(final AluOutputMux outputMux) {
        aluOutputMux = outputMux;
    }

    public void selectLalu(final LeftAluInputMux left) {
        leftAluInputMux = left;
    }

    public void selectRalu(final RightAluInputMux right) {
        rightAluInputMux = right;
    }

    public void writeMem() throws InvalidMuxSelectionException {
        if (aluOutputMux == AluOutputMux.TO_DATA) {
            dataMemoryController.write(alu.getOutput());
        } else {
            throw new InvalidMuxSelectionException("Select TO_DATA to write in memory");
        }
    }

    public void oeMem() throws InvalidMuxSelectionException {
        if (leftAluInputMux == LeftAluInputMux.FROM_DATA) {
            alu.setLeftInput(dataMemoryController.read());
        } else {
            throw new InvalidMuxSelectionException("Select FROM_DATA to read from memory");
        }
    }

    public void writeDmar() throws InvalidMuxSelectionException {
        if (aluOutputMux == AluOutputMux.TO_DMAR) {
            dataMemoryController.writeAddress(Math.toIntExact(alu.getOutput()));
        } else {
            throw new InvalidMuxSelectionException("Select TO_DMAR to write in address registry");
        }
    }

    public void readTos() throws InvalidMuxSelectionException {
        if (rightAluInputMux == RightAluInputMux.FROM_TOS) {
            alu.setRightInput(tos.readValue());
        } else {
            throw new InvalidMuxSelectionException("Select FROM_TOS to read from TOS");
        }
    }

    public void writeTos() throws InvalidMuxSelectionException {
        if (aluOutputMux == AluOutputMux.TO_TOS) {
            tos.writeValue(alu.getOutput());
            zeroFlag = alu.getOutput() == 0;
            negativeFlag = alu.getOutput() < 0;
        } else {
            throw new InvalidMuxSelectionException("Select TO_TOS to write in TOS");
        }
    }

    public void readIp() throws InvalidMuxSelectionException {
        if (rightAluInputMux == RightAluInputMux.FROM_IP) {
            alu.setRightInput(ip.readValue().longValue());
        } else {
            throw new InvalidMuxSelectionException("Select FROM_IP to read from IP");
        }
    }

    public void readAr() throws InvalidMuxSelectionException {
        if (leftAluInputMux == LeftAluInputMux.FROM_AR) {
            alu.setLeftInput(ar.readValue().longValue());
        } else {
            throw new InvalidMuxSelectionException("Select FROM_AR to read from AR");
        }
    }

    public void writeIp() throws InvalidMuxSelectionException {
        if (aluOutputMux == AluOutputMux.TO_IP) {
            ip.writeValue(Math.toIntExact(alu.getOutput()));
        } else {
            throw new InvalidMuxSelectionException("Select TO_IP to write in IP");
        }
    }

    public void writeDs() throws InvalidMuxSelectionException {
        if (aluOutputMux == AluOutputMux.TO_DS) {
            dataStack.push(alu.getOutput());
        } else {
            throw new InvalidMuxSelectionException("Select TO_DS to write in DS");
        }
    }

    public void writeRs() throws InvalidMuxSelectionException {
        if (aluOutputMux == AluOutputMux.TO_RS) {
            returnStack.push(alu.getOutput());
        } else {
            throw new InvalidMuxSelectionException("Select TO_RS to write in RS");
        }
    }

    public void readDs() throws InvalidMuxSelectionException {
        if (leftAluInputMux == LeftAluInputMux.FROM_DS) {
            alu.setLeftInput(dataStack.pop());
        } else {
            throw new InvalidMuxSelectionException("Select FROM_DS to read from DS");
        }
    }

    public void readRs() throws InvalidMuxSelectionException {
        if (leftAluInputMux == LeftAluInputMux.FROM_RS) {
            alu.setLeftInput(returnStack.pop());
        } else {
            throw new InvalidMuxSelectionException("Select FROM_RS to read from RS");
        }
    }

    public boolean zeroFlag() {
        return zeroFlag;
    }

    public boolean negativeFlag() {
        return negativeFlag;
    }

    public Long getTosValue() {
        return tos.readValue();
    }

    public Long getDsValue() {
        return !dataStack.empty() ? dataStack.peek() : null;
    }

    public Long getRsValue() {
        return !returnStack.empty() ? returnStack.peek() : null;
    }

    public String getOutputString() {
        if (outputBuilder.isEmpty()) {
            return null;
        } else {
            return outputBuilder.toString();
        }
    }

    public String getInputToken() {
        if (inputCharacters.empty()) {
            return null;
        } else {
            return String.valueOf(inputCharacters.peek());
        }
    }

}
