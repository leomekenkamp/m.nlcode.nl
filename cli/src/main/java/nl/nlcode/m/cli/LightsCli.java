package nl.nlcode.m.cli;

import nl.nlcode.m.engine.Lights;

/**
 *
 * @author jq59bu
 */
public class LightsCli<M extends Lights> extends MidiInOutCli<M> implements Lights.Ui {
    
    public LightsCli(M midiInOut, ControlCli controlCli) {
        super(midiInOut, controlCli);
    }
    
}
