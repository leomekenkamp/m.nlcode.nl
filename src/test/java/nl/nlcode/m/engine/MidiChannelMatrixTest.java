package nl.nlcode.m.engine;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static nl.nlcode.m.engine.EqualToShortMessage.equalTo;
import static nl.nlcode.m.engine.ExceptChannelEqualToShortMessage.exceptChannelEqualTo;
/**
 *
 * @author leo
 */
public class MidiChannelMatrixTest extends DefaultMidiInOutTest<MidiChannelMatrix> {
        
    @Override
    protected MidiChannelMatrix createInstance() {
        return new MidiChannelMatrix(project);
    }

    /**
     * Test of isActiveReceiver method, of class MidiChannelMatrix.
     */
    @Test
    public void by_default_map_one_on_one() throws InvalidMidiDataException {
        ShortMessage noteOn1 = new ShortMessage(ShortMessage.NOTE_OFF, 0, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOn1);
        ShortMessage noteOff1 = new ShortMessage(ShortMessage.NOTE_OFF, 0, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOff1);
        ShortMessage noteOn4 = new ShortMessage(ShortMessage.NOTE_ON, 4, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOn4);
        ShortMessage noteOff6 = new ShortMessage(ShortMessage.NOTE_OFF, 15, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOff6);
        
        settle();
        
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(noteOn1)));
        assertThat(defaultTestOut.receivedShort(1), is(equalTo(noteOff1)));
        assertThat(defaultTestOut.receivedShort(2), is(equalTo(noteOn4)));
        assertThat(defaultTestOut.receivedShort(3), is(equalTo(noteOff6)));
        assertThat(defaultTestOut.receivedCount(), is(4));
    }

    @Test
    public void no_nothing_when_not_mapped() throws InvalidMidiDataException {
        instance.clearFromTo();
        
        MidiMessage noteOn1 = new ShortMessage(ShortMessage.NOTE_ON, 1, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOn1);
        MidiMessage noteOff1 = new ShortMessage(ShortMessage.NOTE_OFF, 1, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOff1);
        MidiMessage noteOn4 = new ShortMessage(ShortMessage.NOTE_ON, 4, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOn4);
        MidiMessage noteOff6 = new ShortMessage(ShortMessage.NOTE_OFF, 6, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOff6);
        
        settle();

        assertThat(defaultTestOut.receivedCount(), is(0));
    }

    @Test
    public void one_channel_to_multiple_channels() throws InvalidMidiDataException {
        instance.clearFromTo();
        instance.zeroBasedFromTo(1, 0, true);
        instance.zeroBasedFromTo(1, 4, true);
        instance.zeroBasedFromTo(1, 5, true);
        ShortMessage noteOn1 = new ShortMessage(ShortMessage.NOTE_ON, 1, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOn1);
        ShortMessage noteOff1 = new ShortMessage(ShortMessage.NOTE_OFF, 1, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOff1);
        
        settle();
                
        assertThat(defaultTestOut.receivedShort(0), is(exceptChannelEqualTo(noteOn1)));
        assertThat(defaultTestOut.receivedShort(0).getChannel(), is(0));
        assertThat(defaultTestOut.receivedShort(1), is(exceptChannelEqualTo(noteOn1)));
        assertThat(defaultTestOut.receivedShort(1).getChannel(), is(4));
        assertThat(defaultTestOut.receivedShort(2), is(exceptChannelEqualTo(noteOn1)));
        assertThat(defaultTestOut.receivedShort(2).getChannel(), is(5));
        assertThat(defaultTestOut.receivedShort(3), is(exceptChannelEqualTo(noteOff1)));
        assertThat(defaultTestOut.receivedShort(3).getChannel(), is(0));
        assertThat(defaultTestOut.receivedShort(4), is(exceptChannelEqualTo(noteOff1)));
        assertThat(defaultTestOut.receivedShort(4).getChannel(), is(4));
        assertThat(defaultTestOut.receivedShort(5), is(exceptChannelEqualTo(noteOff1)));
        assertThat(defaultTestOut.receivedShort(5).getChannel(), is(5));

        assertThat(defaultTestOut.receivedCount(), is(6));
    }

        @Test
    public void multiple_channels_to_one_channels() throws InvalidMidiDataException {
        instance.clearFromTo();
        instance.zeroBasedFromTo(3, 5, true);
        instance.zeroBasedFromTo(4, 5, true);
        instance.zeroBasedFromTo(5, 5, true);
        ShortMessage noteOn3 = new ShortMessage(ShortMessage.NOTE_ON, 3, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOn3);
        ShortMessage noteOff3 = new ShortMessage(ShortMessage.NOTE_OFF, 3, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOff3);
        ShortMessage noteOn4 = new ShortMessage(ShortMessage.NOTE_ON, 4, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOn4);
        ShortMessage noteOff4 = new ShortMessage(ShortMessage.NOTE_OFF, 4, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOff4);
        ShortMessage noteOn5 = new ShortMessage(ShortMessage.NOTE_ON, 5, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOn5);
        ShortMessage noteOff5 = new ShortMessage(ShortMessage.NOTE_OFF, 5, randomData1(), randomData2());
        defaultTestIn.processReceive(noteOff5);
        
        settle();
                
        assertThat(defaultTestOut.receivedShort(0), is(exceptChannelEqualTo(noteOn3)));
        assertThat(defaultTestOut.receivedShort(0).getChannel(), is(5));
        assertThat(defaultTestOut.receivedShort(1), is(exceptChannelEqualTo(noteOff3)));
        assertThat(defaultTestOut.receivedShort(1).getChannel(), is(5));
        assertThat(defaultTestOut.receivedShort(2), is(exceptChannelEqualTo(noteOn4)));
        assertThat(defaultTestOut.receivedShort(2).getChannel(), is(5));
        assertThat(defaultTestOut.receivedShort(3), is(exceptChannelEqualTo(noteOff4)));
        assertThat(defaultTestOut.receivedShort(3).getChannel(), is(5));
        assertThat(defaultTestOut.receivedShort(4), is(exceptChannelEqualTo(noteOn5)));
        assertThat(defaultTestOut.receivedShort(4).getChannel(), is(5));
        assertThat(defaultTestOut.receivedShort(5), is(exceptChannelEqualTo(noteOff5)));
        assertThat(defaultTestOut.receivedShort(5).getChannel(), is(5));
    }

}
