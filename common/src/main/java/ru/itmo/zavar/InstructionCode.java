package ru.itmo.zavar;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum InstructionCode {
    HALT("00000000", "HALT"),
    ADD("00000001", "ADD"),
    SUB("00000010", "SUB"),
    MUL("00000011", "MUL"),
    DIV("00000100", "ADD"),
    AND("00000101", "AND"),
    OR("00000110", "OR"),
    NOT("00000111", "NOT"),
    XOR("00001000", "XOR"),
    EQ("00001001", "EQ"),
    GR("00001010", "GR"),
    LE("00001011", "LE"),
    DROP("00001100", "DROP"),
    DUP("00001101", "DUP"),
    OVER("00001111", "OVER"),
    SWAP("00010000", "SWAP"),
    MR("00010001", ">R"),
    RM("00010010", "R>"),
    ST("00010011", "!"),
    FT("00010100", "@"),
    LIT("00010101", "[LIT]", true),
    JMP("00010110", "[JMP]", true),
    IF("00010111", "[IF]", true), //TODO req many IF?
    CALL("00011000", "[CALL]", true),
    LOOP("00011001", "[LOOP]", true),
    NOPE("00011010", "NOPE"),
    EXIT("00011011", "EXIT"); //TODO reserved

    private final Short binary;
    private final String mnemonic;
    private final boolean requiredArg;

    InstructionCode(final String binary, final String mnemonic) {
        this.binary = Short.parseShort(binary, 2);
        this.mnemonic = mnemonic;
        this.requiredArg = false;
    }

    InstructionCode(final String binary, final String mnemonic, final boolean requiredArg) {
        this.binary = Short.parseShort(binary, 2);
        this.mnemonic = mnemonic;
        this.requiredArg = requiredArg;
    }

    public static InstructionCode valueByBinary(final String binary) {
        return Arrays.stream(values()).filter(instructionCode -> Integer.toBinaryString(instructionCode.binary)
                .equals(binary)).findFirst().orElse(null);
    }
}
