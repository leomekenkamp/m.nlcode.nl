package nl.nlcode.m.engine;

import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.EqualToShortMessage.equalTo;
import static nl.nlcode.m.engine.EqualToShortMessage.equalToButData2;
import static nl.nlcode.m.engine.EqualToShortMessage.equalToIgnoreData2;
import static nl.nlcode.m.engine.MidiInOut.MIDI_VELOCITY_MAX;
import static nl.nlcode.m.engine.MidiInOut.createShortMessage;
import static nl.nlcode.m.engine.IsMidiClock.isMidiClock;
import nl.nlcode.m.ui.EnglishShortNoteStringConverter;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author leo
 */
public class EchoTest extends DefaultMidiInOutTest<Echo> {

    private EnglishShortNoteStringConverter nc = new EnglishShortNoteStringConverter();

    @Override
    protected Echo createInstance() {
        return new Echo();
    }

    @Test
    public void trivial_without_any_notes() {
        instance.setTickSource(TickSource.MIDI);
        instance.setAbsoluteVelocityDecrease(10);
        instance.setEchoLength(3);
        instance.setRelativeVelocityDecrease(0);

        defaultTestIn.processReceiveMidiClock(10);

        settle();
        for (int i = 0; i < 10; i++) {
            assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        }
        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

    /**
     * note on 127 | clock | note off | clock | clock | echo1 on 117 | clock | echo1 off | clock |
     * clock | echo 2 on 107 | clock | echo 2 off | clock | clock | etc...
     */
    @Test
    public void trivial_note_with_note_off_within_echo_length() {
        instance.setTickSource(TickSource.MIDI);
        instance.setAbsoluteVelocityDecrease(10);
        instance.setEchoLength(3);
        instance.setRelativeVelocityDecrease(0);

        ShortMessage noteOn1 = createShortMessage(ShortMessage.NOTE_ON, 0, nc.fromString("C4"), 31);
        ShortMessage noteOff1 = createShortMessage(ShortMessage.NOTE_OFF, noteOn1.getChannel(), noteOn1.getData1(), 15);
        defaultTestIn.processReceive(noteOn1);
        defaultTestIn.processReceiveMidiClock();
        defaultTestIn.processReceive(noteOff1);
        defaultTestIn.processReceiveMidiClock(12);

        settle();

        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOn1)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOff1)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOn1, 21)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOff1, 5)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOn1, 11)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOff1, 1)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOn1, 1)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOff1, 1)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

    /**
     * note on 127 | clock | clock | clock | echo1 on 117 | clock | 117 note off | clock | clock |
     * echo 2 on 107 | clock | clock | clock | echo 2 on 97 | etc...
     */
    @Test
    public void trivial_note_with_note_off_outside_echo_length() {
        instance.setTickSource(TickSource.MIDI);
        instance.setAbsoluteVelocityDecrease(20);
        instance.setEchoLength(3);
        instance.setRelativeVelocityDecrease(0);

        ShortMessage noteOn1 = createShortMessage(ShortMessage.NOTE_ON, 0, nc.fromString("C4"), 125);
        ShortMessage noteOff1 = createShortMessage(ShortMessage.NOTE_OFF, noteOn1.getChannel(), noteOn1.getData1(), 127);
        defaultTestIn.processReceive(noteOn1);
        defaultTestIn.processReceiveMidiClock(4);
        defaultTestIn.processReceive(noteOff1);
        defaultTestIn.processReceiveMidiClock(19);

        settle();

        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOn1)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOn1, 105)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOn1, 85)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOn1, 65)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOn1, 45)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOn1, 25)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOn1, 5)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalToButData2(noteOff1, 7)));
        assertThat(defaultTestOut.removeFifoShortMessage(), isMidiClock());
        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

}
