package nl.nlcode.m.engine;

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
public class EnglishNoteStringConverterTest {
    
    private EnglishNoteStringConverter instance = new EnglishNoteStringConverter();
    
    public EnglishNoteStringConverterTest() {
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
        assertThat(instance.toString(63), is("D♯4/E♭4"));
    }

    @Test
    public void trivial_from_string() {
        assertThat(instance.fromString("C4"), is(60));
    }
    
    @Test
    public void part_from_string() {
        assertThat(instance.fromString("E♭4"), is(63));
        assertThat(instance.fromString("D♯4"), is(63));
    }
    
    @Test
    public void full_from_string() {
        assertThat(instance.fromString("D♯4/E♭4"), is(63));
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
    
}
