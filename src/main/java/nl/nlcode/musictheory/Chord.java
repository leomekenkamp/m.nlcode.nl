package nl.nlcode.musictheory;

import java.util.Set;

/**
 *
 * @author leo
 */
public class Chord {
    
    public Set<Interval> MINOR_TRIAD = Set.of(Interval.PERFECT_UNISON, Interval.MINOR_THIRD, Interval.PERFECT_FIFTH);
    public Set<Interval> MAJOR_TRIAD = Set.of(Interval.PERFECT_UNISON, Interval.MAJOR_THIRD, Interval.PERFECT_FIFTH);
    public Set<Interval> TRIAD_SUS2 = Set.of(Interval.PERFECT_UNISON, Interval.MAJOR_SECOND, Interval.PERFECT_FIFTH);
    public Set<Interval> TRIAD_SUS4 = Set.of(Interval.PERFECT_UNISON, Interval.PERFECT_FOURTH, Interval.PERFECT_FIFTH);
    
    private ChromaticNote tonic;
    
    private Chord() {}
    
    public static Chord minorTriad(ChromaticNote note) {
        return null;
    }
}
