package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IfZeroTest {
    @Test
    public void testIf() {
        System.out.println("Testing IF instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 15L, 14L, 66L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2); // 0
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 3); // 1
        program.add(InstructionCode.EQ.getBinary().longValue() << 24); // 2
        program.add((InstructionCode.IF.getBinary().longValue() << 24) + 6); // 3
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 4
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 5
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 4); // 6
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(35, controlUnit.getTickLog().get(34).controlUnitTicks());
        Assertions.assertEquals(66, controlUnit.getTickLog().get(34).tos());
    }
}
