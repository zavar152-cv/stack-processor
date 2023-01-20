package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddrTest {
    @Test
    public void testAddr() {
        System.out.println("Testing ADDR instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 1L, 2L, 5L, 3L);

        program.add((InstructionCode.ADDR.getBinary().longValue() << 24) + 5); // 0
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), true);
        controlUnit.start();
        Assertions.assertEquals(6, controlUnit.getTickLog().get(5).controlUnitTicks());
        Assertions.assertEquals(controlUnit.getTickLog().get(5).ar().longValue(), controlUnit.getTickLog().get(5).tos());
    }
}
