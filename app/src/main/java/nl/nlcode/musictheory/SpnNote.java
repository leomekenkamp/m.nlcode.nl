package nl.nlcode.musictheory;

import java.util.Comparator;
import nl.nlcode.musictheory.ChromaticNote.Accidental;
import nl.nlcode.musictheory.ChromaticNote.AccidentalPreference;

/**
 * Adds the octave to the conceptual {@link ChromaticNote}, differentiating e.g.
 * a 'high C' from a 'low C'.
 * 
 * 
 * @author leo
 */
public class SpnNote {

    public static Comparator<SpnNote> BY_PITCH = (o1, o2) -> {
        return -o1.semitonesTo(o2);
    };

    public static final int SEMITONES_PER_OCTAVE = 12;
    public static final int MIDI_CENTRAL_OCTAVE = 4;
    public static final int MIDI_CENTRAL_C = 60;
    public static final int MIDI_CENTRAL_A = MIDI_CENTRAL_C - ChromaticNote.C.semitonesDownTo(ChromaticNote.A);

    private final ChromaticNote chromaticNote;
    private final int octave;

    /**
     * https://en.wikipedia.org/wiki/Scientific_pitch_notation
     * 
     * @param chromaticNote
     * @param octave 
     */
    public SpnNote(ChromaticNote chromaticNote, int octave) {
        if (chromaticNote == null) {
            throw new IllegalArgumentException("chromaticNote cannot be null");
        }
        if (octave < -1 || octave > 10) {
            throw new IllegalArgumentException("octave out of reach");
        }
        this.chromaticNote = chromaticNote;
        this.octave = octave;
    }

    public ChromaticNote getChromaticNote() {
        return chromaticNote;
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof SpnNote other) 
                && semitonesTo(other) == 0;
    }

    @Override
    public int hashCode() {
        return toMidiNote();
    }

    /**
     *
     * @param other
     * @return Distance in semitones to the {@code other SpnNote}, positive if the other is
     * higher, negative if the other is lower.
     */
    public int semitonesTo(SpnNote other) {
        return chromaticNote.semitonesTo(other.chromaticNote) + SEMITONES_PER_OCTAVE * (other.octave - octave);
    }

    public int toMidiNote() {
        return toMidiNote(chromaticNote, octave);
    }

    public static SpnNote fromMidiNote(int midiNote, AccidentalPreference preference) {
        checkMidiNote(midiNote);
        // next two lines make clever use of the fact that the middle C is midi note 60
        int octave = (midiNote / SEMITONES_PER_OCTAVE) - 1;
        int fromC = midiNote % SEMITONES_PER_OCTAVE;
        ChromaticNote note = ChromaticNote.C.withSemitones(fromC, preference);
        return new SpnNote(note, octave);
    }

    protected static void checkMidiNote(int midiNote) {
        if (midiNote < 0 || midiNote > 127) {
            throw new IllegalArgumentException("midiNote must be in interval [0, 127]");
        }
    }

    public static int toMidiNote(ChromaticNote note, int octave) {
        return MIDI_CENTRAL_C + note.semitonesDownTo(ChromaticNote.C) + (octave - MIDI_CENTRAL_OCTAVE) * SEMITONES_PER_OCTAVE;
    }

    @Override
    public String toString() {
        return chromaticNote.toString() + octave;
    }

    
}
