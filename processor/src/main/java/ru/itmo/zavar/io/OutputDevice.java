package ru.itmo.zavar.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("EI_EXPOSE_REP")
public record OutputDevice(Integer byteAddress, Integer charAddress, StringBuilder stringBuilder) {
    public void writeChar(final Long value) {
        stringBuilder.append((char) value.longValue());
    }
    public void writeByte(final Long value) {
        stringBuilder.append(value);
    }
}
