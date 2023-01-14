package ru.itmo.zavar.base.mem;

import java.util.ArrayList;

public final class ProtectedMemory extends Memory {
    public ProtectedMemory(final Integer cellsCount, final Byte cellSize, final ArrayList<Long> initial) {
        super(cellsCount, cellSize);
        super.memory.addAll(0, initial);
    }

    @Override
    public void write(final Long value) {
        throw new UnsupportedOperationException();
    }
}
