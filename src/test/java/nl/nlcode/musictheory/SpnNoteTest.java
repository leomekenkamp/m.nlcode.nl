package nl.nlcode.musictheory;

import static nl.nlcode.musictheory.ChromaticNote.Accidental.FLAT;
import static nl.nlcode.musictheory.ChromaticNote.Accidental.SHARP;
import static nl.nlcode.musictheory.ChromaticNote.AccidentalPreference.FLATTER;
import static nl.nlcode.musictheory.ChromaticNote.AccidentalPreference.SHARPER;
import static nl.nlcode.musictheory.ChromaticNote.C;
import static nl.nlcode.musictheory.SpnNote.MIDI_CENTRAL_C;
import static nl.nlcode.musictheory.SpnNote.SEMITONES_PER_OCTAVE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author leo
 */
public class SpnNoteTest {
    
    public SpnNoteTest() {
    }
    
    @Test
    public void C5_is_midi_72() {
        assertThat(new SpnNote(ChromaticNote.C, 5).toMidiNote(), is(72));
    }

    @Test
    public void G4_is_midi_67() {
        assertThat(new SpnNote(ChromaticNote.G, 4).toMidiNote(), is(67));
    }

    @Test
    public void C9_is_midi_120() {
        assertThat(new SpnNote(ChromaticNote.C, 5).toMidiNote(), is(72));
    }

    @Test
    public void central_C_is_midi_60() {
        assertThat(new SpnNote(ChromaticNote.C, 4).toMidiNote(), is(60));
    }

    @Test
    public void lowest_note_C_minus1_is_midi_0() {
        assertThat(new SpnNote(C, -1).toMidiNote(), is(0));
    }
    
    @Test
    public void highest_note_G9_is_midi_127() {
        assertThat(new SpnNote(ChromaticNote.G, 9).toMidiNote(), is(127));
    }
    
    @Test
    public void from_midi_note_and_back_max() {
        assertThat(SpnNote.fromMidiNote(127, FLATTER).toMidiNote(), is(127));
        assertThat(SpnNote.fromMidiNote(127, SHARPER).toMidiNote(), is(127));
    }
    
    @Test
    public void from_midi_note_and_back_min() {
        assertThat(SpnNote.fromMidiNote(0, FLATTER).toMidiNote(), is(0));
        assertThat(SpnNote.fromMidiNote(0, SHARPER).toMidiNote(), is(0));
    }
    
    @Test
    public void from_midi_note_and_back_C4() {
        assertThat(SpnNote.fromMidiNote(MIDI_CENTRAL_C, FLATTER).toMidiNote(), is(MIDI_CENTRAL_C));
        assertThat(SpnNote.fromMidiNote(MIDI_CENTRAL_C, SHARPER).toMidiNote(), is(MIDI_CENTRAL_C));
    }
    
    @Test
    public void from_midi_note_and_back_Csharp4() {
        assertThat(SpnNote.fromMidiNote(MIDI_CENTRAL_C + 1, FLATTER).toMidiNote(), is(MIDI_CENTRAL_C + 1));
        assertThat(SpnNote.fromMidiNote(MIDI_CENTRAL_C + 1, SHARPER).toMidiNote(), is(MIDI_CENTRAL_C + 1));
    }
    
    @Test
    public void from_midi_note_and_back_Cflat4() {
        assertThat(SpnNote.fromMidiNote(MIDI_CENTRAL_C - 1, FLATTER).toMidiNote(), is(MIDI_CENTRAL_C - 1));
        assertThat(SpnNote.fromMidiNote(MIDI_CENTRAL_C - 1, SHARPER).toMidiNote(), is(MIDI_CENTRAL_C - 1));
    }
    
    @Test
    public void from_midi_note() {
        assertThat(SpnNote.fromMidiNote(54, SHARPER).getChromaticNote(), is(ChromaticNote.F_SHARP));
        assertThat(SpnNote.fromMidiNote(54, FLATTER).getChromaticNote(), is(ChromaticNote.G_FLAT));
    }
    
    @Test
    public void semitonesTo_equal() {
        assertThat(new SpnNote(ChromaticNote.A, 4).semitonesTo(new SpnNote(ChromaticNote.A, 4)), is(0));
    }

    @Test
    public void semitonesTo_one_octave_up() {
        assertThat(new SpnNote(ChromaticNote.A, 3).semitonesTo(new SpnNote(ChromaticNote.A, 4)), is(SEMITONES_PER_OCTAVE));
    }
}
