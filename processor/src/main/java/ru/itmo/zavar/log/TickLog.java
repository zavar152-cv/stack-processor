package ru.itmo.zavar.log;

import org.apache.commons.text.StringEscapeUtils;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.Stage;

public record TickLog(Long controlUnitTicks, Byte tc, Stage stage, Long cr, InstructionCode instructionCode, Integer ip,
                      Integer ar, Long tos,
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
        System.out.print(", OUT: " + StringEscapeUtils.escapeJava(out));
        System.out.println(", IN: " + StringEscapeUtils.escapeJava(in));
    }

    @Override
    public String toString() {
        return "Tick: " + controlUnitTicks
                + ", TC: " + tc
                + ", Stage: " + stage
                + ", CR: " + cr + " {"
                + instructionCode + "}"
                + ", IP: " + ip
                + ", AR: " + ar
                + ", TOS: " + tos
                + ", DS: " + ds
                + ", RS: " + rs
                + ", OUT: " + StringEscapeUtils.escapeJava(out)
                + ", IN: " + StringEscapeUtils.escapeJava(in);
    }
}
