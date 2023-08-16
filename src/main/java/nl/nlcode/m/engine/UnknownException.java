package nl.nlcode.m.engine;

/**
 *
 * @author leo
 */
public class UnknownException extends IllegalStateException {
    
    public UnknownException(Object unknown) {
        super("unknown " + unknown.getClass().getName() + ": " + unknown);
    }
    
}
