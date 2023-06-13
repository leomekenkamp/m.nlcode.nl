package nl.nlcode.m.ui;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author leo
 */
public class GermanShortNoteStringConverterTest {

    private GermanShortNoteStringConverter instance = new GermanShortNoteStringConverter();

    public GermanShortNoteStringConverterTest() {
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
    public void no_B_flat_in_German_from_string() {
        assertThrows(IllegalArgumentException.class, () -> instance.fromString("B@0"));
    }

    @Test 
    public void des_from_string() {
        assertThat(instance.fromString("Des4"), is(61));
    }
    
    @Test 
    public void es_from_string() {
        assertThat(instance.fromString("es4"), is(63));
    }
    
    @Test 
    public void ges_from_string() {
        assertThat(instance.fromString("Ges4"), is(66));
    }
    
    @Test 
    public void as_from_string() {
        assertThat(instance.fromString("as4"), is(68));
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
        for (int i = instance.fromString("C0"); i <= instance.fromString("G9"); i++) {
            System.out.println(i + " " + instance.toString(i));
            assertThat("equality in from-to for note " + i, instance.fromString(instance.toString(i)), is(i));
        }
    }
    
    @Test
    public void to_string_for_German_H() {
        assertThat(instance.toString(83), is("H5"));
    }
    
    @Test
    public void to_string_for_German_a_sharp_b_flat() {
        assertThat(instance.toString(82), is("A♯5/B5"));
    }

}
