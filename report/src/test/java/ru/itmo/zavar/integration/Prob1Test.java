package ru.itmo.zavar.integration;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.Processor;
import ru.itmo.zavar.Translator;
import ru.itmo.zavar.log.TickLog;
import ru.itmo.zavar.zorth.ZorthTranslator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.List;

public class Prob1Test {
    @Test
    public void launch() throws URISyntaxException, IOException, ParseException {
        URL srcUrl = getClass().getClassLoader().getResource("prob1.zorth"); // path for source code
        assert srcUrl != null;
        Path progPath = Paths.get(srcUrl.toURI());

        URL prob1CompUrl = getClass().getClassLoader().getResource("prob1comp.bin"); // path for compare program
        assert prob1CompUrl != null;
        Path prob1CompPath = Paths.get(prob1CompUrl.toURI());

        URL prob1DataUrl = getClass().getClassLoader().getResource("prob1data.dbin"); // path for compare data
        assert prob1DataUrl != null;
        Path prob1DataPath = Paths.get(prob1DataUrl.toURI());

        Path home = Path.of(System.getProperty("user.home"));
        String[] argsLauncher = {"-i", progPath.toString(), "-o", String.valueOf(home), "-f", "bin", "-d", "true"};
        Translator.main(argsLauncher); //translate program

        Path inputPath = home.resolve("input"); // path for input file
        Path compiledPath = home.resolve("compiled.bin"); // path for program
        Path dataPath = home.resolve("data.dbin"); // path for data

        Assertions.assertEquals(-1L, Files.mismatch(compiledPath, prob1CompPath)); // check for identical programs
        Assertions.assertEquals(-1L, Files.mismatch(dataPath, prob1DataPath)); // check for identical data

        System.out.println("Test 1000:");
        Files.deleteIfExists(inputPath);
        Files.createFile(inputPath);
        Files.writeString(inputPath, """
                {
                   "tokens": ["1000"]
                }""",
                StandardOpenOption.APPEND); //prepare input
        String[] argsProcessor = {"-p", compiledPath.toString(), "-d",
                dataPath.toString(), "-dg", "true", "-i", inputPath.toString()};
        Processor.main(argsProcessor); // run for 1000 numbers

        List<TickLog> log = Processor.getLog();
        Assertions.assertEquals("233168", log.get(log.size() - 1).out()); // check answer

        System.out.println("\nTest 10000:");
        Files.deleteIfExists(inputPath);
        Files.createFile(inputPath);
        Files.writeString(inputPath, """
                {
                   "tokens": ["10000"]
                }""",
                StandardOpenOption.APPEND); //prepare input
        String[] argsProcessor1 = {"-p", compiledPath.toString(), "-d",
                dataPath.toString(), "-dg", "false", "-i", inputPath.toString()};
        Processor.main(argsProcessor1); // run for 10000 numbers

        log = Processor.getLog(); // test for 10000 numbers
        Assertions.assertEquals("23331668", log.get(log.size() - 1).out()); // check answer

        System.out.println("\nTest 100:");
        Files.deleteIfExists(inputPath);
        Files.createFile(inputPath);
        Files.writeString(inputPath, """
                {
                   "tokens": ["100"]
                }""",
                StandardOpenOption.APPEND); //prepare input
        String[] argsProcessor2 = {"-p", compiledPath.toString(), "-d",
                dataPath.toString(), "-dg", "false", "-i", inputPath.toString()};
        Processor.main(argsProcessor2); // run for 100 numbers

        log = Processor.getLog(); // test for 100 numbers
        Assertions.assertEquals("2318", log.get(log.size() - 1).out()); // check answer
    }
}
