package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoopTest {
    @Test
    public void testLoop() {
        System.out.println("Testing LOOP instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 0L, 1L, 2L, 5L, 4L);

        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 0
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 6); // 1

        program.add((InstructionCode.ADDR.getBinary().longValue() << 24) + 6); // 2
        program.add(InstructionCode.ST.getBinary().longValue() << 24); // 3
        program.add((InstructionCode.ADDR.getBinary().longValue() << 24) + 6); // 4
        program.add(InstructionCode.FT.getBinary().longValue() << 24); // 5

        program.add((InstructionCode.LOOP.getBinary().longValue() << 24) + 2); // 6
        program.add(InstructionCode.HALT.getBinary().longValue() << 24); // 7
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 8
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(0, controlUnit.getTickLog().get(154).tos());
        Assertions.assertNull(controlUnit.getTickLog().get(154).ds());
    }
}
