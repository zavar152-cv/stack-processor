package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JmpTest {
    @Test
    public void testJmp() {
        System.out.println("Testing JMP instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 15L, 0L);

        program.add((InstructionCode.JMP.getBinary().longValue() << 24) + 3); // 0
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 1
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 2
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2); // 3
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), true);
        controlUnit.start();
        Assertions.assertEquals(16, controlUnit.getTickLog().get(15).controlUnitTicks());
        Assertions.assertEquals(15, controlUnit.getTickLog().get(15).tos());
    }
}
