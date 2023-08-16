package nl.nlcode.m.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.MidiInOut.CHANNEL_COUNT;
import static nl.nlcode.m.engine.MidiInOut.forAllChannels;
import nl.nlcode.marshalling.Marshalled;

/**
 *
 * @author leo
 */
public class DebugMidiInOut<U extends DebugMidiInOut.Ui> extends MidiInOut<U> {

    private String saveTest;
    
    public static interface Ui extends MidiInOut.Ui {

    }

    public static record SaveData0(
            int id,
            String saveTest,
            Marshalled<MidiInOut> s) implements Marshalled<DebugMidiInOut> {

        @Override
        public void unmarshalInto(Context context, DebugMidiInOut target) {
            target.saveTest = saveTest;
            s.unmarshalInto(context, target);
        }

        @Override
        public DebugMidiInOut createMarshallable() {
            return new DebugMidiInOut();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                saveTest,
                super.marshalInternal(-1, context)
        );
    }


    private transient List<MidiMessage> midiMessages = new ArrayList<>();
    private transient List<Long> timeStamps = new ArrayList<>();

    public DebugMidiInOut() {
    }

    public DebugMidiInOut(Project project) {
        openWith(project);
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
        midiMessages.add(message);
        timeStamps.add(timeStamp);
        send(message, timeStamp);
    }

    public List<MidiMessage> getReceivedMidiMessages() {
        return Collections.<MidiMessage>unmodifiableList(midiMessages);
    }

    public List<Long> getReceivedTimeStamps() {
        return Collections.<Long>unmodifiableList(timeStamps);
    }

    public int receivedBufferCount() {
        return getReceivedMidiMessages().size();
    }

    public MidiMessage receivedMidi(int index) {
        return getReceivedMidiMessages().get(index);
    }

    public Long receivedTimeStamp(int index) {
        return getReceivedTimeStamps().get(index);
    }

    public ShortMessage receivedShort(int index) {
        MidiMessage midi = receivedMidi(index);
        if (midi instanceof ShortMessage shortMessage) {
            return shortMessage;
        } else {
            throw new IllegalArgumentException("message at index <" + index + "> has type <" + midi.getClass() + ">");
        }
    }

    public ShortMessage removeFifoShortMessage() {
        MidiMessage midi = midiMessages.remove(0);
        if (midi instanceof ShortMessage shortMessage) {
            timeStamps.remove(0);
            return shortMessage;
        } else {
            getReceivedMidiMessages().add(0, midi);
            throw new IllegalArgumentException("message at front has type <" + midi.getClass() + ">, I pushed it back into the list");
        }
    }

    public void processReceiveMidiClock() {
        processReceive(createTimingClock(), -1);
    }

    public void processReceiveMidiClock(int number) {
        if (number < 1) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < number; i++) {
            processReceiveMidiClock();
        }
    }

    public void clearReceived() {
        midiMessages.clear();
        timeStamps.clear();
    }
}
