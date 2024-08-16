package nl.nlcode.musictheory;

/**
 *
 * @author jq59bu
 */
public abstract class Instrument {
    
    private final SpnNote lowestNote;
    private final SpnNote highestNote;

    protected Instrument(SpnNote lowestNote, SpnNote highestNote) {
        this.lowestNote = lowestNote;
        this.highestNote = highestNote;
    }
    
    public SpnNote getLowestNote() {
        return lowestNote;
    }

    public SpnNote getHighestNote() {
        return highestNote;
    }
}
