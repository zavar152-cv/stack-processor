package ru.itmo.zavar.integration;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.Translator;
import ru.itmo.zavar.Processor;
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

public class CatTest {
    @Test
    public void launch() throws URISyntaxException, IOException, ParseException {
        URL resource = getClass().getClassLoader().getResource("cat.zorth");
        assert resource != null;
        Path path = Paths.get(resource.toURI());
        Path home = Path.of(System.getProperty("user.home"));
        String[] argsLauncher = {"-i", path.toString(), "-o", String.valueOf(home), "-f", "bin", "-d", "true"};
        Translator.main(argsLauncher);
        ZorthTranslator zorthTranslator = Translator.getZorthTranslator();
        Assertions.assertTrue(zorthTranslator.getLiteralAddressTable().containsKey(1L)); //checks for literals in memory
        List<AbstractMap.SimpleEntry<InstructionCode, String>> program = zorthTranslator.getProgram(); //checks for program code
        Assertions.assertEquals(InstructionCode.HALT, program.get(program.size() - 1).getKey());

        Assertions.assertEquals(InstructionCode.LIT, program.get(0).getKey());
        Assertions.assertEquals("3", program.get(0).getValue());

        Assertions.assertEquals(InstructionCode.ADDR, program.get(1).getKey());
        Assertions.assertEquals("1", program.get(1).getValue());

        Assertions.assertEquals(InstructionCode.FT, program.get(2).getKey());

        Assertions.assertEquals(InstructionCode.ADDR, program.get(3).getKey());
        Assertions.assertEquals("2", program.get(3).getValue());

        Assertions.assertEquals(InstructionCode.ST, program.get(4).getKey());

        Assertions.assertEquals(InstructionCode.LOOP, program.get(5).getKey());
        Assertions.assertEquals("0", program.get(5).getValue());

        Files.deleteIfExists(home.resolve("input"));
        Files.createFile(home.resolve("input"));
        Files.writeString(home.resolve("input"), """
                {
                   "tokens": ["f", "o", "o", "\\n"]
                }""",
                StandardOpenOption.APPEND);
        String[] argsProcessor = {"-p", home.resolve("compiled.bin").toString(), "-d",
                home.resolve("data.dbin").toString(), "-dg", "true", "-i", home.resolve("input").toString()};
        Processor.main(argsProcessor);

        List<TickLog> log = Processor.getLog();
        Assertions.assertEquals("f", log.get(0).in()); //checks for processor input
        Assertions.assertEquals("foo\n", log.get(log.size() - 1).out()); //checks for processor output
    }
}
