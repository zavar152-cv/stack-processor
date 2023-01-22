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
        URL resource = getClass().getClassLoader().getResource("prob1.zorth");
        assert resource != null;
        Path path = Paths.get(resource.toURI());
        Path home = Path.of(System.getProperty("user.home"));
        String[] argsLauncher = {"-i", path.toString(), "-o", String.valueOf(home), "-f", "bin", "-d", "true"};
        Translator.main(argsLauncher);
        ZorthTranslator zorthTranslator = Translator.getZorthTranslator();

        //List<AbstractMap.SimpleEntry<InstructionCode, String>> program = zorthTranslator.getProgram(); //checks for program code

        Files.deleteIfExists(home.resolve("input"));
        Files.createFile(home.resolve("input"));
        Files.writeString(home.resolve("input"), """
                {
                   "tokens": ["10000"]
                }""",
                StandardOpenOption.APPEND);
        String[] argsProcessor = {"-p", home.resolve("compiled.bin").toString(), "-d",
                home.resolve("data.dbin").toString(), "-dg", "true", "-i", home.resolve("input").toString()};
        Processor.main(argsProcessor);

        //List<TickLog> log = Processor.getLog();
    }
}
