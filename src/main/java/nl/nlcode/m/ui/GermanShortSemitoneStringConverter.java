package nl.nlcode.m.ui;

import javafx.util.StringConverter;
import static nl.nlcode.m.ui.BaseNoteStringConverter.FLAT_MODIFIERS;
import static nl.nlcode.m.ui.BaseNoteStringConverter.SHARP_MODIFIERS;
import static nl.nlcode.m.ui.BaseNoteStringConverter.descriptionToNoteOffset;

/**
 *
 * @author leo
 */
public class GermanShortSemitoneStringConverter extends StringConverter {

    private final static String[] NOTE_DESC_SHARP = new String[]{
        "C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "H"
    };

    private final static String[] NOTE_DESC_FLAT = new String[]{
        "C", "D♭", "D", "E♭", "E", "F", "G♭", "G", "A♭", "A", "B", "H"
    };

    private final static boolean[] HAS_SHARP_MODIFIER = new boolean[NOTE_DESC_FLAT.length];
    private final static boolean[] HAS_FLAT_MODIFIER = new boolean[NOTE_DESC_FLAT.length];

    static {
        for (int i = 0; i < NOTE_DESC_SHARP.length; i++) {
            HAS_SHARP_MODIFIER[i] = !NOTE_DESC_SHARP[i].equals(NOTE_DESC_FLAT[i]);
            HAS_FLAT_MODIFIER[i] = !NOTE_DESC_FLAT[i].equals("B") && !NOTE_DESC_SHARP[i].equals(NOTE_DESC_FLAT[i]);
        }
    }

    public GermanShortSemitoneStringConverter() {
        for (int i = 0; i < NOTE_DESC_SHARP.length; i++) {
            descriptionToNoteOffset.put(NOTE_DESC_SHARP[i], i);
            descriptionToNoteOffset.put(NOTE_DESC_FLAT[i], i);

            if (HAS_SHARP_MODIFIER[i]) {
                for (String sharpModifier : SHARP_MODIFIERS) {
                    descriptionToNoteOffset.put(NOTE_DESC_SHARP[i].charAt(0) + sharpModifier, i);
                    descriptionToNoteOffset.put(NOTE_DESC_SHARP[i].charAt(0) + "is", i);
                }
            }
            if (HAS_FLAT_MODIFIER[i]) {
                for (String flatModifier : FLAT_MODIFIERS) {
                    descriptionToNoteOffset.put(NOTE_DESC_FLAT[i].charAt(0) + flatModifier, i);
                }
            }
            descriptionToNoteOffset.put("DES", 1);
            descriptionToNoteOffset.put("ES", 3);
            descriptionToNoteOffset.put("GES", 6);
            descriptionToNoteOffset.put("AS", 8);
        }
    }

    //@Override
    protected String noteDescSharp(int semitoneRelIndex) {
        return NOTE_DESC_SHARP[semitoneRelIndex];
    }

    //@Override
    protected String noteDescFlat(int semitoneRelIndex) {
        return NOTE_DESC_FLAT[semitoneRelIndex];
    }

    @Override
    public String toString(Object t) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object fromString(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
