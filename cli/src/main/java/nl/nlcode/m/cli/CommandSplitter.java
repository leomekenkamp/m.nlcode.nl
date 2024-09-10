package nl.nlcode.m.cli;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author jq59bu
 */
public class CommandSplitter {

    private enum State {
        BEGINNING_CHUNK, 
        BEGINNING_CHUNK_ESCAPE, 
        PARSING_WORD, 
        PARSING_WORD_ESCAPE,
        PARSING_QUOTE,
        PARSING_QUOTE_ESCAPE,
        ESCAPE_CHAR
    }

    /**
     * Shamelessly copied from https://stackoverflow.com/questions/3366281/tokenizing-a-string-but-ignoring-delimiters-within-quotes
     * with some additions.
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
        State state = State.BEGINNING_CHUNK;
        LinkedList<Character> chunkBuffer = new LinkedList<>();

        for (Character currentChar : charList) {
            switch (state) {
                case BEGINNING_CHUNK:
                    switch (currentChar) {
                        case '"':
                            state = State.PARSING_QUOTE;
                            break;
                        case ' ':
                            break;
                        case '\\':
                            state = State.BEGINNING_CHUNK_ESCAPE;
                            break;
                        default:
                            state = State.PARSING_WORD;
                            chunkBuffer.add(currentChar);
                    }
                    break;
                case PARSING_WORD:
                    switch (currentChar) {
                        case ' ':
                            state = State.BEGINNING_CHUNK;
                            String newWord = chunkBuffer.stream().map(Object::toString).collect(Collectors.joining());
                            matchList.add(newWord);
                            chunkBuffer = new LinkedList<>();
                            break;
                        case '\\':
                            state = State.PARSING_WORD_ESCAPE;
                            break;
                        case '"':
                            state = State.PARSING_QUOTE;
                            matchList.add(chunkBuffer.stream().map(Object::toString).collect(Collectors.joining()));
                            chunkBuffer = new LinkedList<>();
                            break;
                        default:
                            chunkBuffer.add(currentChar);
                    }
                    break;
                case PARSING_QUOTE:
                    switch (currentChar) {
                        case '"':
                            state = State.BEGINNING_CHUNK;
                            String newWord = chunkBuffer.stream().map(Object::toString).collect(Collectors.joining());
                            matchList.add(newWord);
                            chunkBuffer = new LinkedList<>();
                            break;
                        case '\\':
                            state = State.PARSING_QUOTE_ESCAPE;
                            break;
                        default:
                            chunkBuffer.add(currentChar);
                    }
                    break;
                case PARSING_QUOTE_ESCAPE:
                    switch (currentChar) {
                        case '"': // Intentional fall through
                        case '\\':
                            state = State.PARSING_QUOTE;
                            chunkBuffer.add(currentChar);
                            break;
                        default:
                            state = State.PARSING_QUOTE;
                            chunkBuffer.add('\\');
                            chunkBuffer.add(currentChar);
                    }
                    break;
                case PARSING_WORD_ESCAPE: // fall through
                case BEGINNING_CHUNK_ESCAPE:
                    switch (currentChar) {
                        case '"': // Intentional fall through
                        case ' ': // Intentional fall through
                        case '\\': // Intentional fall through
                            state = State.PARSING_WORD;
                            chunkBuffer.add(currentChar);
                            break;
                        default:
                            state = State.PARSING_WORD;
                            chunkBuffer.add('\\');
                            chunkBuffer.add(currentChar);
                    }
            }
        }

        if (state != State.BEGINNING_CHUNK) {
            String newWord = chunkBuffer.stream().map(Object::toString).collect(Collectors.joining());
            matchList.add(newWord);
        }
        return matchList;
    }

}
