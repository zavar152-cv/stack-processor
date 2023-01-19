package ru.itmo.zavar.log;

import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.Stage;

public record TickLog(Long controlUnitTicks, Byte tc, Stage stage, Long cr, InstructionCode instructionCode, Integer ip, Integer ar, Long tos,
                      Long ds, Long rs, String out, String in) {
    public void print() {
        System.out.print("Tick: " + controlUnitTicks);
        System.out.print(", TC: " + tc);
        System.out.print(", Stage: " + stage);
        System.out.print(", CR: " + cr + " {"
                + instructionCode + "}");
        System.out.print(", IP: " + ip);
        System.out.print(", AR: " + ar);
        System.out.print(", TOS: " + tos);
        System.out.print(", DS: " + ds);
        System.out.print(", RS: " + rs);
        System.out.print(", OUT: " + out);
        System.out.println(", IN: " + in);
    }
}
