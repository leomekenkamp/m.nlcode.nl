package nl.nlcode.musictheory;

import java.util.Comparator;

/**
 * Basically the simplest names of the white keys on the piano keyboard without
 * any octave information.
 *
 * @author jq59bu
 */
public enum ChromaticNaturalNote {
    A(0),
    B(2),
    C(3),
    D(5),
    E(7),
    F(8),
    G(10);

    // FIXME: 12 is all over the source code; put in one place
    public static final int SEMITONES_PER_OCTAVE = 12;

    /**
     *
     */
    public static Comparator<ChromaticNaturalNote> ALPHABETICALLY = (o1, o2) -> {
        return o1.getSemitonesUpFromA() - o2.getSemitonesUpFromA();
    };

    private int distanceUpFromA;

    private ChromaticNaturalNote(int distanceUpFromA) {
        this.distanceUpFromA = distanceUpFromA;
    }

    /**
     * Always returns a positive number (or zero), being the minimum number of
     * semitones up to get to the the {
     *
     * @param other} chromatic note.
     *
     * @param other
     * @return
     */
    public int semitonesUpTo(ChromaticNaturalNote other) {
        return Math.floorMod(other.getSemitonesUpFromA() - getSemitonesUpFromA(), SEMITONES_PER_OCTAVE);
    }

    /**
     * Always returns a positive number (or zero), being the minimum number of
     * semitones down to get to the the {
     *
     * @param other} chromatic note.
     *
     * @param other
     * @return
     */
    public int semitonesDownTo(ChromaticNaturalNote other) {
        return Math.floorMod(getSemitonesUpFromA() - other.getSemitonesUpFromA(), SEMITONES_PER_OCTAVE);
    }

    /**
     * @return the distance from A to this note in semitones
     */
    int getSemitonesUpFromA() {
        return distanceUpFromA;
    }

}
