package ru.itmo.zavar.io;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;
import ru.itmo.zavar.exception.OutOfInputException;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputTest {
    @Test
    public void testInput() throws ParseException {
        System.out.println("Testing INPUT...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 0L, 1L, 2L, 5L, 3L);

        program.add((InstructionCode.ADDR.getBinary().longValue() << 24) + 1);
        program.add(InstructionCode.FT.getBinary().longValue() << 24);
        program.add((InstructionCode.JMP.getBinary().longValue() << 24) + 0);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse("""
                {
                   "tokens": ["a", "2", "3"]
                }""");
        JSONArray input = (JSONArray) jsonObject.get("tokens");
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), input, true);
        Assertions.assertThrows(OutOfInputException.class, controlUnit::start);
    }
}
