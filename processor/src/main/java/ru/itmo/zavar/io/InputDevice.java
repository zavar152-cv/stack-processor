package ru.itmo.zavar.io;

import lombok.RequiredArgsConstructor;
import ru.itmo.zavar.exception.OutOfInputException;

public record InputDevice(Integer address) {
    public Long read() throws OutOfInputException {
        return 0L;
    }
}