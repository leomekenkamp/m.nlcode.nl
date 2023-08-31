package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.Direction.DOWN;
import static nl.nlcode.m.engine.Direction.UP;
import nl.nlcode.m.linkui.IntUpdater;
import nl.nlcode.m.linkui.ObjectUpdater;
import nl.nlcode.m.linkui.Updater;
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

    private final IntUpdater<U, Arpeggiator<U>> channel;

    private final IntUpdater<U, Arpeggiator<U>> octaveUp;

    private final IntUpdater<U, Arpeggiator<U>> octaveDown;

    private final IntUpdater<U, Arpeggiator<U>> length;

    private final ObjectUpdater<ArpeggiatorLengthPer, U, Arpeggiator<U>> lengthPer;

    private final IntUpdater<U, Arpeggiator<U>> overrideAttackVelocity;

    private final IntUpdater<U, Arpeggiator<U>> releaseVelocity;

    private final ObjectUpdater<ArpeggiatorActionTakesEffect, U, Arpeggiator<U>> newChordTakesEffect;

    private final ObjectUpdater<Direction, U, Arpeggiator<U>> chordDirection;

    private final ObjectUpdater<Direction, U, Arpeggiator<U>> rangeDirection;

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
            String chordDirection,
            String rangeDirection,
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
            if (chordDirection() != null) {
                target.chordDirection.set(Direction.valueOf(chordDirection()));
            }
            if (rangeDirection() != null) {
                target.rangeDirection.set(Direction.valueOf(rangeDirection()));
            }
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
                chordDirection.get().name(),
                rangeDirection.get().name(),
                super.marshalInternal(-1, context)
        );
        return result;
    }

    private transient volatile List<Integer> chordNotes;

    private transient int velocity = 64;

    private transient int previousAction;

    private transient int playingNote = -1;

    private transient int chordNoteIndex;

    private transient int currentOctave;

    private transient int noteLength = 24;

    private transient Updater.Listener onChangeRecalculateNoteLength = (oldValue, newValue) -> {
        recalculateNoteLength();
    };

    public Arpeggiator() {
        chordNotes = new ArrayList<>();
        channel = new IntUpdater<>(this, 0);
        octaveDown = new IntUpdater<>(this, 1, 0, 10);
        octaveDown.addListener(onChangeRecalculateNoteLength);
        octaveUp = new IntUpdater<>(this, 1, 0, 10);
        octaveDown.addListener(onChangeRecalculateNoteLength);
        length = new IntUpdater<>(this, 24, 1, 24 * 16); // max bit arbitrary: 16 quarter notes
        lengthPer = new ObjectUpdater<>(this, ArpeggiatorLengthPer.CHORD);
        lengthPer.addListener(onChangeRecalculateNoteLength);
        overrideAttackVelocity = new IntUpdater<>(this, 64, MIDI_DATA_NONE, MIDI_DATA_MAX);
        releaseVelocity = new IntUpdater<>(this, 64, MIDI_DATA_MIN, MIDI_DATA_MAX);
        newChordTakesEffect = new ObjectUpdater<>(this, ArpeggiatorActionTakesEffect.RANGE);
        chordDirection = new ObjectUpdater(this, Direction.DOWN);
        rangeDirection = new ObjectUpdater(this, Direction.UP);
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
        chordNoteIndex = switch (chordDirection.get()) {
            case UP ->
                -1;
            case DOWN ->
                Integer.MAX_VALUE;
        };
        currentOctave = 0;
    }

    private void recalculateNoteLength() {
        noteLength = recalculateNoteLength(lengthPer.get(), length.get(), chordNotes, chordNoteIndex, octaveDown.get(),
                octaveUp.get(), currentOctave);
    }

    static int recalculateNoteLength(ArpeggiatorLengthPer lengthPer, int length, List<Integer> chordNotes, int currentIndex,
            int octaveDown, int octaveUp, int currentOctave) {
        int result = 0;
        switch (lengthPer) {
            case NOTE -> {
                result = length;
            }
            case CHORD -> {
                if (!chordNotes.isEmpty()) {
                    int notesInChord = chordNotes.size();
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
                if (!chordNotes.isEmpty()) {
                    int octaveCount = 1 + octaveUp + octaveDown;
                    int allNoteCount = octaveCount * chordNotes.size();
                    result = length / allNoteCount;
                    int remainder = length % allNoteCount;
                    int octaveOffset = currentOctave + octaveDown;
                    if (currentIndex + octaveOffset * chordNotes.size() < remainder) {
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
            if (playingNote != -1) {
                int midiNote = playingNote + 12 * currentOctave;
                if (validNote(midiNote)) {
                    send(createShortMessage(ShortMessage.NOTE_OFF, channel.get(), midiNote, velocity));
                }
            }

            // NEXT NOTE
            selectNextNote();
            if (chordNotes.isEmpty()) {
                playingNote = -1;
                currentOctave = 0;
            } else {
                playingNote = chordNotes.get(chordNoteIndex);
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

    private void selectNextNote() {
        switch (chordDirection.get()) {
            case UP:
                chordNoteIndex += 1;
                if (chordNoteIndex >= chordNotes.size()) {
                    chordNoteIndex = 0;
                    selectNextOctave();
                }
                break;
            case DOWN:
                chordNoteIndex -= 1;
                if (chordNoteIndex >= chordNotes.size()) {
                    chordNoteIndex = chordNotes.size() - 1;
                }
                if (chordNoteIndex <= -1) {
                    chordNoteIndex = chordNotes.size() - 1;
                    selectNextOctave();
                }
                break;
            default:
                throw new UnknownException(chordDirection.get());
        }
    }

    private void selectNextOctave() {
        switch (rangeDirection.get()) {
            case UP:
                currentOctave += 1;
                if (currentOctave > octaveUp.get()) {
                    currentOctave = -octaveDown.get();
                }
                break;
            case DOWN:
                currentOctave -= 1;
                if (currentOctave < -octaveDown.get()) {
                    currentOctave = octaveUp.get();
                }
                break;
            default:
                throw new UnknownException(rangeDirection.get());
        }
    }

    public int getChannel() {
        return channel.get();
    }

    public void setChannel(int channel) {
        this.channel.set(channel);
    }

    public IntUpdater<U, Arpeggiator<U>> channelProperty() {
        return channel;
    }

    public int getOctaveDown() {
        return octaveDown.get();
    }

    public void setOctaveDown(int octaveDown) {
        this.octaveDown.set(octaveDown);
    }

    public IntUpdater<U, Arpeggiator<U>> octaveDownProperty() {
        return octaveDown;
    }

    public int getOctaveUp() {
        return octaveUp.get();
    }

    public void setOctaveUp(int octaveUp) {
        this.octaveUp.set(octaveUp);
    }

    public IntUpdater<U, Arpeggiator<U>> octaveUpProperty() {
        return octaveUp;
    }

    public int getLength() {
        return length.get();
    }

    public void setLength(int length) {
        this.length.set(length);
    }

    public IntUpdater<U, Arpeggiator<U>> lengthProperty() {
        return length;
    }

    public ArpeggiatorLengthPer getLengthPer() {
        return lengthPer.get();
    }

    public void setLengthPer(ArpeggiatorLengthPer lengthPer) {
        this.lengthPer.set(lengthPer);
    }

    public ObjectUpdater<ArpeggiatorLengthPer, U, Arpeggiator<U>> lengthPerProperty() {
        return lengthPer;
    }

    public int getOverrideAttackVelocity() {
        return overrideAttackVelocity.get();
    }

    public void setOverrideAttackVelocity(int overrideAttackVelocity) {
        this.overrideAttackVelocity.set(overrideAttackVelocity);
    }

    public IntUpdater<U, Arpeggiator<U>> overrideAttackVelocityProperty() {
        return overrideAttackVelocity;
    }

    public int getReleaseVelocity() {
        return overrideAttackVelocity.get();
    }

    public void setReleaseVelocity(int releaseVelocity) {
        this.releaseVelocity.set(releaseVelocity);
    }

    public IntUpdater<U, Arpeggiator<U>> releaseVelocityProperty() {
        return releaseVelocity;
    }

    public ArpeggiatorActionTakesEffect getNewChordTakesEffect() {
        return newChordTakesEffect.get();
    }

    public void setNewChordTakesEffect(ArpeggiatorActionTakesEffect newChordTakesEffect) {
        this.newChordTakesEffect.set(newChordTakesEffect);
    }

    public ObjectUpdater<ArpeggiatorActionTakesEffect, U, Arpeggiator<U>> newChordTakesEffectProperty() {
        return newChordTakesEffect;
    }
    
    public Direction getRangeDirection() {
        return rangeDirection.get();
    }
    
    public void setRangeDirection(Direction direction) {
        rangeDirection.set(direction);
    }
    
    public ObjectUpdater<Direction, U, Arpeggiator<U>> rangeDirectionUpdater() {
        return rangeDirection;
    }
    
    public Direction getChordDirection() {
        return chordDirection.get();
    }
    
    public void setChordDirection(Direction direction) {
        chordDirection.set(direction);
    }
    
    public ObjectUpdater<Direction, U, Arpeggiator<U>> chordDirectionUpdater() {
        return chordDirection;
    }
    
}