package ru.itmo.zavar.io;

import ru.itmo.zavar.exception.OutOfInputException;

import java.util.Stack;

public record InputDevice(Integer address, Stack<Character> tokens) {
    public Long read() throws OutOfInputException {
        if (tokens().empty()) {
            throw new OutOfInputException("Input device with address " + address + " is out of tokens");
        }
        if (Character.isDigit(tokens.peek())) {
            final int radix = 10;
            return (long) Character.digit(tokens.pop(), radix);
        } else {
            return (long) tokens.pop();
        }
    }
}
