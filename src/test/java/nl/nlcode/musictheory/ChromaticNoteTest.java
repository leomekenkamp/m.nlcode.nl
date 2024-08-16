package nl.nlcode.musictheory;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author leo
 */
public class ChromaticNoteTest {

    public ChromaticNoteTest() {
    }

    @Test
    public void natural_of_Dsharp_is_D() {
        assertThat(ChromaticNote.D_SHARP.getNatural(), is(ChromaticNote.D));
    }

    @Test
    public void natural_of_Gflat_is_G() {
        assertThat(ChromaticNote.G_FLAT.getNatural(), is(ChromaticNote.G));
    }

    @Test
    public void natural_of_F_is_F() {
        assertThat(ChromaticNote.F.getNatural(), is(ChromaticNote.F));
    }

    @Test
    public void sharp_of_D_is_Dsharp() {
//        assertThat(ChromaticNote.D.getSharp(), is(ChromaticNote.D_SHARP));
    }

    @Test
    public void sharp_of_B_is_C() {
//        assertThat(ChromaticNote.B.getSharp(), is(ChromaticNote.C));
    }

    @Test
    public void sharp_of_Fsharp_is_G() {
//        assertThat(ChromaticNote.F_SHARP.getSharp(), is(ChromaticNote.G));
    }

    @Test
    public void sharp_of_Dflat_is_D() {
//        assertThat(ChromaticNote.D_FLAT.getSharp(), is(ChromaticNote.D));
    }

    @Test
    public void flat_of_C_is_B() {
//        assertThat(ChromaticNote.C.getFlat(), is(ChromaticNote.B));
    }

    @Test
    public void flat_of_G_is_Gflat() {
//        assertThat(ChromaticNote.G.getFlat(), is(ChromaticNote.G_FLAT));
    }

    @Test
    public void flat_of_Eflat_is_D() {
//        assertThat(ChromaticNote.E_FLAT.getFlat(), is(ChromaticNote.D));
    }

    @Test
    public void flat_of_Fsharp_is_F() {
//        assertThat(ChromaticNote.E_FLAT.getFlat(), is(ChromaticNote.D));
    }

    @Test
    public void BY_PITCH_equal() {
        assertThat(ChromaticNote.BY_PITCH.compare(ChromaticNote.D_DOUBLE_FLAT, ChromaticNote.D_DOUBLE_FLAT), is(0));
    }

    @Test
    public void BY_PITCH_equal_different_source() {
        assertThat(ChromaticNote.BY_PITCH.compare(ChromaticNote.D_DOUBLE_FLAT, ChromaticNote.B_SHARP), is(0));
    }

    @Test
    public void BY_PITCH_bigger() {
        assertThat(ChromaticNote.BY_PITCH.compare(ChromaticNote.E_SHARP, ChromaticNote.B_FLAT), is(7));
    }

    @Test
    public void BY_PITCH_smaller() {
        assertThat(ChromaticNote.BY_PITCH.compare(ChromaticNote.F, ChromaticNote.G_SHARP), is(-3));
    }

    @Test
    public void BY_PITCH_natural_crossover() {
        assertThat(ChromaticNote.BY_PITCH.compare(ChromaticNote.E_QUADRUPLE_SHARP, ChromaticNote.G_QUADRUPLE_FLAT), is(5));
    }

    @Test
    public void BY_PITCH_natural_crossover_alphabetically_equal() {
        assertThat(ChromaticNote.BY_PITCH.compare(ChromaticNote.A_FLAT, ChromaticNote.G_SHARP), is(0));
    }

    @Test
    public void BY_PITCH_natural_crossover_alphabetically_cross() {
        assertThat(ChromaticNote.BY_PITCH.compare(ChromaticNote.A_QUADRUPLE_FLAT, ChromaticNote.G_QUADRUPLE_SHARP), is(6));
    }

    @Test
    public void BY_PITCH_AND_ACCIDENT_sharp_smaller_than_flat() {
        assertThat(ChromaticNote.BY_PITCH_AND_ACCIDENTAL.compare(ChromaticNote.A_SHARP, ChromaticNote.B_FLAT), is(both(
                greaterThan(-10)).and(lessThan(0))));
    }

    @Test
    public void BY_PITCH_AND_ACCIDENT_double_sharp_smaller_than_natural() {
        assertThat(ChromaticNote.BY_PITCH_AND_ACCIDENTAL.compare(ChromaticNote.A_DOUBLE_SHARP, ChromaticNote.B), is(both(
                greaterThan(-10)).and(lessThan(0))));
    }

    @Test
    public void Accidental_BY_DISTANCE_equal() {
        assertThat(ChromaticNote.Accidental.BY_DISTANCE.compare(ChromaticNote.Accidental.DOUBLE_FLAT, ChromaticNote.Accidental.DOUBLE_FLAT), is(0));
    }

    @Test
    public void Accidental_BY_DISTANCE_double_sharp_natural() {
        assertThat(ChromaticNote.Accidental.BY_DISTANCE.compare(ChromaticNote.Accidental.DOUBLE_SHARP, ChromaticNote.Accidental.NATURAL), is(2));
    }

    @Test
    public void Accidental_BY_DISTANCE_max_distance() {
        assertThat(ChromaticNote.Accidental.BY_DISTANCE.compare(ChromaticNote.Accidental.QUADRUPLE_FLAT, ChromaticNote.Accidental.QUADRUPLE_SHARP), is(-8));
    }

    @Test
    public void semitonesTo_equal() {
        assertThat(ChromaticNote.B_FLAT.semitonesTo(ChromaticNote.B_FLAT), is(0));
    }

    @Test
    public void semitonesTo_bigger() {
        assertThat(ChromaticNote.G.semitonesTo(ChromaticNote.F), is(-2));
    }

    @Test
    public void semitonesTo_smaller() {
        assertThat(ChromaticNote.C.semitonesTo(ChromaticNote.F_SHARP), is(6));
    }

    @Test
    public void semitonesTo_smaller_over_boundary() {
        assertThat(ChromaticNote.G.semitonesTo(ChromaticNote.A), is(-10));
    }

    @Test
    public void semitonesUpTo_equal() {
        assertThat(ChromaticNote.B_FLAT.semitonesUpTo(ChromaticNote.B_FLAT), is(0));
    }

    @Test
    public void semitonesUpTo_bigger() {
        assertThat(ChromaticNote.G.semitonesUpTo(ChromaticNote.F), is(10));
    }

    @Test
    public void semitonesUpTo_smaller() {
        assertThat(ChromaticNote.C.semitonesUpTo(ChromaticNote.F_SHARP), is(6));
    }

    @Test
    public void semitonesDownTo_equal() {
        assertThat(ChromaticNote.B_FLAT.semitonesDownTo(ChromaticNote.B_FLAT), is(0));
    }

    @Test
    public void semitonesDownTo_bigger() {
        assertThat(ChromaticNote.B_FLAT.semitonesDownTo(ChromaticNote.A), is(1));
    }

    @Test
    public void semitonesDownTo_smaller() {
        assertThat(ChromaticNote.F_SHARP.semitonesDownTo(ChromaticNote.G), is(11));
    }

    @Test
    public void semitonesDownTo_extreme() {
        assertThat(ChromaticNote.G_SHARP.semitonesDownTo(ChromaticNote.A), is(11));
    }

    @Test
    public void withSemitones_trivial() {
        assertThat(ChromaticNote.A.withSemitones(0, ChromaticNote.Accidental.NATURAL), is(ChromaticNote.A));
    }

    @Test
    public void withSemitones_1_sharp() {
        assertThat(ChromaticNote.A.withSemitones(1, ChromaticNote.Accidental.SHARP), is(ChromaticNote.A_SHARP));
    }

    @Test
    public void withSemitones_1_flat() {
        assertThat(ChromaticNote.A.withSemitones(1, ChromaticNote.Accidental.FLAT), is(ChromaticNote.B_FLAT));
    }

    @Test
    public void withSemitones_no_1_flat() {
        assertThrows(IllegalArgumentException.class, () -> ChromaticNote.A.withSemitones(1, ChromaticNote.Accidental.NATURAL));
    }

    @Test
    public void withSemitones_2_double_sharp() {
        assertThat(ChromaticNote.A.withSemitones(2, ChromaticNote.Accidental.DOUBLE_SHARP), is(ChromaticNote.A_DOUBLE_SHARP));
    }

    @Test
    public void withSemitones_2_natural() {
        assertThat(ChromaticNote.A.withSemitones(2, ChromaticNote.Accidental.NATURAL), is(ChromaticNote.B));
    }

    @Test
    public void _BY_ACCIDENTAL_ONLY_quadruple_sharp_smaller_than_natural() {
        assertThat(ChromaticNote._BY_ACCIDENTAL_ONLY.compare(ChromaticNote.F_QUADRUPLE_SHARP, ChromaticNote.A), is(-4));
    }

    @Test
    public void _BY_ACCIDENTAL_ONLY_natural_smaller_than_double_flat() {
        assertThat(ChromaticNote._BY_ACCIDENTAL_ONLY.compare(ChromaticNote.A, ChromaticNote.B_DOUBLE_FLAT), is(-2));
    }

    @Test
    public void _BY_ACCIDENTAL_ONLY_natural_bigger_than_double_sharp() {
        assertThat(ChromaticNote._BY_ACCIDENTAL_ONLY.compare(ChromaticNote.A, ChromaticNote.G_DOUBLE_SHARP), is(2));
    }

    @Test
    public void _BY_ACCIDENTAL_ONLY_double_sharp_bigger_than_quadruple_sharp() {
        assertThat(ChromaticNote._BY_ACCIDENTAL_ONLY.compare(ChromaticNote.G_DOUBLE_SHARP, ChromaticNote.F_QUADRUPLE_SHARP), is(2));
    }

    @Test
    public void _BY_ACCIDENTAL_ONLY_exception_on_unequal_pitch() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ChromaticNote._BY_ACCIDENTAL_ONLY.compare(ChromaticNote.C, ChromaticNote.C_SHARP));
    }

    @Test // extreme white box test
    public void fromSemitonesFromA_order_0() {
        ChromaticNote.fromSemitonesFromA(0, ChromaticNote.Accidental.NATURAL, ChromaticNote.AccidentalPreference.FLATTER);
        ChromaticNote pitchedAs_A[] = ChromaticNote.FROM_A[0];
        assertThat(pitchedAs_A[0], is(ChromaticNote.F_QUADRUPLE_SHARP));
        assertThat(pitchedAs_A[1], is(nullValue()));
        assertThat(pitchedAs_A[2], is(ChromaticNote.G_DOUBLE_SHARP));
        assertThat(pitchedAs_A[3], is(nullValue()));
        assertThat(pitchedAs_A[4], is(ChromaticNote.A));
        assertThat(pitchedAs_A[5], is(nullValue()));
        assertThat(pitchedAs_A[6], is(ChromaticNote.B_DOUBLE_FLAT));
        assertThat(pitchedAs_A[7], is(ChromaticNote.C_TRIPLE_FLAT));
        assertThat(pitchedAs_A[8], is(nullValue()));
    }

    @Test // extreme white box test
    public void fromSemitonesFromA_order_1() {
        ChromaticNote.fromSemitonesFromA(0, ChromaticNote.Accidental.NATURAL, ChromaticNote.AccidentalPreference.FLATTER);
        ChromaticNote pitchedAs_A[] = ChromaticNote.FROM_A[1];
        assertThat(pitchedAs_A[0], is(nullValue()));
        assertThat(pitchedAs_A[1], is(ChromaticNote.G_TRIPLE_SHARP));
        assertThat(pitchedAs_A[2], is(nullValue()));
        assertThat(pitchedAs_A[3], is(ChromaticNote.A_SHARP));
        assertThat(pitchedAs_A[4], is(nullValue()));
        assertThat(pitchedAs_A[5], is(ChromaticNote.B_FLAT));
        assertThat(pitchedAs_A[6], is(ChromaticNote.C_DOUBLE_FLAT));
        assertThat(pitchedAs_A[7], is(nullValue()));
        assertThat(pitchedAs_A[8], is(ChromaticNote.D_QUADRUPLE_FLAT));
    }

    @Test // extreme white box test
    public void fromSemitonesFromA_order_2() {
        ChromaticNote.fromSemitonesFromA(0, ChromaticNote.Accidental.NATURAL, ChromaticNote.AccidentalPreference.FLATTER);
        ChromaticNote pitchedAs_A[] = ChromaticNote.FROM_A[2];
        assertThat(pitchedAs_A[0], is(ChromaticNote.G_QUADRUPLE_SHARP));
        assertThat(pitchedAs_A[1], is(nullValue()));
        assertThat(pitchedAs_A[2], is(ChromaticNote.A_DOUBLE_SHARP));
        assertThat(pitchedAs_A[3], is(nullValue()));
        assertThat(pitchedAs_A[4], is(ChromaticNote.B));
        assertThat(pitchedAs_A[5], is(ChromaticNote.C_FLAT));
        assertThat(pitchedAs_A[6], is(nullValue()));
        assertThat(pitchedAs_A[7], is(ChromaticNote.D_TRIPLE_FLAT));
        assertThat(pitchedAs_A[8], is(nullValue()));
    }

    @Test
    public void fromSemitonesFromA_order_5() {
        ChromaticNote.fromSemitonesFromA(0, ChromaticNote.Accidental.NATURAL, ChromaticNote.AccidentalPreference.FLATTER);
        ChromaticNote pitchedAs_A[] = ChromaticNote.FROM_A[5];
        assertThat(pitchedAs_A[0], is(nullValue()));
        assertThat(pitchedAs_A[1], is(ChromaticNote.B_TRIPLE_SHARP));
        assertThat(pitchedAs_A[2], is(ChromaticNote.C_DOUBLE_SHARP));
        assertThat(pitchedAs_A[0], is(nullValue()));
        assertThat(pitchedAs_A[4], is(ChromaticNote.D));
        assertThat(pitchedAs_A[5], is(nullValue()));
        assertThat(pitchedAs_A[6], is(ChromaticNote.E_DOUBLE_FLAT));
        assertThat(pitchedAs_A[7], is(ChromaticNote.F_TRIPLE_FLAT));
        assertThat(pitchedAs_A[8], is(nullValue()));
    }

    @Test
    public void fromSemitonesFromA_sharp() {
        ChromaticNote.fromSemitonesFromA(0, ChromaticNote.Accidental.SHARP, ChromaticNote.AccidentalPreference.SHARPER);
        for (int i = 0; i < 11; i++) {
            ChromaticNote found = ChromaticNote.A.fromSemitonesFromA(i, ChromaticNote.Accidental.NATURAL, ChromaticNote.AccidentalPreference.SHARPER);
            assertThat(found.semitonesDownTo(ChromaticNote.A), is(i));
        }
    }

    @Test
    public void fromSemitonesFromA_flat() {
        ChromaticNote.fromSemitonesFromA(0, ChromaticNote.Accidental.SHARP, ChromaticNote.AccidentalPreference.SHARPER);
        for (int i = 0; i < 11; i++) {
            ChromaticNote found = ChromaticNote.A.fromSemitonesFromA(i, ChromaticNote.Accidental.NATURAL, ChromaticNote.AccidentalPreference.FLATTER);
            assertThat(found.semitonesDownTo(ChromaticNote.A), is(i));
        }
    }

}
