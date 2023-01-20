package ru.itmo.zavar.constraint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;
import ru.itmo.zavar.exception.DsEmpty;
import ru.itmo.zavar.exception.RsEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DsRsEmptyTest {
    @Test
    public void testRsEmpty() {
        System.out.println("Testing empty RS...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 0L, 1L, 2L, 5L, 3L);

        program.add(InstructionCode.RM.getBinary().longValue() << 24); // 0
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        Assertions.assertThrows(RsEmpty.class, controlUnit::start);
    }
    @Test
    public void testDsEmpty() {
        System.out.println("Testing empty DS...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 0L, 1L, 2L, 5L, 3L);

        program.add(InstructionCode.DROP.getBinary().longValue() << 24); // 0
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        Assertions.assertThrows(DsEmpty.class, controlUnit::start);
    }
}
