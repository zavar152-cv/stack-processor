package ru.itmo.zavar.exception;

public class InvalidInstructionException extends ControlUnitException {
    public InvalidInstructionException(final String message) {
        super(message);
    }
}
