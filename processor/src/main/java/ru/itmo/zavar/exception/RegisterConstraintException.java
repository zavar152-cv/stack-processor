package ru.itmo.zavar.exception;

public class RegisterConstraintException extends RuntimeException {
    public RegisterConstraintException(final String message) {
        super(message);
    }
}
