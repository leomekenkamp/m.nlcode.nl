package nl.nlcode.musictheory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import static nl.nlcode.musictheory.ChromaticNaturalNote.SEMITONES_PER_OCTAVE;
import static nl.nlcode.musictheory.ChromaticNote.Accidental.MAX_FLAT_AND_SHARP;
import static nl.nlcode.musictheory.ChromaticNote.AccidentalPreference.FLATTER;
import static nl.nlcode.musictheory.ChromaticNote.AccidentalPreference.SHARPER;

/**
 * Contains the information on a note without its octave. The concept of a
 * 'note' in music theory is a bit fuzzy. Since almost all scales are repeated
 * over octaves, the information on what octave a played note is in is often
 * left out. Hence this class.
 *
 * @author leo
 * @see {@link SpnNote#}
 */
public enum ChromaticNote {

    A_QUADRUPLE_FLAT(ChromaticNaturalNote.A, Accidental.QUADRUPLE_FLAT),
    A_TRIPLE_FLAT(ChromaticNaturalNote.A, Accidental.TRIPLE_FLAT),
    A_DOUBLE_FLAT(ChromaticNaturalNote.A, Accidental.DOUBLE_FLAT),
    A_FLAT(ChromaticNaturalNote.A, Accidental.FLAT),
    A(ChromaticNaturalNote.A, Accidental.NATURAL),
    A_SHARP(ChromaticNaturalNote.A, Accidental.SHARP),
    A_DOUBLE_SHARP(ChromaticNaturalNote.A, Accidental.DOUBLE_SHARP),
    A_TRIPLE_SHARP(ChromaticNaturalNote.A, Accidental.TRIPLE_SHARP),
    A_QUADRUPLE_SHARP(ChromaticNaturalNote.A, Accidental.QUADRUPLE_SHARP),
    //
    B_QUADRUPLE_FLAT(ChromaticNaturalNote.B, Accidental.QUADRUPLE_FLAT),
    B_TRIPLE_FLAT(ChromaticNaturalNote.B, Accidental.TRIPLE_FLAT),
    B_DOUBLE_FLAT(ChromaticNaturalNote.B, Accidental.DOUBLE_FLAT),
    B_FLAT(ChromaticNaturalNote.B, Accidental.FLAT),
    B(ChromaticNaturalNote.B, Accidental.NATURAL),
    B_SHARP(ChromaticNaturalNote.B, Accidental.SHARP),
    B_DOUBLE_SHARP(ChromaticNaturalNote.B, Accidental.DOUBLE_SHARP),
    B_TRIPLE_SHARP(ChromaticNaturalNote.B, Accidental.TRIPLE_SHARP),
    B_QUADRUPLE_SHARP(ChromaticNaturalNote.B, Accidental.QUADRUPLE_SHARP),
    //
    C_QUADRUPLE_FLAT(ChromaticNaturalNote.C, Accidental.QUADRUPLE_FLAT),
    C_TRIPLE_FLAT(ChromaticNaturalNote.C, Accidental.TRIPLE_FLAT),
    C_DOUBLE_FLAT(ChromaticNaturalNote.C, Accidental.DOUBLE_FLAT),
    C_FLAT(ChromaticNaturalNote.C, Accidental.FLAT),
    C(ChromaticNaturalNote.C, Accidental.NATURAL),
    C_SHARP(ChromaticNaturalNote.C, Accidental.SHARP),
    C_DOUBLE_SHARP(ChromaticNaturalNote.C, Accidental.DOUBLE_SHARP),
    C_TRIPLE_SHARP(ChromaticNaturalNote.C, Accidental.TRIPLE_SHARP),
    C_QUADRUPLE_SHARP(ChromaticNaturalNote.C, Accidental.QUADRUPLE_SHARP),
    //
    D_QUADRUPLE_FLAT(ChromaticNaturalNote.D, Accidental.QUADRUPLE_FLAT),
    D_TRIPLE_FLAT(ChromaticNaturalNote.D, Accidental.TRIPLE_FLAT),
    D_DOUBLE_FLAT(ChromaticNaturalNote.D, Accidental.DOUBLE_FLAT),
    D_FLAT(ChromaticNaturalNote.D, Accidental.FLAT),
    D(ChromaticNaturalNote.D, Accidental.NATURAL),
    D_SHARP(ChromaticNaturalNote.D, Accidental.SHARP),
    D_DOUBLE_SHARP(ChromaticNaturalNote.D, Accidental.DOUBLE_SHARP),
    D_TRIPLE_SHARP(ChromaticNaturalNote.D, Accidental.TRIPLE_SHARP),
    D_QUADRUPLE_SHARP(ChromaticNaturalNote.D, Accidental.QUADRUPLE_SHARP),
    //
    E_QUADRUPLE_FLAT(ChromaticNaturalNote.E, Accidental.QUADRUPLE_FLAT),
    E_TRIPLE_FLAT(ChromaticNaturalNote.E, Accidental.TRIPLE_FLAT),
    E_DOUBLE_FLAT(ChromaticNaturalNote.E, Accidental.DOUBLE_FLAT),
    E_FLAT(ChromaticNaturalNote.E, Accidental.FLAT),
    E(ChromaticNaturalNote.E, Accidental.NATURAL),
    E_SHARP(ChromaticNaturalNote.E, Accidental.SHARP),
    E_DOUBLE_SHARP(ChromaticNaturalNote.E, Accidental.DOUBLE_SHARP),
    E_TRIPLE_SHARP(ChromaticNaturalNote.E, Accidental.TRIPLE_SHARP),
    E_QUADRUPLE_SHARP(ChromaticNaturalNote.E, Accidental.QUADRUPLE_SHARP),
    // 
    F_QUADRUPLE_FLAT(ChromaticNaturalNote.F, Accidental.QUADRUPLE_FLAT),
    F_TRIPLE_FLAT(ChromaticNaturalNote.F, Accidental.TRIPLE_FLAT),
    F_DOUBLE_FLAT(ChromaticNaturalNote.F, Accidental.DOUBLE_FLAT),
    F_FLAT(ChromaticNaturalNote.F, Accidental.FLAT),
    F(ChromaticNaturalNote.F, Accidental.NATURAL),
    F_SHARP(ChromaticNaturalNote.F, Accidental.SHARP),
    F_DOUBLE_SHARP(ChromaticNaturalNote.F, Accidental.DOUBLE_SHARP),
    F_TRIPLE_SHARP(ChromaticNaturalNote.F, Accidental.TRIPLE_SHARP),
    F_QUADRUPLE_SHARP(ChromaticNaturalNote.F, Accidental.QUADRUPLE_SHARP),
    //
    G_QUADRUPLE_FLAT(ChromaticNaturalNote.G, Accidental.QUADRUPLE_FLAT),
    G_TRIPLE_FLAT(ChromaticNaturalNote.G, Accidental.TRIPLE_FLAT),
    G_DOUBLE_FLAT(ChromaticNaturalNote.G, Accidental.DOUBLE_FLAT),
    G_FLAT(ChromaticNaturalNote.G, Accidental.FLAT),
    G(ChromaticNaturalNote.G, Accidental.NATURAL),
    G_SHARP(ChromaticNaturalNote.G, Accidental.SHARP),
    G_DOUBLE_SHARP(ChromaticNaturalNote.G, Accidental.DOUBLE_SHARP),
    G_TRIPLE_SHARP(ChromaticNaturalNote.G, Accidental.TRIPLE_SHARP),
    G_QUADRUPLE_SHARP(ChromaticNaturalNote.G, Accidental.QUADRUPLE_SHARP);

    public static enum Accidental {
        QUADRUPLE_FLAT("𝄫𝄫", -4),
        TRIPLE_FLAT("♭𝄫", -3),
        DOUBLE_FLAT("𝄫", -2),
        FLAT("♭", -1),
        NATURAL("♮", 0),
        SHARP("♯", 1),
        DOUBLE_SHARP("𝄪", 2),
        TRIPLE_SHARP("♯𝄪", 3),
        QUADRUPLE_SHARP("𝄪𝄪", 4);
        // interesting link: https://www.compart.com/en/unicode/block/U+1D100// interesting link: https://www.compart.com/en/unicode/block/U+1D100

        public static Comparator<ChromaticNote.Accidental> BY_DISTANCE = (o1, o2) -> {
            return o1.semitonesFromNatural - o2.semitonesFromNatural;
        };

        private final String desc;
        private final int semitonesFromNatural;
        
        public static final int MAX_FLAT_AND_SHARP = 4;

        Accidental(String desc, int semitonesFromNatural) {
            this.desc = desc;
            this.semitonesFromNatural = semitonesFromNatural;
        }

        public String toString() {
            return desc;
        }

        public int getSemitonesFromNatural() {
            return semitonesFromNatural;
        }
        
        public boolean isQuadrupal() {
            return Math.abs(semitonesFromNatural) == MAX_FLAT_AND_SHARP;
        }
    }

    public static enum AccidentalPreference {
        FLATTER(-1),
        SHARPER(1);
        
        private int direction;
        
        AccidentalPreference(int direction) {
            this.direction = direction;
        }
        
        public int getDirection() {
            return direction;
        }
    }
    
    public static Comparator<ChromaticNote> BY_PITCH = (o1, o2) -> {
        return -o1.semitonesTo(o2);
    };

    public static Comparator<ChromaticNote> BY_PITCH_AND_ACCIDENTAL = (o1, o2) -> {
        int result = -o1.semitonesTo(o2) * 10;
        return result != 0 ? result : result - Accidental.BY_DISTANCE.compare(o1.getAccidental(), o2.getAccidental());
    };

    // INTERNAL USE ONLY - used ONLY in the FROM_A array to ensure G_QUADRUPLE_FLAT < A_DOUBLE_FLAT
    static Comparator<ChromaticNote> _BY_ACCIDENTAL_ONLY = (o1, o2) -> {
        if (o1.semitonesTo(o2) != 0) {
            throw new IllegalArgumentException("arguments should have equal 'pitch' (" + o1 + ", " + o2 + ")");
        }
        return -Accidental.BY_DISTANCE.compare(o1.getAccidental(), o2.getAccidental());
    };

    static ChromaticNote FROM_A[][];
    static int FROM_A_NATURAL_INDEX[];

    private final Accidental accidental;
    private ChromaticNaturalNote chromaticNaturalNote;

    private ChromaticNote(ChromaticNaturalNote chromaticNaturalNote, Accidental accidental) {
        this.accidental = accidental;
        this.chromaticNaturalNote = chromaticNaturalNote;
    }

    public Accidental getAccidental() {
        return accidental;
    }

    public ChromaticNaturalNote getChromaticNaturalNote() {
        return chromaticNaturalNote;
    }

    public ChromaticNote getNatural() {
        return ChromaticNote.values()[this.ordinal() - getAccidental().getSemitonesFromNatural()];
    }

    /**
     * Always returns a positive number (or zero), being the minimum number of
     * semitones up to get to the the {@code other} chromatic note.
     *
     * @param other} chromatic note.
     *
     * @param other
     * @return
     */
    public int semitonesUpTo(ChromaticNote other) {
        return getChromaticNaturalNote().semitonesUpTo(other.getChromaticNaturalNote())
                - getAccidental().getSemitonesFromNatural() + other.getAccidental().getSemitonesFromNatural();
    }

    /**
     * Always returns a positive number (or zero), being the minimum number of
     * semitones down to get to the the {@code other} chromatic note.
     *
     * @param other
     * @return
     */
    public int semitonesDownTo(ChromaticNote other) {
        return getChromaticNaturalNote().semitonesDownTo(other.getChromaticNaturalNote())
                + getAccidental().getSemitonesFromNatural() - other.getAccidental().getSemitonesFromNatural();
    }

    private int getSemitonesUpFromA() {
        return Math.floorMod(getChromaticNaturalNote().getSemitonesUpFromA() + getAccidental().getSemitonesFromNatural(), SEMITONES_PER_OCTAVE);
    }

    /**
     * Returns the (non-)shortest distance between this and the given note while
     * never going lower than A or higher than G. This means that the highest
     * (positive) result for this function is
     * {@code A.semitonesTo(G_SHARP) == 11} and the lowest (negative) result is
     * {@code G_SHARP.semitonesTo(A) == -11}. Note that the results for A_FLAT
     * and G are the same.
     *
     * @param other
     * @return difference in semitones: a negative number means going to a lower
     * note and a positive number means going to a higher tone. E.g.
     * {@code C.semitonesUpTo(D) == 2} and {@code D.semitonesTo(C) == -2}
     */
    int semitonesTo(ChromaticNote other) {
        return other.getSemitonesUpFromA() - getSemitonesUpFromA();
    }

    /**
     * 
     * @param semitonesFromA
     * @param preferredAccedental preferredAccedental accidental
     * @param upOrDown if no such accidental for {@code semitonesFromA}, go sharper or flatter on the returned match
     * @return 
     */
    static ChromaticNote fromSemitonesFromA(int semitonesFromA, Accidental preferredAccedental, AccidentalPreference preferredDirection) {
        if (FROM_A == null) {
            FROM_A = new ChromaticNote[SEMITONES_PER_OCTAVE][];
            for (int i = 0; i < FROM_A.length; i++) {
                FROM_A[i] = new ChromaticNote[Accidental.values().length];
            }
            for (ChromaticNote chromaticNote : ChromaticNote.values()) {
                FROM_A[chromaticNote.getSemitonesUpFromA()]
                        [Accidental.values().length - 1 - chromaticNote.getAccidental().getSemitonesFromNatural() - MAX_FLAT_AND_SHARP]
                        = chromaticNote;
            }
        }
        if (preferredAccedental == Accidental.QUADRUPLE_FLAT && preferredDirection == FLATTER) {
            throw new IllegalArgumentException("Cannot handle going any flatter than a quadruple flat");
        }
        if (preferredAccedental == Accidental.QUADRUPLE_SHARP && preferredDirection == SHARPER) {
            throw new IllegalArgumentException("Cannot handle going any sharper than a quadruple sharp");
        }
        int firstIndex = MAX_FLAT_AND_SHARP - preferredAccedental.getSemitonesFromNatural();
        ChromaticNote result = FROM_A[semitonesFromA][firstIndex];
        if (result == null) {
            result = FROM_A[semitonesFromA][firstIndex - preferredDirection.getDirection()];
        }
        return result;
    }

    /**
     * Returns a note {@code semitones} up, or down when {@code semitones} is
     * negative.
     *
     * @param semitones distance to returned value, up or down
     * @return
     */
    public ChromaticNote withSemitones(int semitones, AccidentalPreference preferredDirection) {
        checkSemitones(semitones);
        int semitonesFromA = Math.floorMod(getSemitonesUpFromA() + semitones, SEMITONES_PER_OCTAVE);
        return fromSemitonesFromA(semitonesFromA, getAccidental(), preferredDirection);
    }
    
    public ChromaticNote withSemitones(int semitones, Accidental accidental) {
        checkSemitones(semitones);
        int semitonesFromA = Math.floorMod(getSemitonesUpFromA() + semitones, SEMITONES_PER_OCTAVE);
        ChromaticNote result = fromSemitonesFromA(semitonesFromA, accidental, AccidentalPreference.FLATTER);
        if (result.getAccidental() != accidental) {
            throw new IllegalArgumentException("no such note exists");
        }
        return result;
    }

    public static void checkSemitones(int semitones) {
        if (semitones < 0 || semitones > 11) {
            throw new IllegalArgumentException("semitones should be in interval [0..11]");
        }
    }

    @Override
    public String toString() {
        final String letterOnly = name().substring(0, 1);;
        if (getAccidental() == Accidental.NATURAL) {
            return letterOnly;
        } else {
            return letterOnly + getAccidental();
        }
    }
}
