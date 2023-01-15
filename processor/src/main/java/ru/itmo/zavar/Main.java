package ru.itmo.zavar;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.itmo.zavar.comp.ControlUnit;

import java.util.ArrayList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Main {
    public static void main(final String[] args) {
        short c = 253; // command code
        System.out.println(Integer.toBinaryString(c));
        long v = ((long) c) << 24; // extend to 32 bits
        System.out.println(Long.toBinaryString(v));

        long i = v + 16777214;
        System.out.println(Long.toBinaryString(i));

        System.out.println(Integer.toBinaryString((int) (i & 16777215))); // extract address

        System.out.println(Integer.toBinaryString((int) (i >> 24))); // extract command code

        ArrayList<Long> program = new ArrayList<>();
        program.add(InstructionCode.ADD.getBinary().longValue() << 24);
        program.add(InstructionCode.HALT.getBinary().longValue() << 24);
        ControlUnit controlUnit = new ControlUnit(program);
        controlUnit.start();
        System.out.println("lol");

    }
}
