package nl.nlcode.musictheory;

/**
 *
 * @author jq59bu
 */
public class Organ extends Instrument {

    private static final Organ INSTANCE = new Organ();
    
    private Organ() {
        super(new SpnNote(ChromaticNote.C, 2), new SpnNote(ChromaticNote.C, 7));
    }
    
    public static Organ getInstance() {
        return INSTANCE;
    }
    
}
