package nl.nlcode.m.ui;

/**
 *
 * @author leo
 */
public class RomanceShortNoteStringConverter extends BaseNoteStringConverter {

    private final static String[] NOTE_DESC_SHARP = new String[]{
        "do", "do♯", "re", "re♯", "mi", "fa", "fa♯", "sol", "sol♯", "la", "la♯", "si"
    };

    private final static String[] NOTE_DESC_FLAT = new String[]{
        "do", "re♭", "re", "mi♭", "mi", "fa", "sol♭", "sol", "la♭", "la", "si♭", "si"
    };

    private final static boolean[] HAS_SHARP_MODIFIER = new boolean[NOTE_DESC_FLAT.length];
    private final static boolean[] HAS_FLAT_MODIFIER = new boolean[NOTE_DESC_FLAT.length];

    static {
        for (int i = 0; i < NOTE_DESC_SHARP.length; i++) {
            HAS_SHARP_MODIFIER[i] = !NOTE_DESC_SHARP[i].equals(NOTE_DESC_FLAT[i]);
            HAS_FLAT_MODIFIER[i] = !NOTE_DESC_SHARP[i].equals(NOTE_DESC_FLAT[i]);
        }
    }

    public RomanceShortNoteStringConverter() {
        for (int i = 0; i < NOTE_DESC_SHARP.length; i++) {
            descriptionToNoteOffset.put(NOTE_DESC_SHARP[i].toUpperCase(), i);
            descriptionToNoteOffset.put(NOTE_DESC_FLAT[i].toUpperCase(), i);

            if (HAS_SHARP_MODIFIER[i]) {
                for (String sharpModifier : SHARP_MODIFIERS) {
                    descriptionToNoteOffset.put(NOTE_DESC_SHARP[i].substring(0, NOTE_DESC_SHARP[i].length() - 1).toUpperCase() + sharpModifier, i);
                }
            }
            if (HAS_FLAT_MODIFIER[i]) {
                for (String flatModifier : FLAT_MODIFIERS) {
                    descriptionToNoteOffset.put(NOTE_DESC_FLAT[i].substring(0, NOTE_DESC_FLAT[i].length() - 1).toUpperCase() + flatModifier, i);
                }
            }
        }
    }

    @Override
    protected String noteDescSharp(int semitoneRelIndex) {
        return NOTE_DESC_SHARP[semitoneRelIndex];
    }

    @Override
    protected String noteDescFlat(int semitoneRelIndex) {
        return NOTE_DESC_FLAT[semitoneRelIndex];
    }
}
