package nl.nlcode.m.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 *
 * @author leo
 */
public class Arpeggiator extends MidiInOut {
    
    interface Algorithm {
        public int clockTicksUntilNextAction();
        
    }

    public static class Standard implements Algorithm {

        @Override
        public int clockTicksUntilNextAction() {
            return 0;
        }
   
    }
    
    private static final long serialVersionUID = 0L;
    
    public enum LengthPer {
        NOTE,
        CHORD,
        RANGE,
    }
    
    public enum LengthIn {
        MIDI_CLOCK_TICKS,
    }

    private int octavesUp = 0;
    private int octavesDown = 0;
    
    private LengthIn lengthIn = LengthIn.MIDI_CLOCK_TICKS;
    
    private LengthPer lengthPer = lengthPer = LengthPer.CHORD;
    
    private int length = 200;
            
    private transient List<Integer>[] notesOn;
            
    private transient int clockTick = 0;
    
    private transient int noteIndex = 0;
    
    private transient int octaveIndex = 0;
    
    private transient int channel = 0;
    
    private transient int[] velocity;

    public Arpeggiator() {
        notesOn = new ArrayList[CHANNEL_COUNT];
        for (int channel = CHANNEL_MIN; channel <= CHANNEL_MAX; channel++) {
            notesOn[channel] = new ArrayList();
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

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage shortMessage) {
            switch (shortMessage.getCommand()) {
//                case ShortMessage.NOTE_ON -> {
//                    notesOn.add(Integer.valueOf(shortMessage.getData1()));
//                }
//                case ShortMessage.NOTE_OFF -> 
//                    notesOn.remove(Integer.valueOf(shortMessage.getData1()));
//                case ShortMessage.TIMING_CLOCK -> {
//                    if (clockTick++ > 12) {
//                        nextArpeggioAction();
//                    }
//                }
            }
            
        }
    }
    
    private void nextArpeggioAction() throws InvalidMidiDataException {
        int todo = 80;
        int velocity = 80;
        ShortMessage keyOn = new ShortMessage(ShortMessage.NOTE_ON, channel, todo, velocity);
        send(keyOn);
    }

}
