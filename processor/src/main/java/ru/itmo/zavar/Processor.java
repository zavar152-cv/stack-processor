package ru.itmo.zavar;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("MagicNumber")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
//CHECKSTYLE:OFF
public class Processor {
    public static void main(final String[] args) {
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 13L, 2L, 66L);

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 2);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 3);
        program.add(InstructionCode.ADD.getBinary().longValue() << 24);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 4);
        program.add(InstructionCode.HALT.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), true);
        controlUnit.start();
        System.out.println("lol");

    }
}
//CHECKSTYLE:ON
