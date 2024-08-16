package nl.nlcode.musictheory;

import java.util.Set;

/**
 *
 * @author leo
 */
public class ScalesAndModes {
    
    // heptatonic
    public static final Scale NATURAL_MINOR_SCALE = new Scale("Natural minor", new int[] {2, 1, 2, 2, 1, 2, 2});
    public static final Scale HARMONIC_MINOR_SCALE = new Scale("Harmonic minor", new int[] {2, 1, 2, 2, 1, 3, 1});
    public static final Scale ASCENDING_MELODIC_MINOR_SCALE = new Scale("Ascending melodic minor", new int[] {2, 1, 2, 2, 2, 2, 1});
    public static final Scale DESCENDING_MELODIC_MINOR_SCALE = new Scale("Descending melodic minor", new int[] {2, 1, 2, 2, 1, 2, 2});
    public static final Scale JAZZ_MELODIC_MINOR_SCALE = ASCENDING_MELODIC_MINOR_SCALE;
    public static final Mode JAZZ_MINOR_SCALE = new Mode("Jazz minor", JAZZ_MELODIC_MINOR_SCALE, 1);
    public static final Mode DORIAN_FLAT2 = new Mode("Dorian ♭2", JAZZ_MELODIC_MINOR_SCALE, 2);
    public static final Mode PHRYGIAN_NATURAL6 = new Mode("Phrygian ♮6", JAZZ_MELODIC_MINOR_SCALE, 2);
    public static final Mode LYDIAN_AUGMENTED = new Mode("Lydian augmented", JAZZ_MELODIC_MINOR_SCALE, 3);
    public static final Mode ACOUSTIC_SCALE= new Mode("Acoustic scale", JAZZ_MELODIC_MINOR_SCALE, 4);
    public static final Mode LYDIAN_DOMINANT = new Mode("Lydian dominant", JAZZ_MELODIC_MINOR_SCALE, 4);
    public static final Mode MYXOLYDIAN_SHARP4 = new Mode("Mixolydian ♯4", JAZZ_MELODIC_MINOR_SCALE, 4);
    public static final Mode OVERTONE = new Mode("Overtone", JAZZ_MELODIC_MINOR_SCALE, 4);
    public static final Mode AEOLIAN_DOMINANT = new Mode("Aeolian dominant", JAZZ_MELODIC_MINOR_SCALE, 5);
    public static final Mode MYXOLYDIAN_FLAT6 = new Mode("Mixolydian ♭6", JAZZ_MELODIC_MINOR_SCALE, 5);
    public static final Mode DESCENDING_MELODIC_MAJOR = new Mode("Descending melodic major", JAZZ_MELODIC_MINOR_SCALE, 5);
    public static final Mode HINDU = new Mode("Hindu", JAZZ_MELODIC_MINOR_SCALE, 5);
    public static final Mode HALF_DIMINISHED = new Mode("Half diminished", JAZZ_MELODIC_MINOR_SCALE, 6);
    public static final Mode LOCRIAN_NATURAL2 = new Mode("Locrian ♮2", JAZZ_MELODIC_MINOR_SCALE, 6);
    public static final Mode AEOLIAN_FLAT5 = new Mode("Aeolian ♭5", JAZZ_MELODIC_MINOR_SCALE, 6);
    public static final Mode ALTERED_SCALE = new Mode("Altered scale", JAZZ_MELODIC_MINOR_SCALE, 7);
    public static final Mode SUPER_LOCRIAN = new Mode("Super Locrian", JAZZ_MELODIC_MINOR_SCALE, 7);
    public static final Mode LOCRIAN_FLAT4 = new Mode("Locrian ♭4", JAZZ_MELODIC_MINOR_SCALE, 7);
    public static final Mode ALTERED_DOMINANT_SCALE = new Mode("Altered dominant scale", JAZZ_MELODIC_MINOR_SCALE, 7);

    
    public static final Scale MAJOR_SCALE = new Scale("Major", new int[] {2, 2, 1, 2, 2, 2, 1});
    
    public static final Mode IONIAN = new Mode("Ionian", MAJOR_SCALE, 1);
    public static final Mode DORIAN = new Mode("Dorian", MAJOR_SCALE, 2);
    public static final Mode PHRYGIAN = new Mode("Phrygian", MAJOR_SCALE, 3);
    public static final Mode LYDIAN = new Mode("Lydian", MAJOR_SCALE, 4);
    public static final Mode MIXOLYDIAN = new Mode("Myxolydian", MAJOR_SCALE, 5);
    public static final Mode AEOLIAN = new Mode("Aeolian", MAJOR_SCALE, 6);
    public static final Mode LOCRIAN = new Mode("Locrian", MAJOR_SCALE, 7);
// dark to bright        LOCRIAN, PHRYGIAN, AEOLIAN, DORIAN, MIXOLYDIAN, IONIAN, LYDIAN
    
    public static final PitchedScale C_MAJOR = new PitchedScale(ChromaticNote.C, MAJOR_SCALE);
    public static final PitchedScale C_MIXOLYDIAN = new PitchedScale(ChromaticNote.C, MIXOLYDIAN);

    public static final Set<IntervalSequence> DIATONIC_SCALES = Set.of(IONIAN, DORIAN, PHRYGIAN, LYDIAN, MIXOLYDIAN, AEOLIAN, LOCRIAN);
    
    // Pentatonic
    public static final Scale MAJOR_PENTATONIC_SCALE = new Scale("Major pentatonic", new int[] {2, 2, 3, 2, 3});
    public static final Scale RELATIVE_MINOR_PENTATONIC_SCALE = new Scale("Relative minor pentatonic", new int[] {3, 2, 2, 3, 2});
    public static final Mode MAJOR_PENTATONIC_MODE = new Mode("Major pentatonic", MAJOR_PENTATONIC_SCALE, 1);
    public static final Mode SUSPENDED_MODE = new Mode("Suspended", MAJOR_PENTATONIC_SCALE, 2);
    public static final Mode BLUES_MINOR_MODE = new Mode("Blues minor", MAJOR_PENTATONIC_SCALE, 3);
    public static final Mode BLUES_MAJOR_MODE = new Mode("Blues major", MAJOR_PENTATONIC_SCALE, 4);
    public static final Mode MINOR_PENTATONIC_MODE = new Mode("Minor pentatonic", MAJOR_PENTATONIC_SCALE, 5); // difference to RELATIVE_MINOR_PENTATONIC_SCALE???
    
    // others
    public static final Scale WHOLE_TONE_SCALE = new Scale("Whole tone", new int[] {2, 2, 2, 2, 2, 2});
    public static final Scale DOMINANT_BEBOP_SCALE = new Scale("Dominant bebop", new int[] {2, 2, 1, 2, 2, 1, 1, 1});
    public static final Scale MAJOR_BEBOP_SCALE = new Scale("Major bebop", new int[] {2, 2, 1, 2, 1, 1, 2, 1});
    public static final Scale ENIGMATIC_SCALE = new Scale("Enigmatic", new int[] {1, 3, 2, 2, 2, 1, 1});
    

}
