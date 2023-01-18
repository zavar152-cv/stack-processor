package ru.itmo.zavar;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Main {
    public static void main(final String[] args) {
        ArrayList<Long> program = new ArrayList<>();
        program.add(InstructionCode.ADD.getBinary().longValue() << 24);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 1);
        program.add(InstructionCode.HALT.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program);
        controlUnit.start();
        System.out.println("lol");

    }
}
