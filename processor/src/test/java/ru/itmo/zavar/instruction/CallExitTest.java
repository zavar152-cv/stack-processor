package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CallExitTest {
    @Test
    public void testCall() {
        System.out.println("Testing CALL instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 15L, 15L, 66L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2); // 0
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 3); // 1
        program.add(InstructionCode.EQ.getBinary().longValue() << 24); // 2
        program.add((InstructionCode.CALL.getBinary().longValue() << 24) + 8); // 3
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 4
        program.add(InstructionCode.HALT.getBinary().longValue() << 24); // 5
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 6
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 7
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 4); // 8
        program.add(InstructionCode.DUP.getBinary().longValue() << 24); // 9
        program.add(InstructionCode.ADD.getBinary().longValue() << 24); // 10
        program.add(InstructionCode.EXIT.getBinary().longValue() << 24); // 11
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 12
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(24, controlUnit.getTickLog().get(23).controlUnitTicks());
        Assertions.assertEquals(8, controlUnit.getTickLog().get(23).ip());
        Assertions.assertEquals(43, controlUnit.getTickLog().get(42).controlUnitTicks());
        Assertions.assertEquals(4, controlUnit.getTickLog().get(42).ip());
    }

    @Test
    public void testNestedCall() {
        System.out.println("Testing nested CALL instruction...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 15L, 15L, 66L, 89L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2); // 0
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 3); // 1
        program.add(InstructionCode.EQ.getBinary().longValue() << 24); // 2
        program.add((InstructionCode.CALL.getBinary().longValue() << 24) + 8); // 3
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 4
        program.add(InstructionCode.HALT.getBinary().longValue() << 24); // 5
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 6
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 7
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 4); // 8
        program.add(InstructionCode.DUP.getBinary().longValue() << 24); // 9
        program.add(InstructionCode.ADD.getBinary().longValue() << 24); // 10
        program.add((InstructionCode.CALL.getBinary().longValue() << 24) + 14); // 11
        program.add(InstructionCode.EXIT.getBinary().longValue() << 24); // 12
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 13
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 5); // 14
        program.add(InstructionCode.ADD.getBinary().longValue() << 24); // 15
        program.add(InstructionCode.EXIT.getBinary().longValue() << 24); // 16
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 17
        program.add(InstructionCode.NOPE.getBinary().longValue() << 24); // 18
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(24, controlUnit.getTickLog().get(23).controlUnitTicks());
        Assertions.assertEquals(8, controlUnit.getTickLog().get(23).ip());
        Assertions.assertEquals(45, controlUnit.getTickLog().get(44).controlUnitTicks());
        Assertions.assertEquals(14, controlUnit.getTickLog().get(44).ip());
        Assertions.assertEquals(60, controlUnit.getTickLog().get(59).controlUnitTicks());
        Assertions.assertEquals(12, controlUnit.getTickLog().get(59).ip());
        Assertions.assertEquals(64, controlUnit.getTickLog().get(63).controlUnitTicks());
        Assertions.assertEquals(4, controlUnit.getTickLog().get(63).ip());
    }
}
