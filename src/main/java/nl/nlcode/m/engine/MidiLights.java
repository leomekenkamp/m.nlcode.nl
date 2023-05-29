package nl.nlcode.m.engine;

/**
 *
 * @author leo
 */
public class MidiLights extends MidiInOut {

    public static interface Ui extends MidiInOut.Ui {
    }
    
    private static final long serialVersionUID = 0L;

    public MidiLights() {
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

}
