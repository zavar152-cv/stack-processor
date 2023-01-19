package ru.itmo.zavar.io;

public record OutputDevice(Integer address, StringBuilder stringBuilder) {
    public void write(final Long value) {
        stringBuilder.append((char) value.longValue());
    }
}
