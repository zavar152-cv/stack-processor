package ru.itmo.zavar.zorth;

import lombok.RequiredArgsConstructor;
import ru.itmo.zavar.InstructionCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@RequiredArgsConstructor
public class ZorthCompiler {
    private final Path inputFilePath;
    private final Path outputFilePath;
    private final boolean isBinary;

    private final HashMap<AbstractMap.SimpleEntry<String, Long>, Integer> functionAddressTable = new HashMap<>(); // (name, length), address
    private final HashMap<String, Integer> variableAddressTable = new HashMap<>(); // name, address
    private final HashMap<Long, Integer> literalAddressTable = new HashMap<>(); // value, address
    private final ArrayList<AbstractMap.SimpleEntry<InstructionCode, String>> functionsProgram = new ArrayList<>();
    private final ArrayList<AbstractMap.SimpleEntry<InstructionCode, String>> mainProgram = new ArrayList<>();
    private final ArrayList<Long> data = new ArrayList<>();
    private boolean isFunction = false;

    public void compile() {
        ArrayList<String> tokens = new ArrayList<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(inputFilePath)) {
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                tokens.addAll(Arrays.stream(line.split(" ")).toList());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //tokens.forEach(System.out::println);
        ListIterator<String> stringListIterator = tokens.listIterator();
        parseBase(stringListIterator);
        mainProgram.add(new AbstractMap.SimpleEntry<>(InstructionCode.HALT, ""));

        System.out.println("Parsed:");
        mainProgram.forEach(ins -> System.out.println(ins.getKey() + " " + ins.getValue()));
        functionsProgram.forEach(ins -> System.out.println(ins.getKey() + " " + ins.getValue()));
        System.out.println("\nFunction table:");
        functionAddressTable.forEach((e, integer) -> {
            System.out.println(e.getKey() + ", size:" + e.getValue() + ", address:" + integer);
        });
        System.out.println("\nLiteral table:");
        literalAddressTable.forEach((aLong, integer) -> {
            System.out.println(aLong + ", address:" + integer);
        });
        System.out.println("\nVar table:");
        variableAddressTable.forEach((string, integer) -> {
            System.out.println(string + ", address:" + integer);
        });
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

    // TODO errors
    private void parseFunction(final ListIterator<String> listIterator) {
        try {
            listIterator.next(); //skip :
            String name = listIterator.next();
            if (!name.matches("([A-Z]|[a-z]|[0-9])+")) {
                throw new IllegalArgumentException("Invalid function name at " + (listIterator.previousIndex() + 1));
            }
            ArrayList<String> funcTokens = new ArrayList<>();
            String t = listIterator.next();
            try {
                while (!t.equals(";")) {
                    funcTokens.add(t);
                    t = listIterator.next();
                }
            } catch (NoSuchElementException e) {
                throw new NoSuchElementException("Missing \";\" at " + (listIterator.previousIndex() + 1));
            }
            long c = parseWords(funcTokens.listIterator()) + 1;
            functionsProgram.add(new AbstractMap.SimpleEntry<>(InstructionCode.EXIT, ""));
            functionAddressTable.put(new AbstractMap.SimpleEntry<>(name, c), 0);
        } catch (Exception e) {
            throw new IllegalArgumentException((e.getMessage() != null ? e.getMessage() : ""));
        }
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
                String finalWord = word;
                if (variableAddressTable.containsKey(word)) {
                    throw new IllegalArgumentException("Variable \"" + word + "\" is already exists, at " + (listIterator.previousIndex() + 1));
                } else if (functionAddressTable.keySet().stream().anyMatch(s -> s.getKey().equals(finalWord))) {
                    throw new IllegalArgumentException("Variable can't be created. Function \"" + word + "\" is already exists, "
                            + "at " + (listIterator.previousIndex() + 1));
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
                        throw new IllegalArgumentException("Invalid var or func name: \"" + word + "\" at " + (listIterator.previousIndex() + 1));
                    }
                }
            } else if (word.equals(".\"")) {
                //TODO string
                System.out.println("TODO");
            } else {
                throw new IllegalArgumentException("Unknown word: \"" + word + "\" at " + (listIterator.previousIndex() + 1));
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
        return 0;
    }

    private int parseLoop(final ListIterator<String> listIterator) {
        listIterator.next(); // skip "do"
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
            throw new NoSuchElementException("Missing \"loop\" at " + (listIterator.previousIndex() + 1)); //TODO fix position
        }
        int count = parseWords(loopTokens.listIterator());
        if (isFunction) {
            functionsProgram.add(new AbstractMap.SimpleEntry<>(InstructionCode.LOOP, "loop$" + count));
        } else {
            mainProgram.add(new AbstractMap.SimpleEntry<>(InstructionCode.LOOP, "loop$" + count));
        }
        return count + 1;
    }
}
