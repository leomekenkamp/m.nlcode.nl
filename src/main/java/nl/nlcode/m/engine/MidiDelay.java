package nl.nlcode.m.engine;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 *
 * @author leo
 */
public class MidiDelay extends MidiInOut {

    private static final long serialVersionUID = 0L;

    public MidiDelay(Project project) {
        super(project);
    }

    @Override
    protected void send(MidiMessage message, long timeStamp) {
        super.send(message, timeStamp);
        if (message instanceof ShortMessage) {
            ShortMessage shortMessage = ShortMessage.class.cast(message);
            if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {

            }
        }
    }

}
