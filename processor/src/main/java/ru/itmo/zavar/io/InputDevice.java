package ru.itmo.zavar.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ru.itmo.zavar.exception.OutOfInputException;

import java.util.Stack;

@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public record InputDevice(Integer address, Stack<String> tokens) {
    public Long read() throws OutOfInputException {
        if (tokens().empty()) {
            throw new OutOfInputException("Input device with address " + address + " is out of tokens");
        }
        if (tokens.peek().length() == 1 && Character.isDigit(tokens.peek().charAt(0))) {
            final int radix = 10;
            return (long) Character.digit(tokens.pop().charAt(0), radix);
        } else if (tokens.peek().length() == 1 && Character.isDefined(tokens.peek().charAt(0))) {
            return (long) tokens.pop().charAt(0);
        } else {
            return Long.parseLong(tokens.pop());
        }
    }
}
