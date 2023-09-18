package nl.nlcode.musictheory;

import java.util.Comparator;

/**
 *
 * @author leo
 */
public class PitchedNote {

    public static Comparator<PitchedNote> BY_PITCH = (o1, o2) -> {
        return o1.distanceInSemitones(o2);
    };

    public static final int SEMITONES_PER_OCTAVE = 12;
    public static final int MIDI_CENTRAL_OCTAVE = 4;
    public static final int MIDI_CENTRAL_C = 60;
    public static final PitchedNote LOWEST_PIANO_NOTE = new PitchedNote(ChromaticNote.A, 0);
    public static final PitchedNote HIGHEST_PIANO_NOTE = new PitchedNote(ChromaticNote.C, 8);
    public static final PitchedNote LOWEST_ORGAN_NOTE = new PitchedNote(ChromaticNote.C, 2);
    public static final PitchedNote HIGHEST_ORGAN_NOTE = new PitchedNote(ChromaticNote.C, 7);

    private final ChromaticNote note;
    private final int octave;

    public PitchedNote(ChromaticNote note, int octave) {
        this.note = note;
        this.octave = octave;
    }

    public ChromaticNote getChromaticNote() {
        return note;
    }

    /**
     *
     * @param other
     * @return Distance in semitones to the {@code other PitchedNote}, positive if the other is
     * higher, negative if the other is lower.
     */
    public int distanceInSemitones(PitchedNote other) {
        return other.note.getSemitonesFromC() - note.getSemitonesFromC() + (other.octave - octave) * SEMITONES_PER_OCTAVE;
    }

    public int toMidiNote() {
        return toMidiNote(note, octave);
    }

    public static PitchedNote fromMidiNotePreferFlat(int midiNote) {
        checkMidiNote(midiNote);
        int octave = midiNote / SEMITONES_PER_OCTAVE - 1;
        ChromaticNote note = ChromaticNote.fromSemitoneFromCPreferFlat(midiNote % SEMITONES_PER_OCTAVE);
        return new PitchedNote(note, octave);
    }

    public static PitchedNote fromMidiNotePreferSharp(int midiNote) {
        checkMidiNote(midiNote);
        int octave = midiNote / SEMITONES_PER_OCTAVE - 1;
        ChromaticNote note = ChromaticNote.fromSemitoneFromCPreferSharp(midiNote % SEMITONES_PER_OCTAVE);
        return new PitchedNote(note, octave);
    }

    protected static void checkMidiNote(int midiNote) {
        if (midiNote < 0 || midiNote > 127) {
            throw new IllegalArgumentException("midiNote must be in interval [0, 127]");
        }
    }

    public static int toMidiNote(ChromaticNote note, int octave) {
        return MIDI_CENTRAL_C + note.getSemitonesFromC() + (octave - MIDI_CENTRAL_OCTAVE) * SEMITONES_PER_OCTAVE;
    }

}
