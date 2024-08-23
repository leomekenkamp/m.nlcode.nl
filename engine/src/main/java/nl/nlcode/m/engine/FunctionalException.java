package nl.nlcode.m.engine;

/**
 *
 * @author leo
 */
public class FunctionalException extends RuntimeException {
    
    public FunctionalException() {
    }

    public FunctionalException(Throwable cause) {
        super(cause);
    }
    
    public FunctionalException(String message) {
        super(message);
    }
    
    public FunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
