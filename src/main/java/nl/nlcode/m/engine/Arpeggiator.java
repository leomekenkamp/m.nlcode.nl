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

    private static final long serialVersionUID = 0L;

    private transient List<Integer> notesOn;
    
    private transient List<Integer> notesPlaying;
    
    private transient int clockTick = 0;
    
    private transient int noteIndex = 0;
    
    private transient int channel = 0;
    
    private transient int velocity = 0;

    public Arpeggiator(Project project) {
        super(project);
        deserializationInit();
    }    

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        deserializationInit();
    }

    private void deserializationInit() {
        notesOn = new ArrayList<>();
        notesPlaying = new ArrayList<>();
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
        if (message instanceof ShortMessage) {
            ShortMessage shortMessage = ShortMessage.class.cast(message);
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
        ShortMessage keyOn = new ShortMessage(ShortMessage.NOTE_ON, channel, todo, velocity);
        send(keyOn);
    }

}
