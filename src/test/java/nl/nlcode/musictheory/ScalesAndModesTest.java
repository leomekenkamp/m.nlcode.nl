package nl.nlcode.musictheory;

import nl.nlcode.musictheory.ScalesAndModes;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author leo
 */
public class ScalesAndModesTest {
    
    public ScalesAndModesTest() {
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
    public void melodic_minor_scale_has_nine_tones() {
        assertThat(ScalesAndModes.MELODIC_MINOR_SCALE.toneCount(), is(9));
    }
    
    @Test
    public void major_scale_has_seven_tones() {
        assertThat(ScalesAndModes.MAJOR_SCALE.toneCount(), is(7));
    }
    
    @Test
    public void Ionian_intervals_are_whole_whole_half_whole_whole_whole_half() {
        assertThat(ScalesAndModes.IONIAN.intervalUp(1), is(2));
        assertThat(ScalesAndModes.IONIAN.intervalUp(2), is(2));
        assertThat(ScalesAndModes.IONIAN.intervalUp(3), is(1));
        assertThat(ScalesAndModes.IONIAN.intervalUp(4), is(2));
        assertThat(ScalesAndModes.IONIAN.intervalUp(5), is(2));
        assertThat(ScalesAndModes.IONIAN.intervalUp(6), is(2));
        assertThat(ScalesAndModes.IONIAN.intervalUp(7), is(1));
    }

    @Test
    public void Ionian_distance_to_tonic() {
        assertThat(ScalesAndModes.IONIAN.semitonesFromTonicByDegree(1), is(0));
        assertThat(ScalesAndModes.IONIAN.semitonesFromTonicByDegree(2), is(2));
        assertThat(ScalesAndModes.IONIAN.semitonesFromTonicByDegree(3), is(4));
        assertThat(ScalesAndModes.IONIAN.semitonesFromTonicByDegree(4), is(5));
        assertThat(ScalesAndModes.IONIAN.semitonesFromTonicByDegree(5), is(7));
        assertThat(ScalesAndModes.IONIAN.semitonesFromTonicByDegree(6), is(9));
        assertThat(ScalesAndModes.IONIAN.semitonesFromTonicByDegree(7), is(11));
        assertThat(ScalesAndModes.IONIAN.semitonesFromTonicByDegree(8), is(12));
    }

    @Test
    public void Phrygian_intervals_are_half_whole_whole_whole_half_whole_whole() {
        assertThat(ScalesAndModes.PHRYGIAN.intervalUp(1), is(1));
        assertThat(ScalesAndModes.PHRYGIAN.intervalUp(2), is(2));
        assertThat(ScalesAndModes.PHRYGIAN.intervalUp(3), is(2));
        assertThat(ScalesAndModes.PHRYGIAN.intervalUp(4), is(2));
        assertThat(ScalesAndModes.PHRYGIAN.intervalUp(5), is(1));
        assertThat(ScalesAndModes.PHRYGIAN.intervalUp(6), is(2));
        assertThat(ScalesAndModes.PHRYGIAN.intervalUp(7), is(2));
    }

    @Test
    public void Phrygian_distance_to_tonic() {
        assertThat(ScalesAndModes.PHRYGIAN.semitonesFromTonicByDegree(1), is(0));
        assertThat(ScalesAndModes.PHRYGIAN.semitonesFromTonicByDegree(2), is(1));
        assertThat(ScalesAndModes.PHRYGIAN.semitonesFromTonicByDegree(3), is(3));
        assertThat(ScalesAndModes.PHRYGIAN.semitonesFromTonicByDegree(4), is(5));
        assertThat(ScalesAndModes.PHRYGIAN.semitonesFromTonicByDegree(5), is(7));
        assertThat(ScalesAndModes.PHRYGIAN.semitonesFromTonicByDegree(6), is(8));
        assertThat(ScalesAndModes.PHRYGIAN.semitonesFromTonicByDegree(7), is(10));
        assertThat(ScalesAndModes.PHRYGIAN.semitonesFromTonicByDegree(8), is(12));
    }

    @Test
    public void Ionian_Lydian_and_Myxolydian_are_major_and_not_minor() {
        assertThat(ScalesAndModes.IONIAN.isMajor(), is(true));
        assertThat(ScalesAndModes.IONIAN.isMinor(), is(false));
        assertThat(ScalesAndModes.LYDIAN.isMajor(), is(true));
        assertThat(ScalesAndModes.LYDIAN.isMinor(), is(false));
        assertThat(ScalesAndModes.MIXOLYDIAN.isMajor(), is(true));
        assertThat(ScalesAndModes.MIXOLYDIAN.isMinor(), is(false));
    }
    
    @Test
    public void Phrygian_Aeolian_Dorian_and_Locrian_are_minor_and_not_major() {
        assertThat(ScalesAndModes.PHRYGIAN.isMajor(), is(false));
        assertThat(ScalesAndModes.PHRYGIAN.isMinor(), is(true));
        assertThat(ScalesAndModes.AEOLIAN.isMajor(), is(false));
        assertThat(ScalesAndModes.AEOLIAN.isMinor(), is(true));
        assertThat(ScalesAndModes.DORIAN.isMajor(), is(false));
        assertThat(ScalesAndModes.DORIAN.isMinor(), is(true));
        assertThat(ScalesAndModes.LOCRIAN.isMajor(), is(false));
        assertThat(ScalesAndModes.LOCRIAN.isMinor(), is(true));
    }
    
}
