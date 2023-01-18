package ru.itmo.zavar.base.mem;

import ru.itmo.zavar.base.register.Register;
import ru.itmo.zavar.exception.MemoryCellConstraintException;
import ru.itmo.zavar.exception.RegisterConstraintException;

import java.util.ArrayList;
import java.util.List;

public sealed class Memory permits ProtectedMemory {
    protected final Register<Integer> addressRegistry;
    protected final ArrayList<Long> memory;
    protected final Byte bits;
    protected final Long constraint;
    public Memory(final Integer cellsCount, final Byte cellSize) {
        memory = new ArrayList<>(cellsCount);
        for(int i = 0; i < cellsCount; i++) {
            memory.add(0L);
        }
        addressRegistry = new Register<>(cellsCount, 0);
        bits = cellSize;
        constraint = (long) Math.pow(2, bits);
    }

    public Integer readAR() {
        return addressRegistry.readValue();
    }

    public void writeAR(final Integer address) throws RegisterConstraintException {
        addressRegistry.writeValue(address);
    }

    public void write(final Long value) throws MemoryCellConstraintException {
        if (value > constraint) {
            throw new MemoryCellConstraintException("Provided value %s is greater than max %s".formatted(value, constraint));
        }
        if (value < -constraint) {
            throw new MemoryCellConstraintException("Provided value %s is less than min %s".formatted(value, constraint));
        }
        memory.set(addressRegistry.readValue(), value);
    }

    public Long read() {
        return memory.get(addressRegistry.readValue());
    }

}
