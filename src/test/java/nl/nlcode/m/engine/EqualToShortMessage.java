package nl.nlcode.m.engine;

import javax.sound.midi.ShortMessage;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 *
 * @author leo
 */
public class EqualToShortMessage extends TypeSafeDiagnosingMatcher<ShortMessage> {

    private ShortMessage actual;
    private boolean command;
    private boolean channel;
    private boolean data1;
    private boolean data2;
    
    public EqualToShortMessage(ShortMessage actual) {
        this(actual, true, true, true, true);
    }
    
    public EqualToShortMessage(ShortMessage actual, boolean command, boolean channel, boolean data1, boolean data2) {
        this.actual = actual;
        this.command = command;
        this.channel = channel;
        this.data1 = data1;
        this.data2 = data2;
    }
    
    @Override
    public void describeTo(Description description) {
        if (command) {
            description.appendText(" command ").appendValue(actual.getCommand());
        }
        if (channel) {
            description.appendText(" channel ").appendValue(actual.getChannel());
        }
        if (data1) {
            description.appendText(" data1 ").appendValue(actual.getChannel());
        }
        if (data2) {
            description.appendText(" data2 ").appendValue(actual.getChannel());
        }
    }

    @Override
    protected boolean matchesSafely(ShortMessage expected, Description description) {
        boolean result = true;
        if (command && actual.getCommand()!= expected.getCommand()) {
            result = false;
            description.appendText(" command ")
               .appendValue(expected.getCommand());
        }
        if (channel && actual.getChannel() != expected.getChannel()) {
            result = false;
            description.appendText(" channel ")
               .appendValue(expected.getChannel());
        }
        if (data1 && actual.getData1() != expected.getData1()) {
            result = false;
            description.appendText(" data1 ")
               .appendValue(expected.getData1());
        }
        if (data2 && actual.getData2() != expected.getData2()) {
            result = false;
            description.appendText(" data2 ")
               .appendValue(expected.getData2());
        }
       return result;
    }
    
    public static EqualToShortMessage equalTo(ShortMessage actual) {
        return new EqualToShortMessage(actual);
    }
    
    public static EqualToShortMessage equalToIgnoreData2(ShortMessage actual) {
        return new EqualToShortMessage(actual, true, true, true, false);
    }
    
}
