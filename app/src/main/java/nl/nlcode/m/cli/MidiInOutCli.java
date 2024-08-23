package nl.nlcode.m.cli;

import nl.nlcode.m.engine.MidiInOut;

/**
 *
 * @author jq59bu
 */
public abstract class MidiInOutCli<M extends MidiInOut> implements MidiInOut.Ui {
    
    private ControlCli controlCli;
    
    private M midiInOut;
    
    public MidiInOutCli(M midiInOut, ControlCli controlCli) {
        this.midiInOut = midiInOut;
        this.controlCli = controlCli;
        midiInOut.setUi(this);
    }
    
    protected ControlCli getControlCli() {
        return controlCli;
    }
    
    protected M getMidiInOut() {
        return midiInOut;
    }
                
    protected void feedback(String key, Object ... params) {
        getControlCli().commandOutput(key, params);
    }
    
    @Override
    public void sendingTo(MidiInOut receiver) {
        feedback("MidiInOutCli.sendingTo", getMidiInOut().getSystemTypeName(), getMidiInOut().getName(), receiver.getSystemTypeName(), receiver.getName());
    }

    @Override
    public void nameChanged(String previousName, String currentName) {
        if (previousName != null) {
            feedback("MidiInOutCli.nameChanged", getMidiInOut().getHumanTypeName(), previousName, currentName);
        }
    }
    
    @Override
    public void notSendingTo(MidiInOut receiver) {
        feedback("MidiInOutCli.notSendingTo", getMidiInOut().getSystemTypeName(), getMidiInOut().getName(), receiver.getSystemTypeName(), receiver.getName());
    }
    
}
