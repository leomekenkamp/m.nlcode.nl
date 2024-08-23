package nl.nlcode.m.engine;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.EqualToShortMessage.equalTo;
import nl.nlcode.m.engine.MidiInOut.SendReceiveLoopDetectedException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author leo
 */
public class MidiInOutTest extends DefaultMidiInOutTest<MidiInOut> {

    class MyMidiInOut extends MidiInOut {

        public MyMidiInOut() {
        }

        public boolean isActiveReceiver() {
            return true;
        }

        public boolean isActiveSender() {
            return true;
        }

        public void asyncReceive(MidiMessage midiMessage, long timeStamp) {
            this.send(midiMessage, timeStamp);
        }

    }

    @Override
    protected MidiInOut createInstance() {
        return new MyMidiInOut();
    }

    @Test
    public void can_handle_no_listeners() throws InvalidMidiDataException {
        instance.stopSendingTo(defaultTestOut);

        for (int i = 0; i < 100; i++) {
            defaultTestIn.asyncReceive(randomNoteOn(), randomTimeStamp());
        }
    }

    @Test
    public void can_handle_one_output() throws InvalidMidiDataException {
        for (int i = 0; i < 100; i++) {
            defaultTestIn.asyncReceive(randomNoteOn(), randomTimeStamp());
        }

        settle(150);

        assertThat(defaultTestOut.receivedBufferCount(), is(defaultTestIn.receivedBufferCount()));
        for (int i = 0; i < defaultTestIn.receivedBufferCount(); i++) {
            assertThat("comparing short message " + i, defaultTestOut.receivedShort(i), is(equalTo(defaultTestIn.receivedShort(i))));
            assertThat("comparing timestamp " + i, defaultTestOut.receivedTimeStamp(i), is(defaultTestIn.receivedTimeStamp(i)));
        }
    }

    @Test
    public void can_handle_multiple_outputs() throws InvalidMidiDataException {
        DebugMidiInOut extra1 = new DebugMidiInOut(project);
        DebugMidiInOut extra2 = new DebugMidiInOut(project);
        DebugMidiInOut notConnected = new DebugMidiInOut(project);
        instance.startSendingTo(extra1);
        instance.startSendingTo(extra2);
        try {
            for (int i = 0; i < 100; i++) {
                defaultTestIn.asyncReceive(randomNoteOn(), randomTimeStamp());
            }

            settle(200);

            assertThat(notConnected.receivedBufferCount(), is(0));
            assertThat(defaultTestOut.receivedBufferCount(), is(defaultTestIn.receivedBufferCount()));
            for (int i = 0; i < defaultTestIn.receivedBufferCount(); i++) {
                assertThat("comparing short message for default" + i, defaultTestOut.receivedShort(i), is(equalTo(defaultTestIn.receivedShort(i))));
                assertThat("comparing timestamp for default" + i, defaultTestOut.receivedTimeStamp(i), is(defaultTestIn.receivedTimeStamp(i)));
                assertThat("comparing short message for extra1" + i, extra1.receivedShort(i), is(equalTo(defaultTestIn.receivedShort(i))));
                assertThat("comparing timestamp for extra1" + i, extra1.receivedTimeStamp(i), is(defaultTestIn.receivedTimeStamp(i)));
                assertThat("comparing short message for extra2" + i, extra2.receivedShort(i), is(equalTo(defaultTestIn.receivedShort(i))));
                assertThat("comparing timestamp for extra2" + i, extra2.receivedTimeStamp(i), is(defaultTestIn.receivedTimeStamp(i)));
            }
        } finally {
            instance.stopSendingTo(extra1);
            instance.stopSendingTo(extra2);
            notConnected.close();
            extra1.close();
            extra2.close();
        }
    }
    
    @Test
    public void no_loops_allowed() {
        assertThrows(SendReceiveLoopDetectedException.class, () -> defaultTestOut.startSendingTo(defaultTestIn));
    }

    @Test
    public void no_self_loop_allowed() {
        assertThrows(SendReceiveLoopDetectedException.class, () -> instance.startSendingTo(instance));
    }

    @Test
    public void test_createTimingClock() {
        ShortMessage timingClock = MidiInOut.createTimingClock();
        assertThat(timingClock.getLength(), is(1));
        assertThat(timingClock.getStatus(), is(ShortMessage.TIMING_CLOCK));
        assertThat(MidiInOut.isTimingClock(timingClock), is(true));
    }
    
    public class MidiInOutImpl extends MidiInOut {
    }

}
