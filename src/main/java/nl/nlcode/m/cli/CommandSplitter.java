package nl.nlcode.m.cli;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author jq59bu
 */
public class CommandSplitter {

    private enum CommandSplitterState {
        BEGINNING_CHUNK, PARSING_WORD, PARSING_QUOTE, ESCAPE_CHAR
    }

    /**
     * Shamelessly copied from https://stackoverflow.com/questions/3366281/tokenizing-a-string-but-ignoring-delimiters-within-quotes
     * 
     * Splits a command on whitespaces. Preserves whitespace in quotes. Trims
     * excess whitespace between chunks. Supports quote escape within quotes.
     * Failed escape will preserve escape char.
     *
     * @return List of split commands
     */
    public static List<String> splitCommand(String inputString) {
        List<String> matchList = new LinkedList<>();
        LinkedList<Character> charList = inputString.chars()
                .mapToObj(i -> (char) i)
                .collect(Collectors.toCollection(LinkedList::new));

        // Finite-State Automaton for parsing.
        CommandSplitterState state = CommandSplitterState.BEGINNING_CHUNK;
        LinkedList<Character> chunkBuffer = new LinkedList<>();

        for (Character currentChar : charList) {
            switch (state) {
                case BEGINNING_CHUNK:
                    switch (currentChar) {
                        case '"':
                            state = CommandSplitterState.PARSING_QUOTE;
                            break;
                        case ' ':
                            break;
                        default:
                            state = CommandSplitterState.PARSING_WORD;
                            chunkBuffer.add(currentChar);
                    }
                    break;
                case PARSING_WORD:
                    switch (currentChar) {
                        case ' ':
                            state = CommandSplitterState.BEGINNING_CHUNK;
                            String newWord = chunkBuffer.stream().map(Object::toString).collect(Collectors.joining());
                            matchList.add(newWord);
                            chunkBuffer = new LinkedList<>();
                            break;
                        default:
                            chunkBuffer.add(currentChar);
                    }
                    break;
                case PARSING_QUOTE:
                    switch (currentChar) {
                        case '"':
                            state = CommandSplitterState.BEGINNING_CHUNK;
                            String newWord = chunkBuffer.stream().map(Object::toString).collect(Collectors.joining());
                            matchList.add(newWord);
                            chunkBuffer = new LinkedList<>();
                            break;
                        case '\\':
                            state = CommandSplitterState.ESCAPE_CHAR;
                            break;
                        default:
                            chunkBuffer.add(currentChar);
                    }
                    break;
                case ESCAPE_CHAR:
                    switch (currentChar) {
                        case '"': // Intentional fall through
                        case '\\':
                            state = CommandSplitterState.PARSING_QUOTE;
                            chunkBuffer.add(currentChar);
                            break;
                        default:
                            state = CommandSplitterState.PARSING_QUOTE;
                            chunkBuffer.add('\\');
                            chunkBuffer.add(currentChar);
                    }
            }
        }

        if (state != CommandSplitterState.BEGINNING_CHUNK) {
            String newWord = chunkBuffer.stream().map(Object::toString).collect(Collectors.joining());
            matchList.add(newWord);
        }
        return matchList;
    }

}
