package ru.itmo.zavar.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;
import ru.itmo.zavar.exception.OutOfInputException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OutputTest {
    @Test
    public void testOutput() {
        System.out.println("Testing OUTPUT...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 0L, (long) 'a', (long) '5', (long) '1');

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 3);
        program.add((InstructionCode.ST.getBinary().longValue() << 24) + 0);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 4);
        program.add((InstructionCode.ST.getBinary().longValue() << 24) + 0);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 5);
        program.add((InstructionCode.ST.getBinary().longValue() << 24) + 0);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), "b23", false);
        controlUnit.start();
        Assertions.assertEquals("a51", controlUnit.getTickLog().get(45).out());
    }
}
