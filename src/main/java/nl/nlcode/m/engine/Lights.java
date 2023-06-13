package nl.nlcode.m.engine;

import nl.nlcode.marshalling.Marshalled;

/**
 *
 * @author leo
 */
public class Lights extends MidiInOut<Lights.Ui> {

    public static interface Ui extends MidiInOut.Ui {
    }

    public static record SaveData0(
            int id,
            Marshalled<MidiInOut> s) implements Marshalled<Lights> {

        @Override
        public void unmarshalInto(Marshalled.Context context, Lights target) {
            s.unmarshalInto(context, target);
        }

        @Override
        public Lights createMarshallable() {
            return new Lights();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new Lights.SaveData0(
                id,
                super.marshalInternal(-1, context)
        );
    }

    public Lights() {
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

}
