package ru.itmo.zavar.constraint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.base.mem.Memory;
import ru.itmo.zavar.base.register.Register;
import ru.itmo.zavar.exception.MemoryCellConstraintException;
import ru.itmo.zavar.exception.RegisterConstraintException;

public class MemoryConstraintTest {
    @Test
    public void memoryTest() {
        System.out.println("Testing memory constraint...");
        Memory memory = new Memory(10, (byte) 2);
        memory.writeAR(0);
        Assertions.assertThrows(MemoryCellConstraintException.class, () -> {
            memory.write(100L);
        });
        Assertions.assertThrows(MemoryCellConstraintException.class, () -> {
            memory.write(-100L);
        });
        Assertions.assertThrows(RegisterConstraintException.class, () -> {
            memory.writeAR(11);
        });
    }
}
