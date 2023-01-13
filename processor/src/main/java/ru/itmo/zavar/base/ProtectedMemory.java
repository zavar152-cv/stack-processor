package ru.itmo.zavar.base;

import java.util.ArrayList;

public final class ProtectedMemory<T extends Number> extends Memory<T> {
    public ProtectedMemory(final Integer cellsCount, final ArrayList<T> initial) {
        super(cellsCount);
        super.memory.addAll(0, initial);
    }

    @Override
    public void write(final T value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
