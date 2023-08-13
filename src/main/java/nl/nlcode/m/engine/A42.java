package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import javax.sound.midi.ShortMessage;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class A42<U extends A42.Ui> extends MidiInOut<U> {

    public static interface Ui extends MidiInOut.Ui {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static record SaveData0(
            int id,
            Marshalled<MidiInOut> s) implements Marshalled<A42> {

        @Override
        public void unmarshalInto(Context context, A42 target) {
            s.unmarshalInto(context, target);
        }

        @Override
        public A42 createMarshallable() {
            return new A42();
        }
    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                super.marshalInternal(-1, context)
        );
    }

    public A42() {
    }

    @Override
    public boolean isActiveReceiver() {
        return false;
    }

    @Override
    public boolean isActiveSender() {
        return true;
    }

    public void panicManually() {
        forAllChannels(channel -> panicManually(channel));
    }
    
    public void panicManually(int channel) {
        forAllNotes(note -> send(createShortMessage(ShortMessage.NOTE_OFF, channel, note, 64)));
    }
    
    public void allNotesOff() {
        forAllChannels(channel -> allNotesOff(channel));
    }

    public void allNotesOff(int channel) {
        send(ChannelModeMessage.ALL_NOTES_OFF.create(channel));
    }

    public void allSoundOff() {
        forAllChannels(channel -> allSoundOff(channel));
    }
    
    public void allSoundOff(int channel) {
        send(ChannelModeMessage.ALL_SOUND_OFF.create(channel));
    }
}
