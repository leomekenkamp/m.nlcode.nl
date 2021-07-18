package nl.nlcode.m.engine;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class KeyboardKeyboard extends MidiInOut {

    private static final long serialVersionUID = 0L;

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyboardKeyboard.class);

    private int channel;

    private int velocity;

    private int octave;

    public KeyboardKeyboard(Project project) {
        super(project);
        channel = 0;
        velocity = 63;
        octave = 4;
    }

    @Override
    public boolean isActiveSender() {
        return true;
    }

    public int getOneBasedChannel() {
        return getZeroBasedChannel() + 1;
    }

    public void setOneBasedChannel(int channel) {
        setZeroBasedChannel(channel - 1);
    }

    public int getZeroBasedChannel() {
        return channel;
    }

    public void setZeroBasedChannel(int channel) {
        LOGGER.debug("octave (0-based) now {}", octave);
        this.channel = channel;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public int getOctave() {
        return octave;
    }

    public void setOctave(int octave) {
        LOGGER.debug("octave now {}", octave);
        this.octave = octave;
    }

    public void keyChange(int note, boolean pressed) {
        LOGGER.debug("note: {}, pressed: {}", note, pressed);
        int transposedNote = note + (getOctave() - 4) * 12;
        if (transposedNote >= 0 && transposedNote <= 127) {
            try {
                ShortMessage msg = new ShortMessage();
                if (pressed) {
                    msg.setMessage(ShortMessage.NOTE_ON, getZeroBasedChannel(), transposedNote, getVelocity());
                } else {
                    msg.setMessage(ShortMessage.NOTE_OFF, getZeroBasedChannel(), transposedNote, getVelocity());
                }
                send(msg);
            } catch (InvalidMidiDataException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            LOGGER.warn("key outside of reach: {}", transposedNote);
        }
    }

}
