package ru.itmo.zavar.io;

public record OutputDevice(Integer byteAddress, Integer charAddress, StringBuilder stringBuilder) {
    public void writeChar(final Long value) {
        stringBuilder.append((char) value.longValue());
    }
    public void writeByte(final Long value) {
        stringBuilder.append(value);
    }
}
