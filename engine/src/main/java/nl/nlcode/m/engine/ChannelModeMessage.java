package nl.nlcode.m.engine;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

/**
 * Value for ShortMessage.getData1();
 * 
 * @author leo
 */
public enum ChannelModeMessage {
    
    ALL_SOUND_OFF(0x78),
    RESET_ALL_CONTROLLERS(0x79),
    LOCAL_CONTROL(0x7A),
    ALL_NOTES_OFF(0x7B),
    OMNI_MODE_OFF(0x7C),
    OMNI_MODE_ON(0x7D),
    MONO_MODE_ON(0x7E),
    POLY_MODE_ON(0x7F),
    ;
    
    public final int intValue;
    
    ChannelModeMessage(int intValue) {
        this.intValue = intValue;
    }
    
    public ShortMessage create(int channel) {
        try {
            return new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, intValue, 0);
        } catch (InvalidMidiDataException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
