package ru.itmo.zavar.exception;

public class OutOfInputException extends RuntimeException {
    public OutOfInputException(final String message) {
        super(message);
    }
}
