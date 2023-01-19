package ru.itmo.zavar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotTest {
    @Test
    public void testNot() {
        System.out.println("Testing NOT instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 23L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2);
        program.add(InstructionCode.NOT.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(15, controlUnit.getTickLog().get(14).controlUnitTicks());
        Assertions.assertEquals(-23, controlUnit.getTickLog().get(14).tos());

    }
}
