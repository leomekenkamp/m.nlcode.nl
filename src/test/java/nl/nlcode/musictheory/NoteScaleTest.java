package nl.nlcode.musictheory;

import java.util.ArrayList;
import java.util.List;
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
public class NoteScaleTest {

    public NoteScaleTest() {
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
    public void notes_in_C_Ionian() {
        int octave = 4;
        NoteScale scale = new NoteScale(ChromaticNote.C, ScalesAndModes.IONIAN);
        List<PitchedNote> actual = scale.createOctaveNotesUpAndDown(octave);
        List<PitchedNote> expected = new ArrayList<>();

        expected.add(new PitchedNote(ChromaticNote.C, octave));
        expected.add(new PitchedNote(ChromaticNote.D, octave));
        expected.add(new PitchedNote(ChromaticNote.E, octave));
        expected.add(new PitchedNote(ChromaticNote.F, octave));
        expected.add(new PitchedNote(ChromaticNote.G, octave));
        expected.add(new PitchedNote(ChromaticNote.A, octave));
        expected.add(new PitchedNote(ChromaticNote.B, octave));
        expected.add(new PitchedNote(ChromaticNote.C, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.C, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.B, octave));
        expected.add(new PitchedNote(ChromaticNote.A, octave));
        expected.add(new PitchedNote(ChromaticNote.G, octave));
        expected.add(new PitchedNote(ChromaticNote.F, octave));
        expected.add(new PitchedNote(ChromaticNote.E, octave));
        expected.add(new PitchedNote(ChromaticNote.D, octave));
        expected.add(new PitchedNote(ChromaticNote.C, octave));

        assertSameContents(actual, expected);
    }

    @Test
    public void notes_in_C_Aeolian() {
        int octave = 4;
        NoteScale scale = new NoteScale(ChromaticNote.C, ScalesAndModes.AEOLIAN);
        List<PitchedNote> actual = scale.createOctaveNotesUpAndDown(octave);
        List<PitchedNote> expected = new ArrayList<>();

        expected.add(new PitchedNote(ChromaticNote.C, octave));
        expected.add(new PitchedNote(ChromaticNote.D, octave));
        expected.add(new PitchedNote(ChromaticNote.E_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.F, octave));
        expected.add(new PitchedNote(ChromaticNote.G, octave));
        expected.add(new PitchedNote(ChromaticNote.A_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.B_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.C, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.C, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.B_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.A_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.G, octave));
        expected.add(new PitchedNote(ChromaticNote.F, octave));
        expected.add(new PitchedNote(ChromaticNote.E_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.D, octave));
        expected.add(new PitchedNote(ChromaticNote.C, octave));

        assertSameContents(actual, expected);
    }

    @Test
    public void notes_in_C_Melodic_minor() {
        int octave = 4;
        NoteScale scale = new NoteScale(ChromaticNote.C, ScalesAndModes.MELODIC_MINOR_SCALE);
        List<PitchedNote> actual = scale.createOctaveNotesUpAndDown(octave);
        List<PitchedNote> expected = new ArrayList<>();

        expected.add(new PitchedNote(ChromaticNote.C, octave));
        expected.add(new PitchedNote(ChromaticNote.D, octave));
        expected.add(new PitchedNote(ChromaticNote.E_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.F, octave));
        expected.add(new PitchedNote(ChromaticNote.G, octave));
        expected.add(new PitchedNote(ChromaticNote.A, octave));
        expected.add(new PitchedNote(ChromaticNote.B, octave));
        expected.add(new PitchedNote(ChromaticNote.C, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.C, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.B_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.A_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.G, octave));
        expected.add(new PitchedNote(ChromaticNote.F, octave));
        expected.add(new PitchedNote(ChromaticNote.E_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.D, octave));
        expected.add(new PitchedNote(ChromaticNote.C, octave));

        assertSameContents(actual, expected);
    }

    @Test
    public void notes_in_Fsharp_Ionian() {
        int octave = 4;
        NoteScale scale = new NoteScale(ChromaticNote.F_SHARP, ScalesAndModes.IONIAN);
        List<PitchedNote> actual = scale.createOctaveNotesUpAndDown(octave);
        List<PitchedNote> expected = new ArrayList<>();

        expected.add(new PitchedNote(ChromaticNote.F_SHARP, octave));
        expected.add(new PitchedNote(ChromaticNote.G_SHARP, octave));
        expected.add(new PitchedNote(ChromaticNote.A_SHARP, octave));
        expected.add(new PitchedNote(ChromaticNote.B, octave));
        expected.add(new PitchedNote(ChromaticNote.C_SHARP, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.D_SHARP, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.F, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.F_SHARP, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.F_SHARP, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.E.getSharp(), octave + 1));
        expected.add(new PitchedNote(ChromaticNote.D_SHARP, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.C_SHARP, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.B, octave));
        expected.add(new PitchedNote(ChromaticNote.A_SHARP, octave));
        expected.add(new PitchedNote(ChromaticNote.G_SHARP, octave));
        expected.add(new PitchedNote(ChromaticNote.F_SHARP, octave));

        assertSameContents(actual, expected);
    }

    @Test
    public void notes_in_C_Enigmatic() {
        int octave = 4;
        NoteScale scale = new NoteScale(ChromaticNote.C, ScalesAndModes.ENIGMATIC_SCALE);
        List<PitchedNote> actual = scale.createOctaveNotesUpAndDown(octave);
        List<PitchedNote> expected = new ArrayList<>();

        expected.add(new PitchedNote(ChromaticNote.C, octave));
        expected.add(new PitchedNote(ChromaticNote.D_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.E, octave));
        expected.add(new PitchedNote(ChromaticNote.F_SHARP, octave));
        expected.add(new PitchedNote(ChromaticNote.G_SHARP, octave));
        expected.add(new PitchedNote(ChromaticNote.A_SHARP, octave));
        expected.add(new PitchedNote(ChromaticNote.B, octave));
        expected.add(new PitchedNote(ChromaticNote.C, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.C, octave + 1));
        expected.add(new PitchedNote(ChromaticNote.B, octave));
        expected.add(new PitchedNote(ChromaticNote.A_SHARP, octave));
        expected.add(new PitchedNote(ChromaticNote.G_SHARP, octave));
        expected.add(new PitchedNote(ChromaticNote.F, octave));
        expected.add(new PitchedNote(ChromaticNote.E, octave));
        expected.add(new PitchedNote(ChromaticNote.D_FLAT, octave));
        expected.add(new PitchedNote(ChromaticNote.C, octave));

        assertSameContents(actual, expected);
    }

    public void assertSameContents(List<PitchedNote> actual, List<PitchedNote> expected) {
        assertThat("size should be equal", actual.size(), is(expected.size()));
        for (int i = 0; i < actual.size(); i++) {
            assertThat("note at index <" + i + "> should match", actual.get(i), is(expected.get(i)));
        }
    }

}
