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

    private final IntUpdateProperty<U, Arpeggiator<U>> channel;

    private final IntUpdateProperty<U, Arpeggiator<U>> octaveUp;

    private final IntUpdateProperty<U, Arpeggiator<U>> octaveDown;

    private final IntUpdateProperty<U, Arpeggiator<U>> length;

    private final ObjectUpdateProperty<ArpeggiatorLengthPer, U, Arpeggiator<U>> lengthPer;

    private final IntUpdateProperty<U, Arpeggiator<U>> overrideAttackVelocity;

    private final IntUpdateProperty<U, Arpeggiator<U>> releaseVelocity;

    private final ObjectUpdateProperty<ArpeggiatorActionTakesEffect, U, Arpeggiator<U>> newChordTakesEffect;

    public static record SaveData0(
            int id,
            int channel,
            int octaveDown,
            int octaveUp,
            int length,
            String lengthPer,
            int overrideAttackVelocity,
            int releaseVelocity,
            ArpeggiatorActionTakesEffect newChordTakesEffect,
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
            target.newChordTakesEffect.set(newChordTakesEffect());
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
                newChordTakesEffect.get(),
                super.marshalInternal(-1, context)
        );
        return result;
    }

    private transient volatile List<Integer> chordNotes;

    private transient int velocity = 64;

    private transient int previousAction;

    private transient int playingNote = -1;

    private transient int currentIndex;

    private transient int currentOctave;

    private transient int noteLength = 24;

    private transient UpdateProperty.Listener onChangeRecalculateNoteLength = (oldValue, newValue) -> {
        recalculateNoteLength();
    };

    public Arpeggiator() {
        chordNotes = new ArrayList<>();
        channel = new IntUpdateProperty<>(this, 0);
        octaveDown = new IntUpdateProperty<>(this, 1, 0, 10);
        octaveDown.addListener(onChangeRecalculateNoteLength);
        octaveUp = new IntUpdateProperty<>(this, 1, 0, 10);
        octaveDown.addListener(onChangeRecalculateNoteLength);
        length = new IntUpdateProperty<>(this, 24, 1, 24 * 16); // max bit arbitrary: 16 quarter notes
        lengthPer = new ObjectUpdateProperty<>(this, ArpeggiatorLengthPer.CHORD);
        lengthPer.addListener(onChangeRecalculateNoteLength);
        overrideAttackVelocity = new IntUpdateProperty<>(this, 64, MIDI_DATA_NONE, MIDI_DATA_MAX);
        releaseVelocity = new IntUpdateProperty<>(this, 64, MIDI_DATA_MIN, MIDI_DATA_MAX);
        newChordTakesEffect = new ObjectUpdateProperty<>(this, ArpeggiatorActionTakesEffect.RANGE);
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
        if (message instanceof ShortMessage shortMessage) {
            if (isTimingClock(shortMessage)) {
                processReceiveTimingClock(timeStamp);
            } else if (shortMessage.getChannel() == channel.get()) {
                synchronized (SYNCHRONIZATION_LOCK) {
                    switch (shortMessage.getCommand()) {
                        case ShortMessage.NOTE_ON -> {
                            chordNotes.add(Integer.valueOf(shortMessage.getData1()));
                            Collections.sort(chordNotes);
                            if (chordNotes.size() == 1) {
                                resetIndices();
                            }
                            recalculateNoteLength();
                        }
                        case ShortMessage.NOTE_OFF -> {
                            chordNotes.remove(Integer.valueOf(shortMessage.getData1())); // explicit boxing needed
                            recalculateNoteLength();

                        }
                        default -> {
                            super.send(message, timeStamp);
                        }
                    }
                }
            } else {
                super.send(message, timeStamp);
            }
        } else {
            super.send(message, timeStamp);
        }
    }

    private void resetIndices() {
        currentIndex = -1;
        currentOctave = 0;
    }

    private void recalculateNoteLength() {
        noteLength = recalculateNoteLength(lengthPer.get(), length.get(), chordNotes, currentIndex, octaveDown.get(),
                octaveUp.get(), currentOctave);
    }

    static int recalculateNoteLength(ArpeggiatorLengthPer lengthPer, int length, List<Integer> receivedNotesOn, int currentIndex,
            int octaveDown, int octaveUp, int currentOctave) {
        int result = 0;
        switch (lengthPer) {
            case NOTE -> {
                result = length;
            }
            case CHORD -> {
                if (!receivedNotesOn.isEmpty()) {
                    int notesInChord = receivedNotesOn.size();
                    result = length / notesInChord;
                    int remainder = length % notesInChord;
                    if (currentIndex < remainder) {
                        // example: length: 10, 4 notes on. So every note gets 10 DIV 4 = 2 ticks.
                        // but 10 - 2*4 = 2 ticks, so the whole arpeggio run gets shortened. Which
                        // is quite annoying in the long run. This statement divides the remaining
                        // ticks nicely over the notes played.
                        result += 1;
                    }
                }
            }
            case RANGE -> {
                if (!receivedNotesOn.isEmpty()) {
                    int octaveCount = 1 + octaveUp + octaveDown;
                    int allNoteCount = octaveCount * receivedNotesOn.size();
                    result = length / allNoteCount;
                    int remainder = length % allNoteCount;
                    int octaveOffset = currentOctave + octaveDown;
                    if (currentIndex + octaveOffset * receivedNotesOn.size() < remainder) {
                        // similar to CHORD above
                        result += 1;
                    }
                }
            }
            default -> {
                throw new UnknownException(lengthPer);
            }
        }
        return result;
    }

    static int evenlyDivideTicks(int notes, int ticks, int noteOffset) {
        return 0;
    }

    /*
     * Note that this method is called from the superclass within a synchronized block.
     */
    @Override
    protected void unsynchronizedTick() {
        if (playingNote == -1 && chordNotes.isEmpty()) {
            return;
        }
        previousAction++;
        while ((playingNote == -1 || previousAction >= noteLength) && !chordNotes.isEmpty()) {
            previousAction = 0;
            if (playingNote == -1) {
                int midiNote = playingNote + 12 * currentOctave;
                if (validNote(midiNote)) {
                    send(createShortMessage(ShortMessage.NOTE_OFF, channel.get(), midiNote, velocity));
                }
            }

            // NEXT NOTE
            currentIndex += 1;
            if (currentIndex >= chordNotes.size()) {
                currentIndex = 0;
                currentOctave += 1;
                if (currentOctave > octaveUp.get()) {
                    currentOctave = -octaveDown.get();
                }
            }
            if (chordNotes.isEmpty()) {
                playingNote = -1;
                currentOctave = 0;
            } else {
                playingNote = chordNotes.get(currentIndex);
                recalculateNoteLength();
            }

            if (playingNote != -1) {
                int midiNote = playingNote + 12 * currentOctave;
                if (validNote(midiNote) && noteLength > 0) {
                    send(createShortMessage(ShortMessage.NOTE_ON, channel.get(), playingNote + 12 * currentOctave, velocity));
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

    public ArpeggiatorActionTakesEffect getNewChordTakesEffect() {
        return newChordTakesEffect.get();
    }

    public void setNewChordTakesEffect(ArpeggiatorActionTakesEffect newChordTakesEffect) {
        this.newChordTakesEffect.set(newChordTakesEffect);
    }

    public ObjectUpdateProperty<ArpeggiatorActionTakesEffect, U, Arpeggiator<U>> newChordTakesEffectProperty() {
        return newChordTakesEffect;
    }
}
