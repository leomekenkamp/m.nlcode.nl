package nl.nlcode.m.engine;

import java.util.HashMap;
import java.util.Map;
import javafx.util.StringConverter;

/**
 *
 * @author leo
 */
public class EnglishNoteStringConverter extends StringConverter<Integer> {

    private static final String SEPARATOR = "/";
    
    private final static String[] NOTE_DESC_SHARP = new String[]{
        "C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"
    };

    private final static String[] NOTE_DESC_FLAT = new String[]{
        "C", "D♭", "D", "E♭", "E", "F", "G♭", "G", "A♭", "A", "B♭", "B"
    };

    private final static boolean[] SHARP_OR_FLAT = new boolean[NOTE_DESC_FLAT.length];

    static {
        for (int i = 0; i < NOTE_DESC_SHARP.length; i++) {
            SHARP_OR_FLAT[i] = !NOTE_DESC_SHARP[i].equals(NOTE_DESC_FLAT[i]);
        }
    }

    private static Map<String, Integer> DESC_TO_NOTE_OFFSET = new HashMap<>();

    static {
        for (int i = 0; i < NOTE_DESC_SHARP.length; i++) {
            DESC_TO_NOTE_OFFSET.put(NOTE_DESC_SHARP[i], i);
            DESC_TO_NOTE_OFFSET.put(NOTE_DESC_FLAT[i], i);

            if (SHARP_OR_FLAT[i]) {
                DESC_TO_NOTE_OFFSET.put(NOTE_DESC_SHARP[i].charAt(0) + "+", i);
                DESC_TO_NOTE_OFFSET.put(NOTE_DESC_SHARP[i].charAt(0) + "#", i);
                DESC_TO_NOTE_OFFSET.put(NOTE_DESC_FLAT[i].charAt(0) + "-", i);
                DESC_TO_NOTE_OFFSET.put(NOTE_DESC_FLAT[i].charAt(0) + "@", i);
            }
        }
    }

    @Override
    public String toString(Integer note) {
        if (note < 0 || note > 127) {
            throw new IllegalArgumentException("invalid <" + note + ">");
        }
        int noteOffset = note % NOTE_DESC_SHARP.length;
        StringBuilder result = new StringBuilder();
        if (note > 20) {
            result.append(NOTE_DESC_SHARP[noteOffset]);
            result.append((note / 12) - 1);
            if (SHARP_OR_FLAT[noteOffset]) {
                result.append(SEPARATOR);
                result.append(NOTE_DESC_FLAT[noteOffset]);
                result.append((note / 12) - 1);
            }
        }
        return result.toString();
    }

    @Override
    public Integer fromString(String desc) {
        desc = desc.trim();
        if (desc.length() < 2) {
            throw new IllegalArgumentException();
        }
        int separatorIndex = desc.indexOf(SEPARATOR);
        if (separatorIndex != -1) {
            try {
                return fromString(desc.substring(0, separatorIndex));
            } catch (IllegalArgumentException e) {
                return fromString(desc.substring(separatorIndex + 1, desc.length()));
            }
        }
        
        int octave = Integer.parseInt(desc.substring(desc.length() - 1));
        String noteWithoutOctave = desc.substring(0, desc.length() - 1).toUpperCase();
        Integer noteOffset = DESC_TO_NOTE_OFFSET.get(noteWithoutOctave);
        if (noteOffset == null) {
            throw new IllegalArgumentException();
        }
        int result = (octave + 1) * 12 + noteOffset;
        if (result < 0 || result > 127) {
            throw new IllegalArgumentException();
        }
        return result;
    }

}
