package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.m.linkui.IntUpdateProperty;
import nl.nlcode.m.linkui.ObjectUpdateProperty;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class NoteGate<U extends NoteGate.Ui> extends MidiInOut<U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static interface Ui extends MidiInOut.Ui {

    }

    private static final MidiMessageFormat MIDI_FORMAT = new MidiMessageFormat();

    private IntUpdateProperty fromVelocity = new IntUpdateProperty(MIDI_DATA_MIN);

    private IntUpdateProperty toVelocity = new IntUpdateProperty(MIDI_DATA_MAX);

    private ObjectUpdateProperty<IntervalClosure, U, NoteGate<U>> intervalClosure = new ObjectUpdateProperty(IntervalClosure.CLOSED);

    public static record SaveData0(
            int id,
            int fromVelocity,
            int toVelocity,
            IntervalClosure intervalClosure,
            Marshalled<MidiInOut> s) implements Marshalled<NoteGate> {

        @Override
        public void unmarshalInto(Marshalled.Context context, NoteGate target) {
            target.fromVelocity.set(fromVelocity());
            target.toVelocity.set(toVelocity());
            target.intervalClosure.set(intervalClosure());
            s.unmarshalInto(context, target);
        }

        @Override
        public NoteGate createMarshallable() {
            return new NoteGate();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new NoteGate.SaveData0(
                id,
                fromVelocity.get(),
                toVelocity.get(),
                intervalClosure.get(),
                super.marshalInternal(-1, context)
        );
    }

    public NoteGate() {
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        boolean sendMessage = true;
        if (message instanceof ShortMessage shortMessage) {
            if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
                IntInterval velocityInterval = new IntInterval();
                velocityInterval.setIntervalClosure(getIntervalClosure());
                velocityInterval.setLow(fromVelocity.get());
                velocityInterval.setHigh(toVelocity.get());
                sendMessage = velocityInterval.contains(shortMessage.getData2());
            }
        }
        if (sendMessage) {
            send(message, timeStamp);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("gating <{}>", MIDI_FORMAT.format(message));
            }
        }
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

    @Override
    public boolean isActiveSender() {
        return true;
    }

    public int getFromVelocity() {
        return fromVelocity.get();
    }

    public void setFromVelocity(int fromVelocity) {
        this.fromVelocity.set(fromVelocity);
    }

    public IntUpdateProperty<U, NoteGate<U>> fromVelocity() {
        return fromVelocity;
    }

    public int getToVelocity() {
        return toVelocity.get();
    }

    public void setToVelocity(int toVelocity) {
        this.toVelocity.set(toVelocity);
    }

    public IntUpdateProperty<U, NoteGate<U>> toVelocity() {
        return toVelocity;
    }

    public IntervalClosure getIntervalClosure() {
        return intervalClosure.get();
    }

    public void setIntervalClosure(IntervalClosure intervalClosure) {
        this.intervalClosure.set(intervalClosure);
    }

}
