package ru.itmo.zavar.integration;

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
import java.util.ListIterator;

public class HelloTest {
    @Test
    public void launch() throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource("hello.zorth");
        assert resource != null;
        Path path = Paths.get(resource.toURI());
        Path home = Path.of(System.getProperty("user.home"));
        String[] argsLauncher = {"-i", path.toString(), "-o", String.valueOf(home), "-f", "bin", "-d", "true"};
        Translator.main(argsLauncher);
        ZorthTranslator zorthTranslator = Translator.getZorthTranslator();

        "Hello world!".chars().forEach(value -> {
            Assertions.assertTrue(zorthTranslator.getLiteralAddressTable().containsKey((long) value)); //checks for literals in memory
        });

        List<AbstractMap.SimpleEntry<InstructionCode, String>> program = zorthTranslator.getProgram(); //checks for program code
        Assertions.assertEquals(InstructionCode.HALT, program.get(program.size() - 1).getKey());
        var iterator = program.listIterator();
        for (int i = 0; i < (program.size() - 1) / 3; i++) {
            var next = iterator.next();
            Assertions.assertEquals(InstructionCode.LIT, next.getKey());
            next = iterator.next();
            Assertions.assertEquals(InstructionCode.ADDR, next.getKey());
            next = iterator.next();
            Assertions.assertEquals(InstructionCode.ST, next.getKey());
        }

        Files.deleteIfExists(home.resolve("input"));
        Files.createFile(home.resolve("input"));
        Files.writeString(home.resolve("input"), "", StandardOpenOption.APPEND);
        String[] argsProcessor = {"-p", home.resolve("compiled.bin").toString(), "-d",
                home.resolve("data.dbin").toString(), "-dg", "true", "-i", home.resolve("input").toString()};
        Processor.main(argsProcessor);

        List<TickLog> log = Processor.getLog();
        Assertions.assertEquals("Hello world!", log.get(log.size() - 1).out()); //checks for processor output
    }
}
