package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import nl.nlcode.m.linkui.BooleanUpdateProperty;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class MessageTypeFilter<U extends MessageTypeFilter.Ui> extends MidiInOut<U> {

    public static interface Ui extends MidiInOut.Ui {

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final BooleanUpdateProperty filterNoteOn;
    private final BooleanUpdateProperty filterNoteOff;
    private final BooleanUpdateProperty filterTimerClock;
    private final BooleanUpdateProperty filterSysEx;
    private final BooleanUpdateProperty filterControllers;
    private final BooleanUpdateProperty filterProgramChange;

    public static record SaveData0(
            int id,
            boolean filterNoteOn,
            boolean filterNoteOff,
            boolean filterTimerClock,
            boolean filterSysEx,
            boolean filterControllers,
            boolean filterProgramChange,
            Marshalled<MidiInOut> s) implements Marshalled<MessageTypeFilter> {

        @Override
        public void unmarshalInto(Context context, MessageTypeFilter target) {
            target.filterNoteOn.set(filterNoteOn());
            target.filterNoteOff.set(filterNoteOff());
            target.filterTimerClock.set(filterTimerClock());
            target.filterSysEx.set(filterSysEx());
            target.filterControllers.set(filterControllers());
            target.filterProgramChange.set(filterProgramChange());
            s.unmarshalInto(context, target);
        }

        @Override
        public MessageTypeFilter createMarshallable() {
            return new MessageTypeFilter();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                filterNoteOn.get(),
                filterNoteOff.get(),
                filterTimerClock.get(),
                filterSysEx.get(),
                filterControllers.get(),
                filterProgramChange.get(),
                super.marshalInternal(-1, context)
        );
    }

    public MessageTypeFilter() {
        filterNoteOn = new BooleanUpdateProperty(this, false);
        filterNoteOff = new BooleanUpdateProperty(this, false);
        filterTimerClock = new BooleanUpdateProperty(this, false);
        filterSysEx = new BooleanUpdateProperty(this, false);
        filterControllers = new BooleanUpdateProperty(this, false);
        filterProgramChange = new BooleanUpdateProperty(this, false);
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

    @Override
    public boolean isActiveSender() {
        return true;
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        boolean filter = false;
        if (message instanceof SysexMessage) {
            filter = filterSysEx.get();
        } else if (message instanceof ShortMessage incoming) {
            if (incoming.getCommand() == ShortMessage.NOTE_ON) {
                filter = filterNoteOn.get();
            } else if (incoming.getCommand() == ShortMessage.NOTE_OFF) {
                filter = filterNoteOff.get();
            } else if (incoming.getCommand() == ShortMessage.TIMING_CLOCK) {
                filter = filterTimerClock.get();
            } else if (incoming.getCommand() == ShortMessage.PROGRAM_CHANGE) {
                filter = filterProgramChange.get();
            }
        }
        if (!filter) {
            super.processReceive(message, timeStamp);
        }
    }

    public BooleanUpdateProperty filterNoteOn() {
        return filterNoteOn;
    }

    public BooleanUpdateProperty filterNoteOff() {
        return filterNoteOff;
    }

    public BooleanUpdateProperty filterTimerClock() {
        return filterTimerClock;
    }

    public BooleanUpdateProperty filterSysEx() {
        return filterSysEx;
    }

    public BooleanUpdateProperty filterControllers() {
        return filterControllers;
    }

    public BooleanUpdateProperty filterProgramChange() {
        return filterProgramChange;
    }

}
