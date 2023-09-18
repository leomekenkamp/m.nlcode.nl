package nl.nlcode.musictheory;

import java.util.HashMap;
import java.util.Map;
import static nl.nlcode.musictheory.ChromaticNote.Accidental.FLAT;
import static nl.nlcode.musictheory.ChromaticNote.Accidental.NATURAL;
import static nl.nlcode.musictheory.ChromaticNote.Accidental.SHARP;

/**
 *
 * @author leo
 */
public enum ChromaticNote {
    C(Accidental.NATURAL, 0),
    C_SHARP(Accidental.SHARP, 1),
    D_FLAT(Accidental.FLAT, 1),
    D(Accidental.NATURAL, 2),
    D_SHARP(Accidental.SHARP, 3),
    E_FLAT(Accidental.FLAT, 3),
    E(Accidental.NATURAL, 4),
    F(Accidental.NATURAL, 5),
    F_SHARP(Accidental.SHARP, 6),
    G_FLAT(Accidental.FLAT, 6),
    G(Accidental.NATURAL, 7),
    G_SHARP(Accidental.SHARP, 8),
    A_FLAT(Accidental.FLAT, 8),
    A(Accidental.NATURAL, 9),
    A_SHARP(Accidental.SHARP, 10),
    B_FLAT(Accidental.FLAT, 10),
    B(Accidental.NATURAL, 11),;

    public static enum Accidental {
        FLAT,
        NATURAL,
        SHARP,
    }

    private int semitonesFromC;
    private Accidental accidental;

    private static final Map<Integer, ChromaticNote> SEMITONES_FROM_C_TO_CHROMATIC_NOTE_PREF_SHARP = new HashMap<>();
    private static final Map<Integer, ChromaticNote> SEMITONES_FROM_C_TO_CHROMATIC_NOTE_PREF_FLAT = new HashMap<>();

    private ChromaticNote(Accidental accidental, int semitonesFromC, ChromaticNote base) {

    }

    static int euclidianModulo(int num, int mod) {
        int result = num % mod;
        return result < 0 ? result + mod : result;
    }
    
    public ChromaticNote getSharp() {
        int offset = isSharp() ? 2 : 1;
        return ChromaticNote.values()[euclidianModulo(ordinal() + offset, values().length)];
    }

    public ChromaticNote getFlat() {
        int offset = isFlat() ? 2 : 1;
        return ChromaticNote.values()[euclidianModulo(ordinal() - offset, values().length)];
    }

    private ChromaticNote(Accidental accidental, int semitonesFromC) {
        this.accidental = accidental;
        this.semitonesFromC = semitonesFromC;
    }

    static {
        for (ChromaticNote note : ChromaticNote.values()) {
            if (note.getAccidental() != Accidental.FLAT) {
                SEMITONES_FROM_C_TO_CHROMATIC_NOTE_PREF_SHARP.put(note.getSemitonesFromC(), note);
            }
            if (note.getAccidental() != Accidental.SHARP) {
                SEMITONES_FROM_C_TO_CHROMATIC_NOTE_PREF_FLAT.put(note.getSemitonesFromC(), note);
            }
        }
    }

    public Accidental getAccidental() {
        return accidental;
    }

    public boolean isNatural() {
        return accidental == NATURAL;
    }

    public boolean isFlat() {
        return accidental == FLAT;
    }

    public boolean isSharp() {
        return accidental == SHARP;
    }

    public ChromaticNote getNatural() {
        return switch (getAccidental()) {
            case NATURAL ->
                this;
            case FLAT ->
                ChromaticNote.values()[this.ordinal() + 1];
            case SHARP ->
                ChromaticNote.values()[this.ordinal() - 1];
        };
    }

    public int getSemitonesFromC() {
        return semitonesFromC;
    }

    public static ChromaticNote fromSemitoneFromCPreferSharp(int fromC) {
        checkSemitone(fromC);
        return SEMITONES_FROM_C_TO_CHROMATIC_NOTE_PREF_SHARP.get(fromC);
    }

    public static ChromaticNote fromSemitoneFromCPreferFlat(int fromC) {
        checkSemitone(fromC);
        return SEMITONES_FROM_C_TO_CHROMATIC_NOTE_PREF_FLAT.get(fromC);
    }

    protected static void checkSemitone(int s) {
        if (s < 0 || s > 11) {
            throw new IllegalArgumentException("fromC must be in interval [0, 11]");
        }
    }
}
