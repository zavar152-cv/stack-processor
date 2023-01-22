package ru.itmo.zavar.zorth;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ZorthPrimitives {

    PLUS("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    AND("AND"),
    OR("OR"),
    NEG("NEG"),
    XOR("XOR"),
    NOT("NOT"),
    EQ("="),
    NEQ("!="),
    GR(">"),
    LE("<"),
    INC("INC"),
    DEC("DEC"),
    SWAP("SWAP"),
    DROP("DROP"),
    DUP("DUP"),
    OVER("OVER"),
    ST("!"),
    FT("@"),
    DOT("."),
    EMIT("EMIT"),
    IN("IN");

    private final String value;

    ZorthPrimitives(final String v) {
        value = v;
    }

    public static ZorthPrimitives byValue(final String v) {
        return Arrays.stream(values()).filter(z -> z.value.equals(v))
                .findFirst().orElse(null);
    }
}
