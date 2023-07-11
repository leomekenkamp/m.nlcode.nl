package nl.nlcode.m.engine;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.EqualToShortMessage.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author leo
 */
public class NoteHolderTest extends DefaultMidiInOutTest<NoteHolder> {

    @Override
    protected NoteHolder createInstance() {
        return new NoteHolder();
    }

    @Test
    public void trivial_one_note_on_off() throws InvalidMidiDataException {
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(1));
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(defaultTestIn.receivedShort(0))));
    }

    @Test
    public void trivial_one_note_toggle() throws InvalidMidiDataException {
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(1));
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(defaultTestIn.receivedShort(0))));

        clearBuffers();
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(1));
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(defaultTestIn.receivedShort(0))));
    }

    @Test
    public void chord_toggle_one_off() throws InvalidMidiDataException {
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 64, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 67, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 64, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 67, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(3));
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(defaultTestIn.receivedShort(0))));
        assertThat(defaultTestOut.receivedShort(0).getCommand(), is(ShortMessage.NOTE_ON));
        assertThat(defaultTestOut.receivedShort(0).getData1(), is(60));
        assertThat(defaultTestOut.receivedShort(1).getCommand(), is(ShortMessage.NOTE_ON));
        assertThat(defaultTestOut.receivedShort(1).getData1(), is(64));
        assertThat(defaultTestOut.receivedShort(2).getCommand(), is(ShortMessage.NOTE_ON));
        assertThat(defaultTestOut.receivedShort(2).getData1(), is(67));

        clearBuffers();

        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 64, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(0));

        clearBuffers();

        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 64, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(3));  // TODO which three
        assertThat(defaultTestOut.receivedShort(0).getCommand(), is(ShortMessage.NOTE_OFF));
        assertThat(defaultTestOut.receivedShort(0).getData1(), is(64));
        assertThat(defaultTestOut.receivedShort(1).getCommand(), is(ShortMessage.NOTE_OFF));
        assertThat(defaultTestOut.receivedShort(1).getData1(), is(60));
        assertThat(defaultTestOut.receivedShort(2).getCommand(), is(ShortMessage.NOTE_OFF));
        assertThat(defaultTestOut.receivedShort(2).getData1(), is(67));
    }

    @Test
    public void chord_toggle_other_key() throws InvalidMidiDataException {
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 64, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 67, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 64, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 67, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(3));
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(defaultTestIn.receivedShort(0))));
        assertThat(defaultTestOut.receivedShort(0).getCommand(), is(ShortMessage.NOTE_ON));
        assertThat(defaultTestOut.receivedShort(0).getData1(), is(60));
        assertThat(defaultTestOut.receivedShort(1).getCommand(), is(ShortMessage.NOTE_ON));
        assertThat(defaultTestOut.receivedShort(1).getData1(), is(64));
        assertThat(defaultTestOut.receivedShort(2).getCommand(), is(ShortMessage.NOTE_ON));
        assertThat(defaultTestOut.receivedShort(2).getData1(), is(67));

        clearBuffers();

        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 80, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(4));
        assertThat(defaultTestOut.receivedShort(0).getCommand(), is(ShortMessage.NOTE_OFF));
        assertThat(defaultTestOut.receivedShort(0).getData1(), is(60));
        assertThat(defaultTestOut.receivedShort(1).getCommand(), is(ShortMessage.NOTE_OFF));
        assertThat(defaultTestOut.receivedShort(1).getData1(), is(64));
        assertThat(defaultTestOut.receivedShort(2).getCommand(), is(ShortMessage.NOTE_OFF));
        assertThat(defaultTestOut.receivedShort(2).getData1(), is(67));
        assertThat(defaultTestOut.receivedShort(3).getCommand(), is(ShortMessage.NOTE_ON));
        assertThat(defaultTestOut.receivedShort(3).getData1(), is(80));

        clearBuffers();

        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 80, randomData2()));

        settle();
        assertThat(defaultTestOut.receivedBufferCount(), is(0));

    }

    @Test
    public void chord_overlapping_chord_new_key_first() throws InvalidMidiDataException {
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 64, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 67, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 64, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 67, randomData2()));

        settle();
        clearBuffers();

        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 63, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(4));
        assertThat(defaultTestOut.receivedShort(0).getCommand(), is(ShortMessage.NOTE_OFF));
        assertThat(defaultTestOut.receivedShort(0).getData1(), is(60));
        assertThat(defaultTestOut.receivedShort(1).getCommand(), is(ShortMessage.NOTE_OFF));
        assertThat(defaultTestOut.receivedShort(1).getData1(), is(64));
        assertThat(defaultTestOut.receivedShort(2).getCommand(), is(ShortMessage.NOTE_OFF));
        assertThat(defaultTestOut.receivedShort(2).getData1(), is(67));
        assertThat(defaultTestOut.receivedShort(3), is(equalTo(defaultTestIn.receivedShort(0))));

        clearBuffers();

        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 67, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(2));
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(defaultTestIn.receivedShort(0))));
        assertThat(defaultTestOut.receivedShort(1), is(equalTo(defaultTestIn.receivedShort(1))));

        clearBuffers();

        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 63, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 67, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

    @Test
    public void chord_overlapping_chord_overlapping_key_first() throws InvalidMidiDataException {
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 64, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 67, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 64, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 67, randomData2()));

        settle();
        clearBuffers();

        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(0));

        clearBuffers();

        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 63, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(3));

        assertThat(defaultTestOut.receivedShort(0).getCommand(), is(ShortMessage.NOTE_OFF));
        assertThat(defaultTestOut.receivedShort(0).getData1(), is(64));
        assertThat(defaultTestOut.receivedShort(1).getCommand(), is(ShortMessage.NOTE_OFF));
        assertThat(defaultTestOut.receivedShort(1).getData1(), is(67));
        assertThat(defaultTestOut.receivedShort(2), is(equalTo(defaultTestIn.receivedShort(0))));

        clearBuffers();

        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_ON, 0, 67, randomData2()));

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(1));
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(defaultTestIn.receivedShort(0))));

        clearBuffers();

        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 63, randomData2()));
        defaultTestIn.processReceive(new ShortMessage(ShortMessage.NOTE_OFF, 0, 67, randomData2()));

        settle();
        
        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

}
