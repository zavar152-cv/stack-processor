package ru.itmo.zavar.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;
import ru.itmo.zavar.exception.OutOfInputException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputTest {
    @Test
    public void testInput() {
        System.out.println("Testing INPUT...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 1L, 2L, 5L, 3L);

        program.add((InstructionCode.FT.getBinary().longValue() << 24) + 1);
        program.add((InstructionCode.JMP.getBinary().longValue() << 24) + 0);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), "a2 3", false);
        Assertions.assertThrows(OutOfInputException.class, controlUnit::start);
    }
}
