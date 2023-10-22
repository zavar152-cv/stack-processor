package ru.itmo.zavar.zorth;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.exception.InvalidFunctionNameException;
import ru.itmo.zavar.exception.InvalidStringException;
import ru.itmo.zavar.exception.InvalidVariableNameException;
import ru.itmo.zavar.exception.UnknownWordException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

@RequiredArgsConstructor
public class ZorthTranslator {
    private final Path inputFilePath;
    private final Path outputFilePath;
    private final boolean isBinary;

    private final Map<AbstractMap.SimpleEntry<String, Integer>, Integer> functionAddressTable = new LinkedHashMap<>(); // (name, length), address
    private final HashMap<String, Integer> variableAddressTable = new HashMap<>(); // name, address
    private final HashMap<Long, Integer> literalAddressTable = new HashMap<>(); // value, address
    private final ArrayList<AbstractMap.SimpleEntry<InstructionCode, String>> functionsProgram = new ArrayList<>();
    private final ArrayList<AbstractMap.SimpleEntry<InstructionCode, String>> mainProgram = new ArrayList<>();
    private final ArrayList<AbstractMap.SimpleEntry<InstructionCode, String>> program = new ArrayList<>();
    private final ArrayList<Long> data = new ArrayList<>();
    private boolean isFunction = false;
    private final ArrayList<Integer> tokensInLine = new ArrayList<>();
    private Integer tokenNumber = 0;
    private Integer lineNumber = 0;
    private Integer positionInLine = 0;

    private ArrayList<String> debugMessages = new ArrayList<>();

    public void compileFromString(final boolean debug, final String text) {
        ArrayList<String> tokens = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new StringReader(text))) {
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    throw new IOException();
                }
                line = line.replaceAll("//+.*", "");
                if (!line.isEmpty()) {
                    List<String> list = Arrays.stream(line.split(" ")).toList();
                    tokensInLine.add(list.size());
                    tokens.addAll(list);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Text is empty");
        }
        ListIterator<String> stringListIterator = tokens.listIterator();
        parseBase(stringListIterator);
        mainProgram.add(new AbstractMap.SimpleEntry<>(InstructionCode.HALT, ""));

        if (debug) {
            prepareCompilerDebug();
            debugMessages.forEach(System.out::println);
        } else {
            debugMessages.clear();
            debugMessages.add("Debug is disabled");
        }
    }

    public void compileFromFile(final boolean debug) {
        ArrayList<String> tokens = new ArrayList<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(inputFilePath)) {
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    throw new IOException();
                }
                line = line.replaceAll("//+.*", "");
                if (!line.isEmpty()) {
                    List<String> list = Arrays.stream(line.split(" ")).toList();
                    tokensInLine.add(list.size());
                    tokens.addAll(list);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        ListIterator<String> stringListIterator = tokens.listIterator();
        parseBase(stringListIterator);
        mainProgram.add(new AbstractMap.SimpleEntry<>(InstructionCode.HALT, ""));

        if (debug) {
            prepareCompilerDebug();
            debugMessages.forEach(System.out::println);
        } else {
            debugMessages.clear();
            debugMessages.add("Debug is disabled");
        }
    }

    public void linkage(final boolean debug) {
        final int startAddress = 3;
        for (int i = 0; i < literalAddressTable.size() + variableAddressTable.size() + startAddress; i++) {
            data.add(0L);
        }
        int i = startAddress;
        for (Long literal : literalAddressTable.keySet()) {
            data.set(i, literal);
            literalAddressTable.replace(literal, i);
            i++;
        }

        for (String varName : variableAddressTable.keySet()) {
            data.set(i, 0L);
            variableAddressTable.replace(varName, i);
            i++;
        }

        int mainProgramLastAddress = mainProgram.size() - 1;
        int nextFuncAddress = mainProgramLastAddress + 1;

        for (var funcEntry : functionAddressTable.keySet()) {
            functionAddressTable.replace(funcEntry, nextFuncAddress);
            nextFuncAddress = nextFuncAddress + funcEntry.getValue();
        }
        program.addAll(mainProgram);
        program.addAll(functionsProgram);
        int currentAddress = 0;
        for (AbstractMap.SimpleEntry<InstructionCode, String> entry : program) {
            String[] addressArray = entry.getValue().split("\\$");
            if (addressArray.length == 2) {
                switch (addressArray[0]) {
                    case "lit" -> {
                        Integer address = literalAddressTable.get(Long.parseLong(addressArray[1]));
                        int index = program.indexOf(entry);
                        entry.setValue(address.toString());
                        program.set(index, entry);
                    }
                    case "var" -> {
                        Integer address = variableAddressTable.get(addressArray[1]);
                        int index = program.indexOf(entry);
                        entry.setValue(address.toString());
                        program.set(index, entry);
                    }
                    case "fun" -> {
                        AbstractMap.SimpleEntry<String, Integer> simpleEntry = functionAddressTable.keySet().stream().
                                filter(s -> s.getKey().equals(addressArray[1])).findFirst().orElse(null);
                        Integer address = functionAddressTable.get(simpleEntry);
                        int index = program.indexOf(entry);
                        entry.setValue(address.toString());
                        program.set(index, entry);
                    }
                    case "loop" -> {
                        int address = currentAddress - Integer.parseInt(addressArray[1]);
                        int index = program.indexOf(entry);
                        entry.setValue(Integer.toString(address));
                        program.set(index, entry);
                    }
                    case "if" -> {
                        int address = currentAddress + Integer.parseInt(addressArray[1]);
                        int index = program.indexOf(entry);
                        entry.setValue(Integer.toString(address));
                        program.set(index, entry);
                    }
                    default -> throw new NoSuchElementException("Invalid address placeholder: " + addressArray[0]);
                }
            }
            currentAddress++;
        }

        if (debug) {
            prepareLinkerDebug();
            debugMessages.forEach(System.out::println);
        } else {
            debugMessages.clear();
            debugMessages.add("Debug is disabled");
        }
    }

    public void saveProgramAndData() throws IOException {
        if (isBinary) {
            Path programPath = outputFilePath.resolve("compiled.bin");
            Files.deleteIfExists(programPath);
            Files.createFile(programPath);
            final int addrShift = 24;
            program.forEach(entry -> {
                byte[] bytes;
                if (!entry.getValue().isEmpty()) {
                    bytes = InstructionCode.longToBytes((entry.getKey().getBinary().longValue() << addrShift) + Integer.parseInt(entry.getValue()));
                } else {
                    bytes = InstructionCode.longToBytes((entry.getKey().getBinary().longValue() << addrShift));
                }
                try {
                    Files.write(programPath, bytes, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            Path dataPath = outputFilePath.resolve("data.dbin");
            Files.deleteIfExists(dataPath);
            Files.createFile(dataPath);
            data.forEach(aLong -> {
                byte[] bytes = InstructionCode.longToBytes(aLong);
                try {
                    Files.write(dataPath, bytes, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            Path programPath = outputFilePath.resolve("compiled.z");
            Files.deleteIfExists(programPath);
            Files.createFile(programPath);
            program.forEach(entry -> {
                String ins = "";
                if (!entry.getValue().isEmpty()) {
                    ins = entry.getKey().getMnemonic() + " " + entry.getValue();
                } else {
                    ins = entry.getKey().getMnemonic();
                }
                try {
                    Files.writeString(programPath, ins, StandardOpenOption.APPEND);
                    Files.writeString(programPath, "\n", StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            Path dataPath = outputFilePath.resolve("data.dz");
            Files.deleteIfExists(dataPath);
            Files.createFile(dataPath);
            data.forEach(aLong -> {
                String d = String.valueOf(aLong);
                try {
                    Files.writeString(dataPath, d, StandardOpenOption.APPEND);
                    Files.writeString(dataPath, "\n", StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public ProgramAndDataDto getCompiledProgramAndDataInBytes() {
        ArrayList<Byte[]> programInBytes = new ArrayList<>();
        final int addrShift = 24;
        program.forEach(entry -> {
            byte[] bytes;
            if (!entry.getValue().isEmpty()) {
                bytes = InstructionCode.longToBytes((entry.getKey().getBinary().longValue() << addrShift) + Integer.parseInt(entry.getValue()));
            } else {
                bytes = InstructionCode.longToBytes((entry.getKey().getBinary().longValue() << addrShift));
            }
            programInBytes.add(ArrayUtils.toObject(bytes));
        });

        ArrayList<Byte[]> dataInBytes = new ArrayList<>();
        data.forEach(aLong -> {
            byte[] bytes = InstructionCode.longToBytes(aLong);
            dataInBytes.add(ArrayUtils.toObject(bytes));
        });
        return new ProgramAndDataDto(programInBytes, dataInBytes);
    }

    private void prepareCompilerDebug() {
        debugMessages.add("COMPILATION:\n");
        debugMessages.add("Compiled main:");
        mainProgram.forEach(ins -> debugMessages.add(ins.getKey().getMnemonic() + " " + ins.getValue()));
        debugMessages.add("Compiled functions:");
        functionsProgram.forEach(ins -> debugMessages.add(ins.getKey().getMnemonic() + " " + ins.getValue()));
        debugMessages.add("\nFunction table:");
        functionAddressTable.forEach((e, integer) -> debugMessages.add(e.getKey() + ", size:" + e.getValue() + ", address:" + integer));
        debugMessages.add("\nLiteral table:");
        literalAddressTable.forEach((aLong, integer) -> debugMessages.add(aLong + ", address:" + integer));
        debugMessages.add("\nVar table:");
        variableAddressTable.forEach((string, integer) -> debugMessages.add(string + ", address:" + integer));
    }

    private void prepareLinkerDebug() {
        debugMessages.add("\nLINKAGE:\n");
        debugMessages.add("\nFunction table:");
        functionAddressTable.forEach((e, integer) -> debugMessages.add(e.getKey() + ", size:" + e.getValue() + ", address:" + integer));
        debugMessages.add("\nLiteral table:");
        literalAddressTable.forEach((aLong, integer) -> debugMessages.add(aLong + ", address:" + integer));
        debugMessages.add("\nVar table:");
        variableAddressTable.forEach((string, integer) -> debugMessages.add(string + ", address:" + integer));
        debugMessages.add("\nProgram:");
        program.forEach(ins -> debugMessages.add(ins.getKey().getMnemonic() + " " + ins.getValue()));
    }

    private void calculatePositionInFile() {
        int partSum = 0;
        for (int i = 0; i < tokensInLine.size(); i++) {
            partSum = partSum + tokensInLine.get(i);
            if (partSum > tokenNumber) {
                lineNumber = i + 1;
                positionInLine = tokenNumber - (partSum - tokensInLine.get(i));
                break;
            }
        }
    }

    private void parseBase(final ListIterator<String> listIterator) {
        String next = listIterator.next();
        if (next.equals(":")) {
            listIterator.previous();
            isFunction = true;
            parseFunction(listIterator);
            isFunction = false;
            parseBase(listIterator);
        } else {
            listIterator.previous();
            isFunction = false;
            parseWords(listIterator);
        }
    }

    private void parseFunction(final ListIterator<String> listIterator) {
        listIterator.next(); //skip :
        tokenNumber++;
        String name = listIterator.next();
        tokenNumber++;
        if (!name.matches("([A-Z]|[a-z]|[0-9])+")) {
            calculatePositionInFile();
            throw new InvalidFunctionNameException("Invalid function name at " + (lineNumber) + ":" + (positionInLine));
        }
        ArrayList<String> funcTokens = new ArrayList<>();
        String t = listIterator.next();
        try {
            while (!t.equals(";")) {
                funcTokens.add(t);
                t = listIterator.next();
            }
        } catch (NoSuchElementException e) {
            calculatePositionInFile();
            throw new NoSuchElementException("Missing \";\" at " + (lineNumber) + ":" + (positionInLine));
        }
        int c = parseWords(funcTokens.listIterator()) + 1;
        tokenNumber++;
        functionsProgram.add(new AbstractMap.SimpleEntry<>(InstructionCode.EXIT, ""));
        functionAddressTable.put(new AbstractMap.SimpleEntry<>(name, c), 0);
    }

    private int parseWords(final ListIterator<String> listIterator) {
        if (listIterator.hasNext()) {
            String next = listIterator.next();
            int count = 0;
            if (next.equals("do")) {
                listIterator.previous();
                count = parseLoop(listIterator);
            } else if (next.equals("if")) {
                listIterator.previous();
                count = parseIf(listIterator);
            } else {
                listIterator.previous();
                count = parseWord(listIterator);
            }
            return parseWords(listIterator) + count;
        }
        return 0;
    }

    private int parseWord(final ListIterator<String> listIterator) {
        String word = listIterator.next();
        tokenNumber++;
        ZorthPrimitives primitive = ZorthPrimitives.byValue(word);
        ArrayList<AbstractMap.SimpleEntry<InstructionCode, String>> temp = new ArrayList<>();
        if (primitive != null) {
            switch (primitive) {
                case PLUS -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.ADD, ""));
                case SUB -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.SUB, ""));
                case MUL -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.MUL, ""));
                case DIV -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.DIV, ""));
                case AND -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.AND, ""));
                case OR -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.OR, ""));
                case NEG -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.NEG, ""));
                case XOR -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.XOR, ""));
                case NOT -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.NOT, ""));
                case EQ -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.EQ, ""));
                case GR -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.GR, ""));
                case LE -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.LE, ""));
                case SWAP -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.SWAP, ""));
                case DROP -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.DROP, ""));
                case DUP -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.DUP, ""));
                case OVER -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.OVER, ""));
                case ST -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.ST, ""));
                case FT -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.FT, ""));
                case DEC -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.DEC, ""));
                case INC -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.INC, ""));
                case NEQ -> temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.NEQ, ""));
                case DOT -> {
                    temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.ADDR, "0"));
                    temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.ST, ""));
                }
                case EMIT -> {
                    temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.ADDR, "2"));
                    temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.ST, ""));
                }
                case IN -> {
                    temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.ADDR, "1"));
                    temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.FT, ""));
                }
                default -> {
                }
            }
        } else {
            if (word.equals("variable")) {
                word = listIterator.next();
                tokenNumber++;
                String finalWord = word;
                if (variableAddressTable.containsKey(word)) {
                    calculatePositionInFile();
                    throw new InvalidVariableNameException("Variable \"" + word + "\" is already exists, at " + (lineNumber) + ":" + (positionInLine));
                } else if (functionAddressTable.keySet().stream().anyMatch(s -> s.getKey().equals(finalWord))) {
                    calculatePositionInFile();
                    throw new InvalidVariableNameException("Variable can't be created. Function \"" + word + "\" is already exists, "
                            + "at " + (lineNumber) + ":" + (positionInLine));
                } else {
                    variableAddressTable.put(word, 0);
                }
            } else if (word.matches("[0-9]+")) {
                literalAddressTable.put(Long.parseLong(word), 0);
                temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.LIT, "lit$" + word));
            } else if (word.matches("([A-Z]|[a-z]|[0-9])+")) {
                if (variableAddressTable.containsKey(word)) {
                    temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.ADDR, "var$" + word));
                } else {
                    String finalWord = word;
                    if (functionAddressTable.keySet().stream().anyMatch(s -> s.getKey().equals(finalWord))) {
                        temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.CALL, "fun$" + word));
                    } else {
                        calculatePositionInFile();
                        throw new IllegalArgumentException("Invalid var or func name: \"" + word + "\" at " + (lineNumber) + ":" + (positionInLine));
                    }
                }
            } else if (word.equals(".\"")) {
                StringBuilder str = new StringBuilder();
                word = listIterator.next();
                tokenNumber++;
                str.append(word);
                try {
                    while (!word.endsWith("\"")) {
                        word = listIterator.next();
                        str.append(" ");
                        str.append(word);
                    }
                } catch (NoSuchElementException e) {
                    calculatePositionInFile();
                    throw new InvalidStringException("Invalid string format at " + (lineNumber) + ":" + (positionInLine));
                }
                String s = str.substring(0, str.length() - 1);
                s.chars().forEach(value -> {
                    literalAddressTable.put((long) value, 0);
                    temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.LIT, "lit$" + (long) value));
                    temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.ADDR, "2"));
                    temp.add(new AbstractMap.SimpleEntry<>(InstructionCode.ST, ""));
                });
            } else {
                calculatePositionInFile();
                throw new UnknownWordException("Unknown word: \"" + word + "\" at " + (lineNumber) + ":" + (positionInLine));
            }
        }

        if (isFunction) {
            functionsProgram.addAll(temp);
        } else {
            mainProgram.addAll(temp);
        }
        return temp.size();
    }

    private int parseIf(final ListIterator<String> listIterator) {
        listIterator.next(); // skip "if"
        tokenNumber++;
        ArrayList<String> ifTokens = new ArrayList<>();
        String t = listIterator.next();
        long ifCount = 1;
        long endifCount = 0;
        try {
            while (ifCount != endifCount) {
                ifTokens.add(t);
                t = listIterator.next();
                if (t.equals("if")) {
                    ifCount++;
                }
                if (t.equals("endif")) {
                    endifCount++;
                }
            }
        } catch (NoSuchElementException e) {
            calculatePositionInFile();
            throw new NoSuchElementException("Missing \"endif\" for " + (lineNumber) + ":" + (positionInLine));
        }
        if (isFunction) {
            functionsProgram.add(new AbstractMap.SimpleEntry<>(InstructionCode.IF, "if$"));
        } else {
            mainProgram.add(new AbstractMap.SimpleEntry<>(InstructionCode.IF, "if$"));
        }
        int count = parseWords(ifTokens.listIterator());
        tokenNumber++;
        if (isFunction) {
            functionsProgram.set(functionsProgram.size() - count - 1, new AbstractMap.SimpleEntry<>(InstructionCode.IF, "if$" + (count + 1)));
        } else {
            mainProgram.set(mainProgram.size() - count - 1, new AbstractMap.SimpleEntry<>(InstructionCode.IF, "if$" + (count + 1)));
        }
        return count + 1;
    }

    private int parseLoop(final ListIterator<String> listIterator) {
        listIterator.next(); // skip "do"
        tokenNumber++;
        ArrayList<String> loopTokens = new ArrayList<>();
        String t = listIterator.next();
        long doCount = 1;
        long loopCount = 0;
        try {
            while (loopCount != doCount) {
                loopTokens.add(t);
                t = listIterator.next();
                if (t.equals("loop")) {
                    loopCount++;
                }
                if (t.equals("do")) {
                    doCount++;
                }
            }
        } catch (NoSuchElementException e) {
            calculatePositionInFile();
            throw new NoSuchElementException("Missing \"loop\" for " + (lineNumber) + ":" + (positionInLine));
        }
        int count = parseWords(loopTokens.listIterator());
        tokenNumber++;
        if (isFunction) {
            functionsProgram.add(new AbstractMap.SimpleEntry<>(InstructionCode.LOOP, "loop$" + count));
        } else {
            mainProgram.add(new AbstractMap.SimpleEntry<>(InstructionCode.LOOP, "loop$" + count));
        }
        return count + 1;
    }

    public List<String> getDebugMessages() {
        return Collections.unmodifiableList(debugMessages);
    }

    public List<Long> getData() {
        return Collections.unmodifiableList(data);
    }

    public List<AbstractMap.SimpleEntry<InstructionCode, String>> getFunctionsProgram() {
        return Collections.unmodifiableList(functionsProgram);
    }

    public List<AbstractMap.SimpleEntry<InstructionCode, String>> getMainProgram() {
        return Collections.unmodifiableList(mainProgram);
    }

    public List<AbstractMap.SimpleEntry<InstructionCode, String>> getProgram() {
        return Collections.unmodifiableList(program);
    }

    public Map<Long, Integer> getLiteralAddressTable() {
        return Collections.unmodifiableMap(literalAddressTable);
    }

    public Map<String, Integer> getVariableAddressTable() {
        return Collections.unmodifiableMap(variableAddressTable);
    }

    public Map<AbstractMap.SimpleEntry<String, Integer>, Integer> getFunctionAddressTable() {
        return Collections.unmodifiableMap(functionAddressTable);
    }
}
