package nl.nlcode.m.ui;

/**
 *
 * @author leo
 */
public class EnglishShortNoteStringConverter extends BaseNoteStringConverter {

    private final static String[] NOTE_DESC_SHARP = new String[]{
        "C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"
    };

    private final static String[] NOTE_DESC_FLAT = new String[]{
        "C", "D♭", "D", "E♭", "E", "F", "G♭", "G", "A♭", "A", "B♭", "B"
    };

    public EnglishShortNoteStringConverter() {
        for (int i = 0; i < NOTE_DESC_SHARP.length; i++) {
            descriptionToNoteOffset.put(NOTE_DESC_SHARP[i], i);
            descriptionToNoteOffset.put(NOTE_DESC_FLAT[i], i);

            if (!NOTE_DESC_SHARP[i].equals(NOTE_DESC_FLAT[i])) {
                for (String sharpModifier : SHARP_MODIFIERS) {
                    descriptionToNoteOffset.put(NOTE_DESC_SHARP[i].charAt(0) + sharpModifier, i);
                }
                for (String flatModifier : FLAT_MODIFIERS) {
                    descriptionToNoteOffset.put(NOTE_DESC_FLAT[i].charAt(0) + flatModifier, i);
                }
                descriptionToNoteOffset.put(NOTE_DESC_SHARP[i].charAt(0) + "SHARP", i);
                descriptionToNoteOffset.put(NOTE_DESC_FLAT[i].charAt(0) + "FLAT", i);
                descriptionToNoteOffset.put(NOTE_DESC_SHARP[i].charAt(0) + " SHARP ", i);
                descriptionToNoteOffset.put(NOTE_DESC_FLAT[i].charAt(0) + " FLAT ", i);
                descriptionToNoteOffset.put(NOTE_DESC_SHARP[i].charAt(0) + "S", i);
                descriptionToNoteOffset.put(NOTE_DESC_FLAT[i].charAt(0) + "F", i);
                descriptionToNoteOffset.put(NOTE_DESC_SHARP[i].charAt(0) + "♯", i);
                descriptionToNoteOffset.put(NOTE_DESC_FLAT[i].charAt(0) + "♭", i);
                
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
