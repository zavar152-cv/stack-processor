package ru.itmo.zavar.exception;

public class InvalidInstructionException extends RuntimeException {
    public InvalidInstructionException(final String message) {
        super(message);
    }
}
