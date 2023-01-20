package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LitTest {
    @Test
    public void testLit() {
        System.out.println("Testing LIT instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 0L, 13L, 2L, 66L);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 5);
        program.add(InstructionCode.HALT.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(11, controlUnit.getTickLog().get(10).controlUnitTicks());
        Assertions.assertEquals(66, controlUnit.getTickLog().get(10).tos());
    }
}
