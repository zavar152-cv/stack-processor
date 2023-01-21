package ru.itmo.zavar;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.cli.*;
import ru.itmo.zavar.zorth.ZorthCompiler;

import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Launcher {
    public static void main(final String[] args) {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        input.setType(Path.class);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output path");
        output.setRequired(true);
        output.setType(Path.class);
        options.addOption(output);

        Option format = new Option("f", "format", true, "output file format");
        format.setRequired(true);
        options.addOption(format);

        Option debugOption = new Option("d", "debug", true, "debug");
        debugOption.setRequired(true);
        options.addOption(debugOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("translator.jar", options);
            System.exit(1);
        }

        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");
        String outputFormat = cmd.getOptionValue("format");
        boolean debug = Boolean.parseBoolean(cmd.getOptionValue("debug"));

        ZorthCompiler zorthCompiler = new ZorthCompiler(Path.of(inputFilePath), Path.of(outputFilePath), outputFormat.equals("bin"));
        try {
            zorthCompiler.compile(debug);
            zorthCompiler.linkage(debug);
            zorthCompiler.saveProgramAndData();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
