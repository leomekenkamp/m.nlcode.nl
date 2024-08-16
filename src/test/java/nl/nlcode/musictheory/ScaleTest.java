package nl.nlcode.musictheory;

import nl.nlcode.musictheory.Scale;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author leo
 */
public class ScaleTest {
    
    public ScaleTest() {
    }
    
//    @Test
//    public void intervalUpForMode_should_give_correct_intervals() {
//        Scale testScale = new Scale("test", new int[] {1, 2, 4, 7, 12, 20, 33});
//        assertThat(testScale.intervalUpFrom(1, 1), is(1));
//        assertThat(testScale.intervalUpFrom(1, 7), is(33));
//        assertThat(testScale.intervalUpFrom(7, 1), is(33));
//        assertThat(testScale.intervalUpFrom(1, 6), is(20));
//        assertThat(testScale.intervalUpFrom(2, 2), is(4));
//        assertThat(testScale.intervalUpFrom(2, 6), is(33));
//        assertThat(testScale.intervalUpFrom(3, 6), is(1));
//        assertThat(testScale.intervalUpFrom(6, 6), is(7));
//        assertThat(testScale.intervalUpFrom(7, 7), is(20));
//        assertThat(testScale.intervalUpFrom(9, 7), is(1));
//    }
//
//    @Test
//    public void intervalDownForMode_should_give_correct_intervals() {
//        Scale testScale = new Scale("test", new int[] {1, 2, 4, 7, 12, 20, 33});
//        assertThat(testScale.intervalDownFrom(1, 1), is(33));
//        assertThat(testScale.intervalDownFrom(1, 7), is(20));
//        assertThat(testScale.intervalDownFrom(7, 1), is(20));
//        assertThat(testScale.intervalDownFrom(1, 6), is(12));
//        assertThat(testScale.intervalDownFrom(2, 2), is(2));
//        assertThat(testScale.intervalDownFrom(2, 6), is(20));
//        assertThat(testScale.intervalDownFrom(3, 6), is(33));
//        assertThat(testScale.intervalDownFrom(6, 6), is(4));
//        assertThat(testScale.intervalDownFrom(7, 7), is(12));
//        assertThat(testScale.intervalDownFrom(9, 7), is(33));
//    }
//
//    @Test
//    public void semitonesFromTonicByDegree() {
//        Scale testScale = new Scale("test", new int[] {1, 2, 4, 7, 12, 20, 33});
//        assertThat(testScale.semitonesFromTonicByDegree(0), is(0));
//        assertThat(testScale.semitonesFromTonicByDegree(1), is(1));
//        assertThat(testScale.semitonesFromTonicByDegree(2), is(3));
//        assertThat(testScale.semitonesFromTonicByDegree(3), is(7));
//        assertThat(testScale.semitonesFromTonicByDegree(4), is(14));
//    }
//
//    @Test
//    public void semitonesFromTonicByDegree_and_mode1() {
//        Scale testScale = new Scale("test", new int[] {1, 2, 4, 7, 12, 20, 33});
//        assertThat(testScale.semitonesFromTonicByDegree(0, 1), is(0));
//        assertThat(testScale.semitonesFromTonicByDegree(1, 1), is(1));
//        assertThat(testScale.semitonesFromTonicByDegree(2, 1), is(3));
//        assertThat(testScale.semitonesFromTonicByDegree(3, 1), is(7));
//        assertThat(testScale.semitonesFromTonicByDegree(4, 1), is(14));
//    }
//
//    @Test
//    public void semitonesFromTonicByDegree_and_mode2() {
//        Scale testScale = new Scale("test", new int[] {1, 2, 4, 7, 12, 20, 33});
//        assertThat(testScale.semitonesFromTonicByDegree(0, 2), is(0));
//        assertThat(testScale.semitonesFromTonicByDegree(1, 2), is(2));
//        assertThat(testScale.semitonesFromTonicByDegree(2, 2), is(6));
//        assertThat(testScale.semitonesFromTonicByDegree(3, 2), is(13));
//        assertThat(testScale.semitonesFromTonicByDegree(4, 2), is(25));
//    }
}
