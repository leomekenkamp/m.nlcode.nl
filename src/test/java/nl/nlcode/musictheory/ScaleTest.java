package nl.nlcode.musictheory;

import nl.nlcode.musictheory.Scale;
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
public class ScaleTest {
    
    public ScaleTest() {
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
    public void intervalUpForMode_should_give_correct_intervals() {
        Scale testScale = new Scale("test", new int[] {1, 2, 4, 7, 12, 20, 33});
        assertThat(testScale.intervalUp(1, 1), is(1));
        assertThat(testScale.intervalUp(1, 7), is(33));
        assertThat(testScale.intervalUp(7, 1), is(33));
        assertThat(testScale.intervalUp(1, 6), is(20));
        assertThat(testScale.intervalUp(2, 2), is(4));
        assertThat(testScale.intervalUp(2, 6), is(33));
        assertThat(testScale.intervalUp(3, 6), is(1));
        assertThat(testScale.intervalUp(6, 6), is(7));
        assertThat(testScale.intervalUp(7, 7), is(20));
        assertThat(testScale.intervalUp(9, 7), is(1));
    }

    @Test
    public void intervalDownForMode_should_give_correct_intervals() {
        Scale testScale = new Scale("test", new int[] {1, 2, 4, 7, 12, 20, 33});
        assertThat(testScale.intervalDown(1, 1), is(33));
        assertThat(testScale.intervalDown(1, 7), is(20));
        assertThat(testScale.intervalDown(7, 1), is(20));
        assertThat(testScale.intervalDown(1, 6), is(12));
        assertThat(testScale.intervalDown(2, 2), is(2));
        assertThat(testScale.intervalDown(2, 6), is(20));
        assertThat(testScale.intervalDown(3, 6), is(33));
        assertThat(testScale.intervalDown(6, 6), is(4));
        assertThat(testScale.intervalDown(7, 7), is(12));
        assertThat(testScale.intervalDown(9, 7), is(33));
    }

}
