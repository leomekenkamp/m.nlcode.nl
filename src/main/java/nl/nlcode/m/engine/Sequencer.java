package nl.nlcode.m.engine;

import nl.nlcode.marshalling.Marshalled;

/**
 *
 * @author leo
 */
public class Sequencer<U extends Sequencer.Ui> extends MidiInOut<U> {

    public static interface Ui extends MidiInOut.Ui {
    }

    public static record SaveData0(
            int id,
            Marshalled<MidiInOut> s) implements Marshalled<Sequencer> {

        @Override
        public void unmarshalInto(Marshalled.Context context, Sequencer target) {
            s.unmarshalInto(context, target);
        }

        @Override
        public Sequencer createMarshallable() {
            return new Sequencer();
        }
    }
    
    public Sequencer() {
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

    @Override
    public boolean isActiveSender() {
        return true;
    }

}
