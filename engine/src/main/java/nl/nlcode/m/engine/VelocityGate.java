package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.m.linkui.MidiDataUpdater;
import nl.nlcode.m.linkui.ObjectUpdater;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class VelocityGate<U extends VelocityGate.Ui> extends MidiInOut<U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static interface Ui extends MidiInOut.Ui {

    }

    private static final MidiMessageFormat MIDI_FORMAT = new MidiMessageFormat();

    private final MidiDataUpdater fromVelocity;
    private final MidiDataUpdater toVelocity;
    private final ObjectUpdater<IntervalClosure, U, VelocityGate<U>> intervalClosure;

    public static record SaveData0(
            int id,
            int fromVelocity,
            int toVelocity,
            IntervalClosure intervalClosure,
            Marshalled<MidiInOut> s) implements Marshalled<VelocityGate> {

        @Override
        public void unmarshalInto(Marshalled.Context context, VelocityGate target) {
            target.fromVelocity.set(fromVelocity());
            target.toVelocity.set(toVelocity());
            target.intervalClosure.set(intervalClosure());
            s.unmarshalInto(context, target);
        }

        @Override
        public VelocityGate createMarshallable() {
            return new VelocityGate();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new VelocityGate.SaveData0(
                id,
                fromVelocity.get(),
                toVelocity.get(),
                intervalClosure.get(),
                super.marshalInternal(-1, context)
        );
    }

    public VelocityGate() {
        fromVelocity = new MidiDataUpdater("from", this, MIDI_DATA_MIN);
        toVelocity = new MidiDataUpdater("to", this, MIDI_DATA_MAX);
        intervalClosure = new ObjectUpdater(this, IntervalClosure.CLOSED);
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

    public MidiDataUpdater<U, VelocityGate<U>> fromVelocity() {
        return fromVelocity;
    }

    public int getToVelocity() {
        return toVelocity.get();
    }

    public void setToVelocity(int toVelocity) {
        this.toVelocity.set(toVelocity);
    }

    public MidiDataUpdater<U, VelocityGate<U>> toVelocity() {
        return toVelocity;
    }

    public IntervalClosure getIntervalClosure() {
        return intervalClosure.get();
    }

    public void setIntervalClosure(IntervalClosure intervalClosure) {
        this.intervalClosure.set(intervalClosure);
    }

}
