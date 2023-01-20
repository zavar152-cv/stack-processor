package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndTest {
    @Test
    public void testAnd() {
        System.out.println("Testing AND instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 0L, 1L, 0L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 3);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 4);
        program.add(InstructionCode.AND.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(22, controlUnit.getTickLog().get(21).controlUnitTicks());
        Assertions.assertNotEquals(1, controlUnit.getTickLog().get(21).tos());

    }
}
