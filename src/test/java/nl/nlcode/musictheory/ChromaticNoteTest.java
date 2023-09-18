package nl.nlcode.musictheory;

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
public class ChromaticNoteTest {
    
    public ChromaticNoteTest() {
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
    public void euclidianModulus_6_mod_5_is_1() {
        assertThat(ChromaticNote.euclidianModulo(6, 5), is(1));
    }
    
    @Test
    public void euclidianModulus_minus2_mod_7_is_5() {
        assertThat(ChromaticNote.euclidianModulo(-2, 7), is(5));
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
        assertThat(ChromaticNote.D.getSharp(), is(ChromaticNote.D_SHARP));
    }

    @Test
    public void sharp_of_B_is_C() {
        assertThat(ChromaticNote.B.getSharp(), is(ChromaticNote.C));
    }

    @Test
    public void sharp_of_Fsharp_is_G() {
        assertThat(ChromaticNote.F_SHARP.getSharp(), is(ChromaticNote.G));
    }
    
    @Test
    public void sharp_of_Dflat_is_D() {
        assertThat(ChromaticNote.D_FLAT.getSharp(), is(ChromaticNote.D));
    }
    
    @Test
    public void flat_of_C_is_B() {
        assertThat(ChromaticNote.C.getFlat(), is(ChromaticNote.B));
    }

    @Test
    public void flat_of_G_is_Gflat() {
        assertThat(ChromaticNote.G.getFlat(), is(ChromaticNote.G_FLAT));
    }

    @Test
    public void flat_of_Eflat_is_D() {
        assertThat(ChromaticNote.E_FLAT.getFlat(), is(ChromaticNote.D));
    }

    @Test
    public void flat_of_Fsharp_is_F() {
        assertThat(ChromaticNote.E_FLAT.getFlat(), is(ChromaticNote.D));
    }
}
