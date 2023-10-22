package ru.itmo.zavar.zorth;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public record ProgramAndDataDto(List<Byte[]> program, List<Byte[]> data) {
}
