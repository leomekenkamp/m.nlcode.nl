package nl.nlcode.m.engine;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 *
 * @author leo
 */
public class MidiDelay extends MidiInOut {

    private static final long serialVersionUID = 0L;

    public MidiDelay() {
    }

    @Override
    protected void send(MidiMessage message, long timeStamp) {
        super.send(message, timeStamp);
        if (message instanceof ShortMessage shortMessage) {
            if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {

            }
        }
    }

}
