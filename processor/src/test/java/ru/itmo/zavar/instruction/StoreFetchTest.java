package ru.itmo.zavar.instruction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoreFetchTest {

    @Test
    public void storeFetchTest() {
        System.out.println("Testing ST and FT instructions...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 25L, 14L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2);
        program.add((InstructionCode.ST.getBinary().longValue() << 24) + 3);

        program.add((InstructionCode.FT.getBinary().longValue() << 24) + 3);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), false);
        controlUnit.start();
        Assertions.assertEquals(dataMemory.get(2), controlUnit.getTickLog().get(22).tos());
    }

}
