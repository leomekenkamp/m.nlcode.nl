package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.m.linkui.IntUpdateProperty;
import nl.nlcode.m.linkui.ObjectUpdateProperty;
import nl.nlcode.m.linkui.UpdateProperty;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class Arpeggiator<U extends Arpeggiator.Ui> extends TimeSensitiveMidiInOut<U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static interface Ui extends TimeSensitiveMidiInOut.Ui {
    }

    private IntUpdateProperty<U, Arpeggiator<U>> channel;

    private IntUpdateProperty<U, Arpeggiator<U>> octaveUp;

    private IntUpdateProperty<U, Arpeggiator<U>> octaveDown;

    private IntUpdateProperty<U, Arpeggiator<U>> length;

    private ObjectUpdateProperty<ArpeggiatorLengthPer, U, Arpeggiator<U>> lengthPer;

    private IntUpdateProperty<U, Arpeggiator<U>> overrideAttackVelocity;

    private IntUpdateProperty<U, Arpeggiator<U>> releaseVelocity;

    public static record SaveData0(
            int id,
            int channel,
            int octaveDown,
            int octaveUp,
            int length,
            String lengthPer,
            int overrideAttackVelocity,
            int releaseVelocity,
            Marshalled<MidiInOut> s) implements Marshalled<Arpeggiator> {

        @Override
        public void unmarshalInto(Context context, Arpeggiator target) {
            s.unmarshalInto(context, target);
            target.channel.set(channel());
            target.octaveDown.set(octaveDown());
            target.octaveUp.set(octaveUp());
            target.length.set(length());
            target.lengthPer.set(ArpeggiatorLengthPer.valueOf(lengthPer()));
            target.overrideAttackVelocity.set(overrideAttackVelocity());
            target.releaseVelocity.set(releaseVelocity());
        }

        @Override
        public Arpeggiator createMarshallable() {
            return new Arpeggiator();
        }
    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        SaveData0 result = new SaveData0(
                id,
                channel.get(),
                octaveDown.get(),
                octaveUp.get(),
                length.get(),
                lengthPer.get().name(),
                overrideAttackVelocity.get(),
                releaseVelocity.get(),
                super.marshalInternal(-1, context)
        );
        return result;
    }

    private transient volatile List<Integer> notesOn;

    private transient int velocity = 64;

    private transient int previousAction;

    private transient int currentNote = -1;

    private transient int currentIndex;

    private transient int currentOctave;

    private transient int noteLength = 24;

    private transient UpdateProperty.Listener onChangeRecalculateNoteLength = (oldValue, newValue) -> {
        recalculateNoteLength();
    };

    public Arpeggiator() {
        notesOn = new ArrayList<>();
        channel = new IntUpdateProperty<>(0);
        channel.register(this);
        octaveDown = new IntUpdateProperty<>(1, 0, 10);
        octaveDown.register(this);
        octaveDown.addListener(onChangeRecalculateNoteLength);
        octaveUp = new IntUpdateProperty<>(1, 0, 10);
        octaveUp.register(this);
        octaveDown.addListener(onChangeRecalculateNoteLength);
        length = new IntUpdateProperty<>(24, 1, 24 * 16); // max bit arbitrary: 16 quarter notes
        length.register(this);
        length.addListener(onChangeRecalculateNoteLength);
        lengthPer = new ObjectUpdateProperty<>(ArpeggiatorLengthPer.CHORD);
        lengthPer.register(this);
        lengthPer.addListener(onChangeRecalculateNoteLength);
        overrideAttackVelocity = new IntUpdateProperty<>(MIDI_DATA_NONE, MIDI_DATA_NONE, MIDI_DATA_MAX);
        overrideAttackVelocity.register(this);
        releaseVelocity = new IntUpdateProperty<>(64, MIDI_DATA_MIN, MIDI_DATA_MAX);
        releaseVelocity.register(this);
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
        if (message instanceof ShortMessage shortMessage && shortMessage.getChannel() == channel.get()) {
            synchronized (SYNCHRONIZATION_LOCK) {
                switch (shortMessage.getCommand()) {
                    case ShortMessage.NOTE_ON -> {
                        if (notesOn.isEmpty()) {
                            send(shortMessage);
                            currentNote = shortMessage.getData1();
                            previousAction = 0;
                            recalculateNoteLength();
                        }
                        notesOn.add(Integer.valueOf(shortMessage.getData1()));
                        Collections.sort(notesOn);
                    }
                    case ShortMessage.NOTE_OFF -> {
                        notesOn.remove(Integer.valueOf(shortMessage.getData1())); // explicit boxing needed
                        recalculateNoteLength();

                    }
                    case ShortMessage.TIMING_CLOCK -> {
                        processReceiveTimingClock(timeStamp);
                    }
                    default -> {
                        super.send(message, timeStamp);
                    }
                }
            }
        } else {
            super.send(message, timeStamp);
        }
    }

    private void recalculateNoteLength() {
        switch (lengthPer.get()) {
            case NOTE -> {
                noteLength = length.get();
            }
            case CHORD -> {
                if (!notesOn.isEmpty()) {
                    noteLength = length.get() / notesOn.size();
                    int remainder = length.get() % notesOn.size();
                    if (remainder > 0 && currentNote < remainder) {
                        // example: length: 10, 4 notes on. So every note get 10 DIV 4 = 2 ticks.
                        // but 10 - 2*4 = 2 ticks, so the whole arpeggio run gets shortened. Which
                        // is quite annoying in the long run. This statement divides the remaining
                        // ticks over the first notes played.
                        noteLength += 1;
                    }
                }
            }
            case RANGE -> {
                if (!notesOn.isEmpty()) {
                    int octaveCount = 1 + octaveUp.get() + octaveDown.get();
                    int allNoteCount = octaveCount * notesOn.size();
                    noteLength = length.get() / allNoteCount;
                    int remainder = length.get() % allNoteCount;
                    if (remainder > 0) {
                        int octaveOffset = currentOctave + octaveDown.get();
                        if (currentNote + (currentOctave * notesOn.size()) < remainder) {
                            // similar to CHORD above
                            noteLength += 1;
                        }
                    }
                }
            }
            default -> {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    protected void synchronizedTick() {
        if (currentNote == -1 && notesOn.isEmpty()) {
            return;
        }
        if (previousAction++ >= noteLength) {
            previousAction = 0;
            if (currentNote != -1) {
                int midiNote = currentNote + 12 * currentOctave;
                if (validNote(midiNote)) {
                    send(createShortMessage(ShortMessage.NOTE_OFF, channel.get(), midiNote, velocity));
                }
            }

            // NEXT NOTE
            currentIndex += 1;
            if (currentIndex >= notesOn.size()) {
                currentIndex = 0;
                currentOctave += 1;
                if (currentOctave > octaveUp.get()) {
                    currentOctave = -octaveDown.get();
                }
            }
            if (notesOn.isEmpty()) {
                currentNote = -1;
                currentOctave = 0;
            } else {
                currentNote = notesOn.get(currentIndex);
                recalculateNoteLength();
            }

            if (currentNote != -1) {
                int midiNote = currentNote + 12 * currentOctave;
                if (validNote(midiNote) && noteLength > 0) {
                    send(createShortMessage(ShortMessage.NOTE_ON, channel.get(), currentNote + 12 * currentOctave, velocity));
                }
            }
        }
    }

    public int getChannel() {
        return channel.get();
    }

    public void setChannel(int channel) {
        this.channel.set(channel);
    }

    public IntUpdateProperty<U, Arpeggiator<U>> channelProperty() {
        return channel;
    }

    public int getOctaveDown() {
        return octaveDown.get();
    }

    public void setOctaveDown(int octaveDown) {
        this.octaveDown.set(octaveDown);
    }

    public IntUpdateProperty<U, Arpeggiator<U>> octaveDownProperty() {
        return octaveDown;
    }

    public int getOctaveUp() {
        return octaveUp.get();
    }

    public void setOctaveUp(int octaveUp) {
        this.octaveUp.set(octaveUp);
    }

    public IntUpdateProperty<U, Arpeggiator<U>> octaveUpProperty() {
        return octaveUp;
    }

    public int getLength() {
        return length.get();
    }

    public void setLength(int length) {
        this.length.set(length);
    }

    public IntUpdateProperty<U, Arpeggiator<U>> lengthProperty() {
        return length;
    }

    public ArpeggiatorLengthPer getLengthPer() {
        return lengthPer.get();
    }

    public void setLengthPer(ArpeggiatorLengthPer lengthPer) {
        this.lengthPer.set(lengthPer);
    }

    public ObjectUpdateProperty<ArpeggiatorLengthPer, U, Arpeggiator<U>> lengthPerProperty() {
        return lengthPer;
    }

    public int getOverrideAttackVelocity() {
        return overrideAttackVelocity.get();
    }

    public void setOverrideAttackVelocity(int overrideAttackVelocity) {
        this.overrideAttackVelocity.set(overrideAttackVelocity);
    }

    public IntUpdateProperty<U, Arpeggiator<U>> overrideAttackVelocityProperty() {
        return overrideAttackVelocity;
    }

    public int getReleaseVelocity() {
        return overrideAttackVelocity.get();
    }

    public void setReleaseVelocity(int releaseVelocity) {
        this.releaseVelocity.set(releaseVelocity);
    }

    public IntUpdateProperty<U, Arpeggiator<U>> releaseVelocityProperty() {
        return releaseVelocity;
    }

}
