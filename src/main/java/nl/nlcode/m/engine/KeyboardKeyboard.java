package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import nl.nlcode.m.linkui.IntUpdater;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class KeyboardKeyboard<U extends KeyboardKeyboard.Ui> extends MidiInOut<U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static interface Ui extends MidiInOut.Ui {
    }

    private final IntUpdater<U, KeyboardKeyboard<U>> channel;

    private final IntUpdater<U, KeyboardKeyboard<U>> velocity;

    private final IntUpdater<U, KeyboardKeyboard<U>> octave;

    public static record SaveData0(
            int id,
            int channel,
            int velocity,
            int octave,
            Marshalled<MidiInOut> s) implements Marshalled<KeyboardKeyboard> {

        @Override
        public void unmarshalInto(Context context, KeyboardKeyboard target) {
            target.channel.set(channel());
            target.velocity.set(velocity());
            target.octave.set(octave());
            s.unmarshalInto(context, target);
        }

        @Override
        public KeyboardKeyboard createMarshallable() {
            return new KeyboardKeyboard();
        }
    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                channel.get(),
                velocity.get(),
                octave.get(),
                super.marshalInternal(-1, context)
        );
    }

    public KeyboardKeyboard() {
        channel = new IntUpdater<>(this, 0, CHANNEL_MIN, CHANNEL_MAX);
        velocity = new IntUpdater<>(this, 63, MIDI_DATA_MIN, MIDI_DATA_MAX);
        octave = new IntUpdater<>(this, 4);
    }

    @Override
    public boolean isActiveSender() {
        return true;
    }

    public void keyChange(int note, boolean pressed) {
        LOGGER.debug("note: <{}>, pressed: <{}>", note, pressed);
        int transposedNote = note + (getOctave() - 4) * 12;
        if (transposedNote >= 0 && transposedNote <= 127) {
            try {
                ShortMessage msg = new ShortMessage();
                if (pressed) {
                    msg.setMessage(ShortMessage.NOTE_ON, getChannel(), transposedNote, getVelocity());
                } else {
                    msg.setMessage(ShortMessage.NOTE_OFF, getChannel(), transposedNote, getVelocity());
                }
                send(msg);
            } catch (InvalidMidiDataException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            LOGGER.warn("key outside of reach: <{}>", transposedNote);
        }
    }

    public int getChannel() {
        return channel.get();
    }

    public void setChannel(int channel) {
        this.channel.set(channel);
    }

    public IntUpdater<U, KeyboardKeyboard<U>> channel() {
        return channel;
    }

    public int getVelocity() {
        return velocity.get();
    }

    public void setVelocity(int velocity) {
        this.velocity.set(velocity);
    }

    public IntUpdater<U, KeyboardKeyboard<U>> velocity() {
        return velocity;
    }

    public int getOctave() {
        return octave.get();
    }

    public void setOctave(int octave) {
        this.octave.set(octave);
    }

    public IntUpdater<U, KeyboardKeyboard<U>> octave() {
        return octave;
    }

}
