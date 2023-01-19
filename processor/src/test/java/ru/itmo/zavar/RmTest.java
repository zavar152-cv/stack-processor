package ru.itmo.zavar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RmTest {
    @Test
    public void testMr() {
        System.out.println("Testing RM instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 23L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2);
        program.add(InstructionCode.MR.getBinary().longValue() << 24);
        program.add(InstructionCode.RM.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), true);
        controlUnit.start();
        Assertions.assertEquals(21, controlUnit.getTickLog().get(20).controlUnitTicks());
        Assertions.assertEquals(23, controlUnit.getTickLog().get(20).tos());

    }
}
