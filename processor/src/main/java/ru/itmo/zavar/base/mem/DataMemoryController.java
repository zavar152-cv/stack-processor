package ru.itmo.zavar.base.mem;

import lombok.RequiredArgsConstructor;
import ru.itmo.zavar.io.InputDevice;
import ru.itmo.zavar.io.OutputDevice;

@RequiredArgsConstructor
public final class DataMemoryController {
    private final Memory memory;
    private final InputDevice inputDevice;
    private final OutputDevice outputDevice;

    public void write(final Long value) {
        if (memory.readAR().equals(outputDevice.charAddress())) {
            outputDevice.writeChar(value);
        } else if (memory.readAR().equals(outputDevice.byteAddress())) {
            outputDevice.writeByte(value);
        } else {
            memory.write(value);
        }
    }

    public Long read() {
        if (memory.readAR().equals(inputDevice.address())) {
            return inputDevice.read();
        } else {
            return memory.read();
        }
    }

    public void writeAddress(final Integer address) {
        memory.writeAR(address);
    }

    public Integer readAddress() {
        return memory.readAR();
    }
}
