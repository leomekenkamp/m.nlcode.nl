package nl.nlcode.m.engine;

import javax.sound.midi.ShortMessage;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 *
 * @author leo
 */
public class IsMidiClock<T extends ShortMessage> extends TypeSafeDiagnosingMatcher<T> {

    private static final ShortMessage MIDI_CLOCK = MidiInOut.createMidiClock();
    
    @Override
    public void describeTo(Description description) {
        description.appendText("status 248 (0xF8) as result of a BITWISE OR of getCommand() and getChannel()");
    }

    @Override
    protected boolean matchesSafely(ShortMessage msg, Description description) {
        boolean result = true;
        if (msg.getCommand() != MIDI_CLOCK.getCommand() || msg.getChannel() != MIDI_CLOCK.getChannel()) {
            result = false;
            description.appendText("status was ")
               .appendValue(msg.getCommand() | msg.getChannel());
        }
       return result;
    }
    
    public static <T extends ShortMessage> IsMidiClock<T> isMidiClock() {
        return new IsMidiClock();
    }
    
}
