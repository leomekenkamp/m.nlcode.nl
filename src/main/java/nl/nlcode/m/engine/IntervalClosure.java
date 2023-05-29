package nl.nlcode.m.engine;

/**
 *
 * @author leo
 */
public enum IntervalClosure {
    
    CLOSED(true, true),
    OPEN(false, false),
    LOW_CLOSED_HIGH_OPEN(true, false),
    LOW_OPEN_HIGH_CLOSED(false, true),
    ;
    
    private boolean lowClosed;
    
    private boolean highClosed;
    
    private IntervalClosure(boolean lowClosed, boolean highClosed) {
        this.lowClosed = lowClosed;
        this.highClosed = highClosed;
    }
    
    public boolean isLowClosed() {
        return lowClosed;
    }
    
    public boolean isHighClosed() {
        return highClosed;
    }
    
}
