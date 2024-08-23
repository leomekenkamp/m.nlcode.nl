package nl.nlcode.m.engine;

import javax.sound.midi.ShortMessage;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 *
 * @author leo
 */
public class EqualToShortMessage extends TypeSafeDiagnosingMatcher<ShortMessage> {

    private Integer command;
    private Integer channel;
    private Integer data1;
    private Integer data2;
    
    public EqualToShortMessage(ShortMessage expected) {
        this(expected.getCommand(), expected.getChannel(), expected.getData1(), expected.getData2());
    }
    
    public EqualToShortMessage(Integer command, Integer channel, Integer data1, Integer data2) {
        this.command = command;
        this.channel = channel;
        this.data1 = data1;
        this.data2 = data2;
    }
    
    @Override
    public void describeTo(Description description) {
        if (command != null) {
            description.appendText(" command ").appendValue(command);
        }
        if (channel != null) {
            description.appendText(" channel ").appendValue(channel);
        }
        if (data1 != null) {
            description.appendText(" data1 ").appendValue(data1);
        }
        if (data2 != null) {
            description.appendText(" data2 ").appendValue(data2);
        }
    }

    @Override
    protected boolean matchesSafely(ShortMessage actual, Description description) {
        boolean result = true;
        if (command != null && command != actual.getCommand()) {
            result = false;
            description.appendText(" command ")
               .appendValue(actual.getCommand());
        }
        if (channel != null && channel != actual.getChannel()) {
            result = false;
            description.appendText(" channel ")
               .appendValue(actual.getChannel());
        }
        if (data1 != null && data1 != actual.getData1()) {
            result = false;
            description.appendText(" data1 ")
               .appendValue(actual.getData1());
        }
        if (data2 != null && data2 != actual.getData2()) {
            result = false;
            description.appendText(" data2 ")
               .appendValue(actual.getData2());
        }
       return result;
    }
    
    public static EqualToShortMessage equalTo(ShortMessage expected) {
        return new EqualToShortMessage(expected);
    }
    
    public static EqualToShortMessage equalToIgnoreData2(ShortMessage expected) {
        return new EqualToShortMessage(expected.getCommand(), expected.getChannel(), expected.getData1(), null);
    }

    public static EqualToShortMessage equalToButData2(ShortMessage expected, int data2) {
        return new EqualToShortMessage(expected.getCommand(), expected.getChannel(), expected.getData1(), data2);
    }

    public static EqualToShortMessage equalToIgnoreChannel(ShortMessage expected) {
        return new EqualToShortMessage(expected.getCommand(), null, expected.getData1(), expected.getData2());
    }
    
    public static EqualToShortMessage equalToButChannel(ShortMessage expected, int channel) {
        return new EqualToShortMessage(expected.getCommand(), channel, expected.getData1(), expected.getData2());
    }
    
}
