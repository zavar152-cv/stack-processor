package ru.itmo.zavar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BnotTest {
    @Test
    public void testBnot() {
        System.out.println("Testing BNOT instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 23L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2);
        program.add(InstructionCode.BNOT.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(15, controlUnit.getTickLog().get(14).controlUnitTicks());
        Assertions.assertEquals(-24, controlUnit.getTickLog().get(14).tos());

    }
}
