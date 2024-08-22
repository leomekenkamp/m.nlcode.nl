package nl.nlcode.m.cli;

import javax.sound.midi.MidiDevice;
import nl.nlcode.m.engine.MidiDeviceMgr;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 *
 * @author jq59bu
 */
@Command(name = "midiDevice", description = "device management")
public class MidiDeviceCommand extends ChildCommand<BaseCommand> implements Runnable {

//    @Parameters(paramLabel = "<MidiInOut type>", description = "type of instance to create", index = "0")
//    private String midiInOutType;
//
//    @Parameters(paramLabel = "<name>", description = "name for the new instance", arity = "0..1", index = "1")
//    private String name;

    @Override
    public void run() {
        getControlCli().getControl().getMidiDeviceMgr().refreshMidiDevices();
        for (MidiDevice midiDevice : getControlCli().getControl().getMidiDeviceMgr().getMidiDevices()) {
            getControlCli().stdout().print(MidiDeviceMgr.getDisplayName(midiDevice));
            getControlCli().stdout().println(midiDevice.isOpen() ? " (open)" : " (closed)");
        }
    }

}
