package ru.itmo.zavar;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.ArrayUtils;
import ru.itmo.zavar.comp.ControlUnit;
import ru.itmo.zavar.exception.ControlUnitException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Processor {
    public static void main(final String[] args) throws IOException {
        Options options = new Options();

        Option programOption = new Option("p", "program", true, "program path");
        programOption.setRequired(true);
        programOption.setType(Path.class);
        options.addOption(programOption);

        Option dataOption = new Option("d", "data", true, "data path");
        dataOption.setRequired(true);
        dataOption.setType(Path.class);
        options.addOption(dataOption);

        Option inputOption = new Option("i", "input", true, "input path");
        inputOption.setRequired(true);
        inputOption.setType(Path.class);
        options.addOption(inputOption);

        Option debugOption = new Option("dg", "debug", true, "debug");
        debugOption.setRequired(true);
        options.addOption(debugOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("processor.jar", options);
            System.exit(1);
        }

        Path programPath = Path.of(cmd.getOptionValue("program"));
        Path dataPath = Path.of(cmd.getOptionValue("data"));
        Path inputPath = Path.of(cmd.getOptionValue("input"));
        boolean debug = Boolean.parseBoolean(cmd.getOptionValue("debug"));

        ArrayList<Long> program = new ArrayList<>();
        ArrayList<Long> data = new ArrayList<>();

        String input = Files.readString(inputPath);

        byte[] bytesProg = Files.readAllBytes(programPath);
        List<Byte[]> instructions = splitArray(ArrayUtils.toObject(bytesProg));
        instructions.forEach(bInst -> program.add(InstructionCode.bytesToLong(ArrayUtils.toPrimitive(bInst))));

        byte[] bytesData = Files.readAllBytes(dataPath);
        List<Byte[]> datas = splitArray(ArrayUtils.toObject(bytesData));
        datas.forEach(bData -> data.add(InstructionCode.bytesToLong(ArrayUtils.toPrimitive(bData))));
        ControlUnit controlUnit = new ControlUnit(program, data, input, debug);
        try {
            controlUnit.start();
        } catch (ControlUnitException e) {
            System.err.println(e.getMessage());
        }

    }

    private static <T> List<T[]> splitArray(final T[] array) {

        int numberOfArrays = array.length / Long.BYTES;
        int remainder = array.length % Long.BYTES;

        int start = 0;
        int end = 0;

        List<T[]> list = new ArrayList<T[]>();
        for (int i = 0; i < numberOfArrays; i++) {
            end += Long.BYTES;
            list.add(Arrays.copyOfRange(array, start, end));
            start = end;
        }

        if (remainder > 0) {
            list.add(Arrays.copyOfRange(array, start, (start + remainder)));
        }
        return list;
    }

}
