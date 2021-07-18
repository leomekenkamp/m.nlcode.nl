package nl.nlcode.m.engine;

/**
 *
 * @author leo
 */
public class MidiSequencer extends MidiInOut {

    private static final long serialVersionUID = 0L;

    public MidiSequencer(Project project) {
        super(project);
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
