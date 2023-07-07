package nl.nlcode.m.engine;

import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.EqualToShortMessage.equalTo;
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
        assertThat(defaultTestOut.receivedCount(), is(10));
        for (int i = 0; i < 10; i++) {
            assertThat(defaultTestOut.receivedShort(i), isMidiClock());
        }
    }
    
    /**
     * note on 127 | clock | note off | clock | clock | echo1 on 117 | clock | echo1 off | clock | clock | 
     * echo 2 on 107 | clock | echo 2 off | clock | clock | etc...
     */
    @Test
    public void trivial_note_with_note_off_within_echo_length() {
        instance.setTickSource(TickSource.MIDI);
        instance.setAbsoluteVelocityDecrease(10);
        instance.setEchoLength(3);
        instance.setRelativeVelocityDecrease(0);
        
        ShortMessage noteOn1 = createShortMessage(ShortMessage.NOTE_ON, 0, nc.fromString("C4"), MIDI_VELOCITY_MAX);
        ShortMessage noteOff1 = createShortMessage(ShortMessage.NOTE_OFF, noteOn1.getChannel(), noteOn1.getData1(), randomData2());
        defaultTestIn.processReceive(noteOn1);
        defaultTestIn.processReceiveMidiClock();
        defaultTestIn.processReceive(noteOff1);
        defaultTestIn.processReceiveMidiClock();
        
        settle();
        
        assertThat(defaultTestOut.receivedCount(), is(4));
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(noteOn1)));
        assertThat(defaultTestOut.receivedShort(1), isMidiClock());
        assertThat(defaultTestOut.receivedShort(2), is(equalTo(noteOff1)));
        assertThat(defaultTestOut.receivedShort(3), isMidiClock());
        
        defaultTestOut.clearReceived();
        defaultTestIn.processReceiveMidiClock(3);

        settle();

        assertThat(defaultTestOut.receivedCount(), is(5));
        assertThat(defaultTestOut.receivedShort(0), isMidiClock());
        assertThat(defaultTestOut.receivedShort(1), is(equalToIgnoreData2(noteOn1)));
        assertThat(defaultTestOut.receivedShort(1).getData2(), is(noteOn1.getData2() - instance.getAbsoluteVelocityDecrease()));
        assertThat(defaultTestOut.receivedShort(2), isMidiClock());
        assertThat(defaultTestOut.receivedShort(3), is(equalToIgnoreData2(noteOff1)));
        assertThat(defaultTestOut.receivedShort(3).getData2(), is(noteOff1.getData2() - instance.getAbsoluteVelocityDecrease()));
        assertThat(defaultTestOut.receivedShort(4), isMidiClock());
        
        defaultTestOut.clearReceived();
        defaultTestIn.processReceiveMidiClock(3);

        settle();

   //     assertThat(defaultTestOut.receivedCount(), is(5));q
        assertThat(defaultTestOut.receivedShort(0), isMidiClock());
        assertThat(defaultTestOut.receivedShort(1), is(equalToIgnoreData2(noteOn1)));
        assertThat(defaultTestOut.receivedShort(1).getData2(), is(noteOn1.getData2() - 2 * instance.getAbsoluteVelocityDecrease()));
        assertThat(defaultTestOut.receivedShort(2), isMidiClock());
        assertThat(defaultTestOut.receivedShort(3), is(equalToIgnoreData2(noteOff1)));
        assertThat(defaultTestOut.receivedShort(3).getData2(), is(noteOff1.getData2() - 2 * instance.getAbsoluteVelocityDecrease()));
        assertThat(defaultTestOut.receivedShort(4), isMidiClock());
    }
    /**
     * note on 127 | clock | clock | clock | echo1 on 117 | clock | 117 note off | clock | clock | echo 2 on 107 | 
     * clock | echo1  off | clock | clock | echo 2 on 107 | etc...
     */
    @Test
    public void trivial_note_with_note_off_outside_echo_length() {
        instance.setTickSource(TickSource.MIDI);
        instance.setAbsoluteVelocityDecrease(10);
        instance.setEchoLength(3);
        instance.setRelativeVelocityDecrease(0);
        
        ShortMessage noteOn1 = createShortMessage(ShortMessage.NOTE_ON, 0, nc.fromString("C4"), MIDI_VELOCITY_MAX);
        ShortMessage noteOff1 = createShortMessage(ShortMessage.NOTE_OFF, noteOn1.getChannel(), noteOn1.getData1(), randomData2());
        defaultTestIn.processReceive(noteOn1);
        defaultTestIn.processReceiveMidiClock(4);
        
        settle();
        
        assertThat(defaultTestOut.receivedCount(), is(6));
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(noteOn1)));
        assertThat(defaultTestOut.receivedShort(1), isMidiClock());
        assertThat(defaultTestOut.receivedShort(2), isMidiClock());
        assertThat(defaultTestOut.receivedShort(3), isMidiClock());
        assertThat(defaultTestOut.receivedShort(4), is(equalToIgnoreData2(noteOn1)));
        assertThat(defaultTestOut.receivedShort(4).getData2(), is(noteOn1.getData2() - instance.getAbsoluteVelocityDecrease()));
        assertThat(defaultTestOut.receivedShort(5), isMidiClock());

        defaultTestIn.processReceive(noteOff1);
        defaultTestIn.processReceiveMidiClock(4);

        settle();

        assertThat(defaultTestOut.receivedCount(), is(6));
        assertThat(defaultTestOut.receivedShort(0), is(equalTo(noteOff1)));
        assertThat(defaultTestOut.receivedShort(1), isMidiClock());
        assertThat(defaultTestOut.receivedShort(2), isMidiClock());
        
        defaultTestIn.processReceiveMidiClock();
        
        defaultTestOut.clearReceived();
        defaultTestIn.processReceiveMidiClock(3);

        settle();

        assertThat(defaultTestOut.receivedCount(), is(5));
        assertThat(defaultTestOut.receivedShort(0), isMidiClock());
        assertThat(defaultTestOut.receivedShort(1), is(equalToIgnoreData2(noteOn1)));
        assertThat(defaultTestOut.receivedShort(1).getData2(), is(noteOn1.getData2() - instance.getAbsoluteVelocityDecrease()));
        assertThat(defaultTestOut.receivedShort(2), isMidiClock());
        assertThat(defaultTestOut.receivedShort(3), is(equalToIgnoreData2(noteOff1)));
        assertThat(defaultTestOut.receivedShort(3).getData2(), is(noteOff1.getData2() - instance.getAbsoluteVelocityDecrease()));
        assertThat(defaultTestOut.receivedShort(4), isMidiClock());
        
        defaultTestOut.clearReceived();
        defaultTestIn.processReceiveMidiClock(4);

        settle();

   //     assertThat(defaultTestOut.receivedCount(), is(5));q
        assertThat(defaultTestOut.receivedShort(0), isMidiClock());
        assertThat(defaultTestOut.receivedShort(1), is(equalToIgnoreData2(noteOn1)));
        assertThat(defaultTestOut.receivedShort(1).getData2(), is(noteOn1.getData2() - 2 * instance.getAbsoluteVelocityDecrease()));
        assertThat(defaultTestOut.receivedShort(2), isMidiClock());
        assertThat(defaultTestOut.receivedShort(3), is(equalToIgnoreData2(noteOff1)));
        assertThat(defaultTestOut.receivedShort(3).getData2(), is(noteOff1.getData2() - 2 * instance.getAbsoluteVelocityDecrease()));
        assertThat(defaultTestOut.receivedShort(4), isMidiClock());
    }

    
}
