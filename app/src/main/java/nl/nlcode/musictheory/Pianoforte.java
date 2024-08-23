package nl.nlcode.musictheory;

/**
 *
 * @author jq59bu
 */
public class Pianoforte extends Instrument {

    private static final Pianoforte INSTANCE = new Pianoforte();
    
    private Pianoforte() {
        super(new SpnNote(ChromaticNote.A, 0), new SpnNote(ChromaticNote.C, 8));
    }
    
    public static Pianoforte getInstance() {
        return INSTANCE;
    }
    
}
