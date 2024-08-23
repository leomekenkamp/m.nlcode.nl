package nl.nlcode.m.ui;

/**
 *
 * @author leo
 */
public enum ZeroOrOneBased {
    ZERO_BASED,
    ONE_BASED;
    
    public static ZeroOrOneBased getOneBased(boolean oneBased) {
        return oneBased ? ONE_BASED : ZERO_BASED;
    }

    public static ZeroOrOneBased getZeroBased(boolean zeroBased) {
        return zeroBased ? ZERO_BASED : ONE_BASED;
    }
}
