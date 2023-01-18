package ru.itmo.zavar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LitAddTest {

    @Test
    public void testAdd() {
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 13L, 2L, 66L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 3);
        program.add(InstructionCode.ADD.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory));
        controlUnit.start();
        Assertions.assertEquals(controlUnit.getTickLog().get(21).controlUnitTicks(), 22);
        Assertions.assertEquals(controlUnit.getTickLog().get(21).tos(), 15);
    }

    @Test
    public void testLit() {
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 13L, 2L, 66L);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 4);
        program.add(InstructionCode.HALT.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory));
        controlUnit.start();
        Assertions.assertEquals(controlUnit.getTickLog().get(10).controlUnitTicks(), 11);
        Assertions.assertEquals(controlUnit.getTickLog().get(10).tos(), 66);
    }

}
