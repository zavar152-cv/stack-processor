package ru.itmo.zavar.exception;

public class ReservedInstructionException extends ControlUnitException {
    public ReservedInstructionException() {
        super("Instruction is reserved");
    }
}
