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
public class RomanceShortNoteStringConverterTest {

    private RomanceShortNoteStringConverter instance = new RomanceShortNoteStringConverter();

    public RomanceShortNoteStringConverterTest() {
    }

    @Test
    public void trivial_to_string() {
        assertThat(instance.toString(60), is("do4"));
    }

    @Test
    public void half_to_string() {
        assertThat(instance.toString(63), is("re♯4/mi♭4"));
    }

    @Test
    public void trivial_from_string() {
        assertThat(instance.fromString("do4"), is(60));
    }

    @Test
    public void part_from_string() {
        assertThat(instance.fromString("mi♭4"), is(63));
        assertThat(instance.fromString("re♯4"), is(63));
    }

    @Test
    public void full_from_string() {
        assertThat(instance.fromString("re♯4/mi♭4"), is(63));
    }

    @Test
    public void full_from_string_error_in_first_half() {
        assertThat(instance.fromString("poing/mi♭4"), is(63));
    }

    @Test
    public void full_from_string_error_in_second_half() {
        assertThat(instance.fromString("re♯4/poing"), is(63));
    }

    @Test
    public void plus_for_sharp_from_string() {
        assertThat(instance.fromString("re+4"), is(63));
    }

    @Test
    public void minus_for_flat_from_string() {
        assertThat(instance.fromString("mi-4"), is(63));
    }

    @Test
    public void hash_for_sharp_from_string() {
        assertThat(instance.fromString("la#0"), is(22));
    }

    @Test
    public void at_for_flat_from_string() {
        assertThat(instance.fromString("si@0"), is(22));
    }

    @Test
    public void sol_from_string() {
        assertThat(instance.fromString("sol4"), is(67));
    }

    @Test
    public void lower_boundary() {
        assertThat(instance.fromString("do0"), is(12));
        assertThat(instance.toString(12), is("do0"));
    }

    @Test
    public void upper_boundary() {
        assertThat(instance.fromString("sol9"), is(127));
        assertThat(instance.toString(127), is("sol9"));
    }

    @Test
    public void round_trip() {
        for (int i = instance.fromString("do0"); i < instance.fromString("sol9"); i++) {
            System.out.println(i + " " + instance.toString(i));
            assertThat("equality in from-to for note " + i, instance.fromString(instance.toString(i)), is(i));
        }
    }

}
