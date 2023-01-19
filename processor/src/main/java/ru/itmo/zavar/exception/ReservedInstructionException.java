package ru.itmo.zavar.exception;

public class ReservedInstructionException extends RuntimeException {
    public ReservedInstructionException() {
        super("Instruction is reserved");
    }
}
