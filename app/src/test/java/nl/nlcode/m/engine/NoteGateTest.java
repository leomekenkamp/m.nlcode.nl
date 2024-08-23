package nl.nlcode.m.engine;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static nl.nlcode.m.engine.EqualToShortMessage.equalTo;

/**
 *
 * @author leo
 */
public class NoteGateTest extends DefaultMidiInOutTest<VelocityGate> {

    @Override
    protected VelocityGate createInstance() {
        return new VelocityGate();
    }

    @Test
    public void by_default_let_all_notes_pass() throws InvalidMidiDataException {
        for (int velocity = 0; velocity <= 127; velocity++) {
            ShortMessage noteOn = new ShortMessage(ShortMessage.NOTE_ON, randomChannel(), randomData1(), randomData2());
            defaultTestIn.processReceive(noteOn);
        }

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(defaultTestIn.receivedBufferCount()));

        for (int velocity = 0; velocity <= 127; velocity++) {
            assertThat(defaultTestOut.receivedShort(velocity), is(equalTo(defaultTestIn.receivedShort(velocity))));
        }
    }

    @Test
    public void pass_no_on_velocity_extremes_and_low_high_reversed() throws InvalidMidiDataException {
        instance.setFromVelocity(127);
        instance.setToVelocity(0);
        instance.setIntervalClosure(IntervalClosure.OPEN);

        for (int velocity = 0; velocity <= 127; velocity++) {
            ShortMessage noteOn = new ShortMessage(ShortMessage.NOTE_ON, randomChannel(), randomData1(), randomData2());
            defaultTestIn.processReceive(noteOn);
        }

        settle();

        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

    @Test
    public void pass_notes_only_between_low_velocity_and_high_velocity() throws InvalidMidiDataException {
        instance.setFromVelocity(10);
        instance.setToVelocity(20);

        ShortMessage noteBelow = new ShortMessage(ShortMessage.NOTE_ON, randomChannel(), randomData1(), 9);
        defaultTestIn.processReceive(noteBelow);
        ShortMessage noteOnLower = new ShortMessage(ShortMessage.NOTE_ON, randomChannel(), randomData1(), 10);
        defaultTestIn.processReceive(noteOnLower);
        ShortMessage noteOnUpper = new ShortMessage(ShortMessage.NOTE_ON, randomChannel(), randomData1(), 20);
        defaultTestIn.processReceive(noteOnUpper);
        ShortMessage noteOnAbove = new ShortMessage(ShortMessage.NOTE_ON, randomChannel(), randomData1(), 21);
        defaultTestIn.processReceive(noteOnAbove);

        settle();

        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOnLower)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOnUpper)));
        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }

    @Test
    public void pass_notes_not_between_high_velocity_and_low_velocity_reversed() throws InvalidMidiDataException {
        instance.setFromVelocity(20);
        instance.setToVelocity(10);

        ShortMessage noteBetween = new ShortMessage(ShortMessage.NOTE_ON, randomChannel(), randomData1(), 15);
        defaultTestIn.processReceive(noteBetween);
        ShortMessage noteOnLower = new ShortMessage(ShortMessage.NOTE_ON, randomChannel(), randomData1(), 10);
        defaultTestIn.processReceive(noteOnLower);
        ShortMessage noteOnUpper = new ShortMessage(ShortMessage.NOTE_ON, randomChannel(), randomData1(), 20);
        defaultTestIn.processReceive(noteOnUpper);
        ShortMessage noteOnAbove = new ShortMessage(ShortMessage.NOTE_ON, randomChannel(), randomData1(), 21);
        defaultTestIn.processReceive(noteOnAbove);

        settle();

        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOnLower)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOnUpper)));
        assertThat(defaultTestOut.removeFifoShortMessage(), is(equalTo(noteOnAbove)));
        assertThat(defaultTestOut.receivedBufferCount(), is(0));
    }
}
