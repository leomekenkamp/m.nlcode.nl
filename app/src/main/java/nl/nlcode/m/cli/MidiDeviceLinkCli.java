package nl.nlcode.m.cli;

import nl.nlcode.m.engine.MidiDeviceLink;
import nl.nlcode.m.engine.MidiDeviceMgr;

/**
 *
 * @author jq59bu
 */
public class MidiDeviceLinkCli<M extends MidiDeviceLink> extends MidiInOutCli<M> implements MidiDeviceLink.Ui {
    
    public MidiDeviceLinkCli(M midiInOut, ControlCli controlCli) {
        super(midiInOut, controlCli);
    }

    @Override
    public void midiDeviceChanged() {
        feedback("MidiDeviceLinkCli.midiDeviceChanged", getMidiInOut().getName(),  MidiDeviceMgr.getDisplayName(getMidiInOut().getMidiDevice()));
    }
    
}
