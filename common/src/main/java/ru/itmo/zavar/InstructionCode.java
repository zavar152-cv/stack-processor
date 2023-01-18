package ru.itmo.zavar;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum InstructionCode {
    HALT("0000000", "HALT"),
    ADD("0000001", "ADD"),
    SUB("0000010", "SUB"),
    MUL("0000011", "MUL"),
    DIV("0000100", "ADD"),
    AND("0000101", "AND"),
    OR("0000110", "OR"),
    NOT("0000111", "NOT"),
    XOR("0001000", "XOR"),
    EQ("0001001", "EQ"),
    GR("0001010", "GR"),
    LE("0001011", "LE"),
    DROP("0001100", "DROP"),
    DUP("0001101", "DUP"),
    OVER("0001111", "OVER"),
    SWAP("0010000", "SWAP"),
    MR("0010001", ">R"),
    RM("0010010", "R>"),
    ST("0010011", "!"),
    FT("0010100", "@"),
    LIT("0010101", "[LIT]", true),
    JMP("0010110", "[JMP]", true),
    IF("0010111", "[IF]", true), //TODO req many IF?
    CALL("0011000", "[CALL]", true),
    LOOP("0011001", "[LOOP]", true),
    NOPE("0011010", "NOPE"),
    EXIT("0011011", "EXIT"); //TODO reserved

    private final Short binary;
    private final String mnemonic;
    private final boolean requiredArg;

    InstructionCode(final String binary, final String mnemonic) {
        this.binary = Short.parseShort(binary, 2);
        this.mnemonic = mnemonic;
        this.requiredArg = false;
    }

    InstructionCode(final String binary, final String mnemonic, final boolean requiredArg) {
        String fullBinary;
        if (requiredArg) {
            fullBinary = binary.concat("1");
        } else {
            fullBinary = binary.concat("0");
        }
        this.binary = Short.parseShort(fullBinary, 2);
        this.mnemonic = mnemonic;
        this.requiredArg = requiredArg;
    }

    public static InstructionCode valueByBinary(final String binary) {
        return Arrays.stream(values()).filter(instructionCode -> Integer.toBinaryString(instructionCode.binary)
                .equals(binary)).findFirst().orElse(null);
    }
}