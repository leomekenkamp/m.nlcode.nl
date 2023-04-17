package nl.nlcode.m.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 *
 * @author leo
 */
public class DebugMidiInOut extends MidiInOut {
    
    private List<MidiMessage> midiMessages = new ArrayList<>(); 
    private List<Long> timeStamps = new ArrayList<>(); 
            
    public DebugMidiInOut(Project project) {
        super(project);
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
    
    public int receivedCount() {
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
        if (midi instanceof ShortMessage) {
            return ShortMessage.class.cast(midi);
        } else {
            throw new IllegalArgumentException("message at index <" + index + "> has type <" + midi.getClass() + ">");
        }
    }
}
