package nl.nlcode.musictheory;

/**
 *
 * @author leo
 */
public enum Interval {

    PERFECT_UNISON(0, "P1"),
    MINOR_SECOND(1, "m2"),
    MAJOR_SECOND(2, "M2"),
    MINOR_THIRD(3, "m3"),
    MAJOR_THIRD(4, "M4"),
    PERFECT_FOURTH(5,"P4"),
    TRITONE(6, "TT"),
    PERFECT_FIFTH(7, "P5"),
    MINOR_SIXTH(8, "m6"),
    MAJOR_SIXTH(9, "M6"),
    MINOR_SEVENTH(10, "m7"),
    MAJOR_SEVENTH(11, "M7"),
    PERFECT_OCTAVE(12, "P8"),
    ;

    private int semitones;
    private String shortName;

    private Interval(int semitones, String shortName) {
        this.semitones = semitones;
        this.shortName = shortName;
    }
    
    public int semitones() {
        return semitones;
    }
    
    public String shortName() {
        return shortName;
    }
}
