package nl.nlcode.musictheory;

import static nl.nlcode.musictheory.ChromaticNote.C;
import static nl.nlcode.musictheory.PitchedNote.MIDI_CENTRAL_C;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author leo
 */
public class PitchedNoteTest {
    
    public PitchedNoteTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    @Test
    public void C5_is_midi_72() {
        assertThat(new PitchedNote(ChromaticNote.C, 5).toMidiNote(), is(72));
    }

    @Test
    public void G4_is_midi_67() {
        assertThat(new PitchedNote(ChromaticNote.G, 4).toMidiNote(), is(67));
    }

    @Test
    public void C9_is_midi_120() {
        assertThat(new PitchedNote(ChromaticNote.C, 5).toMidiNote(), is(72));
    }

    @Test
    public void central_C_is_midi_60() {
        assertThat(new PitchedNote(ChromaticNote.C, 4).toMidiNote(), is(60));
    }

    @Test
    public void lowest_note_C_minus1_is_midi_0() {
        assertThat(new PitchedNote(C, -1).toMidiNote(), is(0));
    }
    
    @Test
    public void highest_note_G9_is_midi_127() {
        assertThat(new PitchedNote(ChromaticNote.G, 9).toMidiNote(), is(127));
    }
    
    @Test
    public void from_midi_note_and_back_max() {
        assertThat(PitchedNote.fromMidiNotePreferFlat(127).toMidiNote() ,is(127));
        assertThat(PitchedNote.fromMidiNotePreferSharp(127).toMidiNote() ,is(127));
    }
    
    @Test
    public void from_midi_note_and_back_min() {
        assertThat(PitchedNote.fromMidiNotePreferFlat(0).toMidiNote() ,is(0));
        assertThat(PitchedNote.fromMidiNotePreferSharp(0).toMidiNote() ,is(0));
    }
    
    @Test
    public void from_midi_note_and_back_C4() {
        assertThat(PitchedNote.fromMidiNotePreferFlat(MIDI_CENTRAL_C).toMidiNote() ,is(MIDI_CENTRAL_C));
        assertThat(PitchedNote.fromMidiNotePreferSharp(MIDI_CENTRAL_C).toMidiNote() ,is(MIDI_CENTRAL_C));
    }
    
    @Test
    public void from_midi_note_pref_flat() {
        assertThat(PitchedNote.fromMidiNotePreferFlat(54).getChromaticNote(), is(ChromaticNote.G_FLAT));
    }
    
    @Test
    public void from_midi_note_pref_sharp() {
        assertThat(PitchedNote.fromMidiNotePreferSharp(54).getChromaticNote(), is(ChromaticNote.F_SHARP));
    }
    
}
