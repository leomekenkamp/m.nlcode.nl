package nl.nlcode.m.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javafx.util.converter.IntegerStringConverter;

/**
 * Implementing classes are based on https://en.wikipedia.org/wiki/Musical_note.
 * 
 * @author leo
 */
public abstract class BaseNoteStringConverter extends IntegerStringConverter {

    protected static final String SEPARATOR = "/";

    protected abstract String noteDescSharp(int semitoneRelIndex);

    protected abstract String noteDescFlat(int semitoneRelIndex);

    protected static Map<String, Integer> descriptionToNoteOffset = new HashMap<>();

    protected static final int SEMITONES_IN_OCTAVE = 12;

    protected static String[] FLAT_MODIFIERS = new String[]{"-", "@"};
    protected static String[] SHARP_MODIFIERS = new String[]{"+", "#"};

    private static final int NOTE_C0 = 12;
    
    @Override
    public String toString(Integer note) {
        if (note < 0 || note > 127) {
            throw new IllegalArgumentException("invalid <" + note + ">");
        }
        int noteOffset = note % SEMITONES_IN_OCTAVE;
        StringBuilder result = new StringBuilder();
        if (note >= NOTE_C0) {
            String noteSharp = noteDescSharp(noteOffset);
            String noteFlat = noteDescFlat(noteOffset);
            result.append(noteDescSharp(noteOffset));
            result.append((note / 12) - 1);
            if (!noteSharp.equals(noteFlat)) {
                result.append(SEPARATOR);
                result.append(noteFlat);
                result.append((note / 12) - 1);
            }
        }
        return result.toString();
    }

    @Override
    public Integer fromString(String desc) {
        desc = desc.trim();
        if (desc.split("\\" + SEPARATOR).length > 2) {
            throw new IllegalArgumentException("");
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
        String noteWithoutOctave = desc.substring(0, desc.length() - 1).toUpperCase(Locale.UK);
        Integer noteOffset = descriptionToNoteOffset.get(noteWithoutOctave);
        if (noteOffset == null) {
            throw new IllegalArgumentException("not found: " + noteWithoutOctave);
        }
        int result = (octave + 1) * 12 + noteOffset;
        if (result < 0 || result > 127) {
            throw new IllegalArgumentException();
        }
        return result;
    }

}
