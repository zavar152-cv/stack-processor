package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;
import ru.itmo.zavar.exception.InvalidInstructionException;
import ru.itmo.zavar.exception.ReservedInstructionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvalidTest {
    @Test
    public void testInvalid() {
        System.out.println("Testing null instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 0L, 15L, 14L, 66L);

        program.add((Long.valueOf("11111111", 2) << 24)); // 0
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        Assertions.assertThrows(InvalidInstructionException.class, controlUnit::start);
    }
}
