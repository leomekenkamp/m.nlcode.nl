package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class ScaleNoteFilter<U extends ScaleNoteFilter.Ui> extends MidiInOut<U> {

    public static interface Ui extends MidiInOut.Ui {

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static record SaveData0(
            int id,
            Marshalled<MidiInOut> s) implements Marshalled<ScaleNoteFilter> {

        @Override
        public void unmarshalInto(Context context, ScaleNoteFilter target) {
            s.unmarshalInto(context, target);
        }

        @Override
        public ScaleNoteFilter createMarshallable() {
            return new ScaleNoteFilter();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                super.marshalInternal(-1, context)
        );
    }

    public ScaleNoteFilter() {
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
        boolean sendOriginal = false;
        if (message instanceof ShortMessage incoming) {
        } else {
            sendOriginal = true;
        }
        if (sendOriginal) {
            super.processReceive(message, timeStamp);
        }
    }

}
