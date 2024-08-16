package nl.nlcode.musictheory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jq59bu
 */
public class ChromaticNaturalNoteTest {
    
    public ChromaticNaturalNoteTest() {
    }
    
    @Test
    public void semitonesUpTo_zero_for_same_note() {
        assertThat(ChromaticNaturalNote.C.semitonesUpTo(ChromaticNaturalNote.C), is(0));
    }

    @Test
    public void semitonesUpTo_for_higher_letter() {
        assertThat(ChromaticNaturalNote.D.semitonesUpTo(ChromaticNaturalNote.E), is(2));
    }

    @Test
    public void semitonesUpTo_for_lower_letter() {
        assertThat(ChromaticNaturalNote.F.semitonesUpTo(ChromaticNaturalNote.A), is(4));
    }

    @Test
    public void semitonesDownTo_zero_for_same_note() {
        assertThat(ChromaticNaturalNote.E.semitonesDownTo(ChromaticNaturalNote.E), is(0));
    }

    @Test
    public void semitonesDownTo_for_higher_letter() {
        assertThat(ChromaticNaturalNote.C.semitonesDownTo(ChromaticNaturalNote.F), is(7));
    }

    @Test
    public void semitonesDownTo_for_lower_letter() {
        assertThat(ChromaticNaturalNote.F.semitonesDownTo(ChromaticNaturalNote.A), is(8));
    }
    
    @Test
    public void BY_HEIGHT_equal() {
        assertThat(ChromaticNaturalNote.ALPHABETICALLY.compare(ChromaticNaturalNote.D, ChromaticNaturalNote.D), is(0));
    }

    @Test
    public void BY_HEIGHT_bigger() {
        assertThat(ChromaticNaturalNote.ALPHABETICALLY.compare(ChromaticNaturalNote.E, ChromaticNaturalNote.B), is(5));
    }

    @Test
    public void BY_HEIGHT_smaller() {
        assertThat(ChromaticNaturalNote.ALPHABETICALLY.compare(ChromaticNaturalNote.F, ChromaticNaturalNote.G), is(-2));
    }

    @Test
    public void BY_HEIGHT_max() {
        assertThat(ChromaticNaturalNote.ALPHABETICALLY.compare(ChromaticNaturalNote.A, ChromaticNaturalNote.G), is(-10));
    }

    @Test
    public void BY_HEIGHT_max_negative() {
        assertThat(ChromaticNaturalNote.ALPHABETICALLY.compare(ChromaticNaturalNote.G, ChromaticNaturalNote.A), is(10));
    }

}
