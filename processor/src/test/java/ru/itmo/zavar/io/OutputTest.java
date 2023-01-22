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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OutputTest {
    @Test
    public void testOutput() throws ParseException {
        System.out.println("Testing OUTPUT...");
        ArrayList<Long> program = new ArrayList<>();

        List<Long> dataMemory = Arrays.asList(0L, 0L, 0L, 97L, (long) '5', (long) 'a');

        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 3);
        program.add((InstructionCode.ADDR.getBinary().longValue() << 24) + 0);
        program.add(InstructionCode.ST.getBinary().longValue() << 24);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 4);
        program.add((InstructionCode.ADDR.getBinary().longValue() << 24) + 2);
        program.add(InstructionCode.ST.getBinary().longValue() << 24);
        program.add((InstructionCode.LIT.getBinary().longValue() << 24) + 5);
        program.add((InstructionCode.ADDR.getBinary().longValue() << 24) + 2);
        program.add(InstructionCode.ST.getBinary().longValue() << 24);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse("""
                {
                   "tokens": ["b", "2", "3"]
                }""");
        JSONArray input = (JSONArray) jsonObject.get("tokens");
        ControlUnit controlUnit = new ControlUnit(program, new ArrayList<>(dataMemory), input, false);
        controlUnit.start();
        Assertions.assertEquals("975a", controlUnit.getTickLog().get(60).out());
    }
}
