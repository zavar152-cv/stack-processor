package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddTest {

    @Test
    public void testAdd() {
        System.out.println("Testing ADD instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 0L, 13L, 2L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 3);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 4);
        program.add(InstructionCode.ADD.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(22, controlUnit.getTickLog().get(21).controlUnitTicks());
        Assertions.assertEquals(15, controlUnit.getTickLog().get(21).tos());
    }
}
