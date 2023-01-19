package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubTest {

    @Test
    public void testSub() {
        System.out.println("Testing SUB instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 34L, 10L, 66L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 3);
        program.add(InstructionCode.SUB.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(22, controlUnit.getTickLog().get(21).controlUnitTicks());
        Assertions.assertEquals(-24, controlUnit.getTickLog().get(21).tos());

    }

}
