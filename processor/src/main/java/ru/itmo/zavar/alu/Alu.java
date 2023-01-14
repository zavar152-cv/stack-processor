package ru.itmo.zavar.alu;

import lombok.Getter;
import lombok.Setter;

public final class Alu {
    @Setter
    private AluOperation operation;
    @Setter
    private Long leftInput;
    @Setter
    private Long rightInput;
    @Getter
    private Long output;

    public void calculate() {
        switch (operation) {
            case PLUS -> output = rightInput + leftInput;
            case MINUS -> output = rightInput - leftInput;
            case MULTIPLY -> output = rightInput * leftInput;
            case DIVIDE -> output = rightInput / leftInput;
            case AND -> output = rightInput & leftInput;
            case OR -> output = rightInput | leftInput;
            case XOR -> output = rightInput ^ leftInput;
            case NOT -> output = ~rightInput;
            case EQ -> output = rightInput.equals(leftInput) ? 1L : 0L;
            case GR -> output = rightInput > leftInput ? 1L : 0L;
            case LE -> output = rightInput < leftInput ? 1L : 0L;
            case RIGHT -> output = rightInput;
            case LEFT -> output = leftInput;
            default -> throw new UnsupportedOperationException();
        }
    }

}
