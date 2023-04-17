package nl.nlcode.m.engine;

import javax.sound.midi.ShortMessage;

/**
 *
 * @author leo
 */
public class ExceptChannelEqualToShortMessage extends EqualToShortMessage {
    
    public ExceptChannelEqualToShortMessage(ShortMessage actual) {
        super(actual, true, false, true, true);
    }
    
    public static ExceptChannelEqualToShortMessage exceptChannelEqualTo(ShortMessage actual) {
        return new ExceptChannelEqualToShortMessage(actual);
    }
    
}
