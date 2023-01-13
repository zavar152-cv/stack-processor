package ru.itmo.zavar.base;

import ru.itmo.zavar.exception.RegisterConstraintException;

import java.util.ArrayList;

public sealed class Memory<T extends Number> permits ProtectedMemory {
    protected final Register<Integer> addressRegistry;
    protected final ArrayList<T> memory;
    public Memory(final Integer cellsCount) {
        memory = new ArrayList<>(cellsCount);
        addressRegistry = new Register<>(cellsCount, 0);
    }

    public Integer readAR() {
        return addressRegistry.readValue();
    }

    public void writeAR(final Integer address) throws RegisterConstraintException {
        addressRegistry.writeValue(address);
    }

    public void write(final T value) {
        memory.set(addressRegistry.readValue(), value);
    }

    public T oe() {
        return memory.get(addressRegistry.readValue());
    }

}
