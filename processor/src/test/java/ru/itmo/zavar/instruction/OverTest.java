package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OverTest {
    @Test
    public void testOver() {
        System.out.println("Testing OVER instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 23L, 47L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 3);
        program.add(InstructionCode.OVER.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(25, controlUnit.getTickLog().get(24).controlUnitTicks());
        Assertions.assertEquals(23, controlUnit.getTickLog().get(24).tos());
        Assertions.assertEquals(47, controlUnit.getTickLog().get(24).ds());
    }
}
