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
                
    protected void notifyUser(String key, Object ... params) {
        getControlCli().printMessage(key, params);
    }
    
    @Override
    public void sendingTo(MidiInOut receiver) {
        notifyUser("MidiInOutCli.sendingTo", getMidiInOut().getHumanTypeName(), getMidiInOut().getName(), receiver.getHumanTypeName(), receiver.getName());
    }

    @Override
    public void nameChanged(String previousName, String currentName) {
        if (previousName == null) {
            notifyUser("MidiInOutCli.new", getMidiInOut().getHumanTypeName(), currentName);
        } else {
            notifyUser("MidiInOutCli.nameChanged", getMidiInOut().getHumanTypeName(), previousName, currentName);
        }
    }
    
    @Override
    public void notSendingTo(MidiInOut receiver) {
        notifyUser("MidiInOutCli.notSendingTo", getMidiInOut().getSystemTypeName(), getMidiInOut().getName(), receiver.getSystemTypeName(), receiver.getName());
    }
    
}
