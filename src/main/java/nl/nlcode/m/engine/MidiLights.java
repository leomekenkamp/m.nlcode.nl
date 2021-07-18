package nl.nlcode.m.engine;

/**
 *
 * @author leo
 */
public class MidiLights extends MidiInOut {

    private static final long serialVersionUID = 0L;

    public MidiLights(Project project) {
        super(project);
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

}
