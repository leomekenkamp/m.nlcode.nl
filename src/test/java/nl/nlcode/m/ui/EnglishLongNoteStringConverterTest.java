package nl.nlcode.m.ui;

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
public class EnglishLongNoteStringConverterTest {

    private EnglishLongNoteStringConverter instance = new EnglishLongNoteStringConverter();

    public EnglishLongNoteStringConverterTest() {
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
    public void trivial_to_string() {
        assertThat(instance.toString(60), is("C4"));
    }

    @Test
    public void half_to_string() {
        assertThat(instance.toString(63), is("D sharp 4/E flat 4"));
    }

    @Test
    public void trivial_from_string() {
        assertThat(instance.fromString("C4"), is(60));
    }

    @Test
    public void part_from_string() {
        assertThat(instance.fromString("Eflat4"), is(63));
        assertThat(instance.fromString("D sharp 4"), is(63));
    }

    @Test
    public void full_from_string() {
        assertThat(instance.fromString("D sharp 4/E flat 4"), is(63));
    }

    @Test
    public void full_from_string_error_in_first_half() {
        assertThat(instance.fromString("poing/E♭4"), is(63));
    }

    @Test
    public void full_from_string_error_in_second_half() {
        assertThat(instance.fromString("D♯4/poing"), is(63));
    }

    @Test
    public void plus_for_sharp_from_string() {
        assertThat(instance.fromString("D+4"), is(63));
    }

    @Test
    public void minus_for_flat_from_string() {
        assertThat(instance.fromString("E-4"), is(63));
    }

    @Test
    public void hash_for_sharp_from_string() {
        assertThat(instance.fromString("A#0"), is(22));
    }

    @Test
    public void at_for_flat_from_string() {
        assertThat(instance.fromString("B@0"), is(22));
    }

    @Test
    public void text_sharp_from_string() {
        assertThat(instance.fromString("Bflat0"), is(22));
    }

    @Test
    public void text_flat_from_string() {
        assertThat(instance.fromString("Asharp0"), is(22));
    }

    @Test
    public void spaced_text_sharp_from_string() {
        assertThat(instance.fromString("B flat 0"), is(22));
    }

    @Test
    public void spaced_text_flat_from_string() {
        assertThat(instance.fromString("A sharp 0"), is(22));
    }

    @Test
    public void letter_sharp_from_string() {
        assertThat(instance.fromString("Bf0"), is(22));
    }

    @Test
    public void letter_flat_from_string() {
        assertThat(instance.fromString("As0"), is(22));
    }

    @Test
    public void lower_boundary() {
        assertThat(instance.fromString("C0"), is(12));
        assertThat(instance.toString(12), is("C0"));
    }

    @Test
    public void upper_boundary() {
        assertThat(instance.fromString("G9"), is(127));
        assertThat(instance.toString(127), is("G9"));
    }

    @Test
    public void round_trip() {
        for (int i = instance.fromString("C0"); i < instance.fromString("G9"); i++) {
            System.out.println(i + " " + instance.toString(i));
            assertThat("equality in from-to for note " + i, instance.fromString(instance.toString(i)), is(i));
        }
    }

}
