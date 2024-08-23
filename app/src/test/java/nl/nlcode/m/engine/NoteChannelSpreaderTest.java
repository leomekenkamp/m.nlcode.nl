package nl.nlcode.m.engine;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.EqualToShortMessage.equalTo;
import static nl.nlcode.m.engine.EqualToShortMessage.equalToButChannel;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class NoteChannelSpreaderTest extends DefaultMidiInOutTest<NoteChannelSpreader> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected NoteChannelSpreader createInstance() {
        return new NoteChannelSpreader();
    }

    @Test
    public void default_settings() throws InvalidMidiDataException {
        assertThat(instance.isOutputChannel(0), is(true));
        assertThat(instance.isOutputChannel(1), is(false));
        assertThat(instance.isOutputChannel(2), is(false));
        assertThat(instance.isOutputChannel(3), is(false));
        assertThat(instance.isOutputChannel(4), is(false));
        assertThat(instance.isOutputChannel(5), is(false));
        assertThat(instance.isOutputChannel(6), is(false));
        assertThat(instance.isOutputChannel(7), is(false));
        assertThat(instance.isOutputChannel(8), is(false));
        assertThat(instance.isOutputChannel(9), is(false));
        assertThat(instance.isOutputChannel(10), is(false));
        assertThat(instance.isOutputChannel(11), is(false));
        assertThat(instance.isOutputChannel(12), is(false));
        assertThat(instance.isOutputChannel(13), is(false));
        assertThat(instance.isOutputChannel(14), is(false));
        assertThat(instance.isOutputChannel(15), is(false));
        assertThat(instance.getInputChannel(), is(0));
    }

    @Test
    public void trivial_empty() throws InvalidMidiDataException {
        instance.setOutputChannel(0, false);
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

    @Test
    public void ignore_key_msg_from_non_input_channel() throws InvalidMidiDataException {
        instance.setInputChannel(4);
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 1, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 2, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 6, 61, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 6, 61, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

    @Test
    public void input_channel() throws InvalidMidiDataException {
        instance.setOutputChannel(0, false);
        instance.setOutputChannel(6, true);
        instance.setInputChannel(10);
        ShortMessage noteOn1 = new ShortMessage(ShortMessage.NOTE_ON, 10, 60, 127);
        ShortMessage noteOff1 = new ShortMessage(ShortMessage.NOTE_OFF, 10, 60, 127);
        ShortMessage noteOn2 = new ShortMessage(ShortMessage.NOTE_ON, 10, 61, 127);
        ShortMessage noteOff2 = new ShortMessage(ShortMessage.NOTE_OFF, 10, 61, 127);

        defaultTestIn.processReceive(noteOn1);
        defaultTestIn.processReceive(noteOff1);
        defaultTestIn.processReceive(noteOn2);
        defaultTestIn.processReceive(noteOff2);

        settle();

        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOn1, 6)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOff1, 6)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOn2, 6)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOff2, 6)));
        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

    @Nested
    public class round_robin_next_output_channel {

        @Test
        public void from_no_channels_to_multiple() throws InvalidMidiDataException, IOException {
            instance.setOutputChannel(0, false);
            assertThat(instance.nextChannelForNoteOn(), is(nullValue()));
            assertThat(instance.nextChannelForNoteOn(), is(nullValue()));
            assertThat(instance.nextChannelForNoteOn(), is(nullValue()));
            
            instance.setOutputChannel(5, true);
            assertThat(instance.nextChannelForNoteOn(), is(5));
            assertThat(instance.nextChannelForNoteOn(), is(5));
            assertThat(instance.nextChannelForNoteOn(), is(5));

            instance.setOutputChannel(6, true);
            assertThat(instance.nextChannelForNoteOn(), is(6));
            assertThat(instance.nextChannelForNoteOn(), is(5));
            assertThat(instance.nextChannelForNoteOn(), is(6));

            instance.setOutputChannel(8, true);
            assertThat(instance.nextChannelForNoteOn(), is(8));
            assertThat(instance.nextChannelForNoteOn(), is(5));
            assertThat(instance.nextChannelForNoteOn(), is(6));
            assertThat(instance.nextChannelForNoteOn(), is(8));
            assertThat(instance.nextChannelForNoteOn(), is(5));
            assertThat(instance.nextChannelForNoteOn(), is(6));

        }

        @Test
        public void from_multiple_channels_to_zero() throws InvalidMidiDataException, IOException {
            instance.setOutputChannel(0, false);
            instance.setOutputChannel(3, true);
            instance.setOutputChannel(14, true);
            instance.setOutputChannel(15, true);

            assertThat(instance.nextChannelForNoteOn(), is(3));
            assertThat(instance.nextChannelForNoteOn(), is(14));
            assertThat(instance.nextChannelForNoteOn(), is(15));
            assertThat(instance.nextChannelForNoteOn(), is(3));
            assertThat(instance.nextChannelForNoteOn(), is(14));
            assertThat(instance.nextChannelForNoteOn(), is(15));

            instance.setOutputChannel(15, false);
            assertThat(instance.nextChannelForNoteOn(), is(3));
            assertThat(instance.nextChannelForNoteOn(), is(14));
            assertThat(instance.nextChannelForNoteOn(), is(3));
            assertThat(instance.nextChannelForNoteOn(), is(14));

            instance.setOutputChannel(3, false);
            assertThat(instance.nextChannelForNoteOn(), is(14));
            assertThat(instance.nextChannelForNoteOn(), is(14));
            
            instance.setOutputChannel(14, false);
            assertThat(instance.nextChannelForNoteOn(), is(nullValue()));
            assertThat(instance.nextChannelForNoteOn(), is(nullValue()));

        }
    }

    @Test
    public void one_output_channel() throws InvalidMidiDataException, IOException {
        ShortMessage noteOn1 = new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 127);
        ShortMessage noteOff1 = new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 100);
        ShortMessage noteOn2 = new ShortMessage(ShortMessage.NOTE_ON, 0, 61, 66);
        ShortMessage noteOff2 = new ShortMessage(ShortMessage.NOTE_OFF, 0, 61, 70);

        defaultTestIn.processReceive(noteOn1);
        defaultTestIn.processReceive(noteOff1);
        defaultTestIn.processReceive(noteOn2);
        defaultTestIn.processReceive(noteOff2);

        settle(500);

        assertThat(instance.getInputChannel(), is(0));
        assertThat(instance.isOutputChannel(0), is(true));

        assertThat(defaultTestOut.receivedBufferCount(), is(4));
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(noteOn1)));
        assertThat(defaultTestOut.receivedShort(1), is(equalTo(noteOff1)));
        assertThat(defaultTestOut.receivedShort(2), is(equalTo(noteOn2)));
        assertThat(defaultTestOut.receivedShort(3), is(equalTo(noteOff2)));
               
     }

    @Test
    public void two_output_channels() throws InvalidMidiDataException {
        instance.setOutputChannel(4, true);
        ShortMessage noteOn1 = new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 127);
        ShortMessage noteOff1 = new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 100);
        ShortMessage noteOn2 = new ShortMessage(ShortMessage.NOTE_ON, 0, 61, 66);
        ShortMessage noteOff2 = new ShortMessage(ShortMessage.NOTE_OFF, 0, 61, 70);
        ShortMessage noteOn3 = new ShortMessage(ShortMessage.NOTE_ON, 0, 62, 110);
        ShortMessage noteOff3 = new ShortMessage(ShortMessage.NOTE_OFF, 0, 62, 4);
        ShortMessage noteOn4 = new ShortMessage(ShortMessage.NOTE_ON, 0, 63, 44);
        ShortMessage noteOff4 = new ShortMessage(ShortMessage.NOTE_OFF, 0, 63, 20);

        defaultTestIn.processReceive(noteOn1);
        defaultTestIn.processReceive(noteOff1);
        defaultTestIn.processReceive(noteOn2);
        defaultTestIn.processReceive(noteOff2);
        defaultTestIn.processReceive(noteOn3);
        defaultTestIn.processReceive(noteOff3);
        defaultTestIn.processReceive(noteOn4);
        defaultTestIn.processReceive(noteOff4);

        settle();

        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOn1)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOff1)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOn2, 4)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOff2, 4)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOn3)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOff3)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOn4, 4)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOff4, 4)));
        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

    @Test
    public void two_channels_interwaving_on_and_off() throws InvalidMidiDataException {
        instance.setOutputChannel(3, true);
        ShortMessage noteOn1 = new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 127);
        ShortMessage noteOff1 = new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 100);
        ShortMessage noteOn2 = new ShortMessage(ShortMessage.NOTE_ON, 0, 61, 66);
        ShortMessage noteOff2 = new ShortMessage(ShortMessage.NOTE_OFF, 0, 61, 70);
        ShortMessage noteOn3 = new ShortMessage(ShortMessage.NOTE_ON, 0, 62, 110);
        ShortMessage noteOff3 = new ShortMessage(ShortMessage.NOTE_OFF, 0, 62, 4);
        ShortMessage noteOn4 = new ShortMessage(ShortMessage.NOTE_ON, 0, 63, 44);
        ShortMessage noteOff4 = new ShortMessage(ShortMessage.NOTE_OFF, 0, 63, 20);

        defaultTestIn.processReceive(noteOn1);
        defaultTestIn.processReceive(noteOn2);
        defaultTestIn.processReceive(noteOn3);
        defaultTestIn.processReceive(noteOff3);
        defaultTestIn.processReceive(noteOn4);
        defaultTestIn.processReceive(noteOff4);
        defaultTestIn.processReceive(noteOff2);
        defaultTestIn.processReceive(noteOff1);

        settle();

        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOn1)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOn2, 3)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOn3)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOff3)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOn4, 3)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOff4, 3)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOff2, 3)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOff1)));
        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

    @Test
    public void persistence() throws IOException, InvalidMidiDataException {
        instance.setInputChannel(5);
        instance.setOutputChannel(0, false);
        instance.setOutputChannel(3, true);
        instance.setOutputChannel(7, true);

        persistAndLoad(true);

        ShortMessage noteOn1 = new ShortMessage(ShortMessage.NOTE_ON, 5, 60, 127);
        ShortMessage noteOff1 = new ShortMessage(ShortMessage.NOTE_OFF, 5, 60, 127);
        ShortMessage noteOn2 = new ShortMessage(ShortMessage.NOTE_ON, 5, 61, 127);
        ShortMessage noteOff2 = new ShortMessage(ShortMessage.NOTE_OFF, 5, 61, 127);

        defaultTestIn.processReceive(noteOn1);
        defaultTestIn.processReceive(noteOff1);
        defaultTestIn.processReceive(noteOn2);
        defaultTestIn.processReceive(noteOff2);

        settle();

        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOn1, 3)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOff1, 3)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOn2, 7)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButChannel(noteOff2, 7)));
        assertThat(defaultTestOut.receivedBufferCount(), is(0));

    }
}
