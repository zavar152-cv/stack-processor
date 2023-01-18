package ru.itmo.zavar.log;

import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.Stage;

public record TickLog(Long controlUnitTicks, Byte tc, Stage stage, Long cr, InstructionCode instructionCode, Integer ip, Integer ar, Long tos) {
    public void print() {
        System.out.print("Tick: " + controlUnitTicks);
        System.out.print(", TC: " + tc);
        System.out.print(", Stage: " + stage);
        System.out.print(", CR: " + cr + " {"
                + instructionCode + "}");
        System.out.print(", IP: " + ip);
        System.out.print(", AR: " + ar);
        System.out.println(", TOS: " + tos);
    }
}
