package nl.nlcode.m.cli;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import nl.nlcode.m.engine.MidiDeviceMgr;
import nl.nlcode.m.engine.MidiInOut;
import nl.nlcode.m.engine.Project;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.IParameterPreprocessor;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 *
 * @author jq59bu
 */
@Command(name = "device", description = "external MIDI device management")
public class MidiDeviceCommand extends ChildCommand<BaseCommand> implements Runnable {

    /**
     *
     * @author jq59bu
     */
    @Command(description = "Either shows output connections to other midiInOut instances (no parameter), or connects a sending midiInOut instances to a receiving one.")
    public class ConnectCommand extends ChildCommand<MidiInOutCommand> implements Runnable {

        @Option(names = {"--reverse", "-r"}, description = "Show or set input instead of output connections.")
        private boolean reverse;

        @Parameters(paramLabel = "<receiver>", description = "name for the receiving MidiInOut instance", arity = "0..1")
        private String secondName;

        @Override
        public void run() {
            MidiInOut first = getParent().getMidiInOut();
            if (secondName == null) {
                final Set<MidiInOut> others = reverse ? first.receivingFrom() : getParent().getMidiInOut().sendingTo();
                for (MidiInOut r : others) {
                    getControlCli().stdout().println(r.getName());
                }
            } else {
                MidiInOut second = lookup(getControlCli().getCurrentProject(), secondName);
                if (reverse) {
                    second.startSendingTo(first);
                } else {
                    first.startSendingTo(second);
                }
            }
        }

        private MidiInOut lookup(Project project, String name) {
            MidiInOut result = project.getMidiInOutLookup().get(name);
            if (result == null) {
                getControlCli().commandOutput("MidiInOut.name.none", name, project.getPath());
            }
            return result;
        }

    }

    protected static class ClosedMidiDevices implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            List<String> result = new ArrayList<>();
            for (MidiDevice midiDevice : MidiDeviceMgr.getInstance().getMidiDevices()) {
                if (!midiDevice.isOpen()) {
                    result.add("\"" + MidiDeviceMgr.getDisplayName(midiDevice) + "\"");
                }
            }
            return result.iterator();
        }
    }

    protected static class MidiDeviceDefinition {

        @Option(completionCandidates = ClosedMidiDevices.class, names = {"--name", "-n"}, description = "name of midi device to open", scope = CommandLine.ScopeType.INHERIT)
        protected String name;

        @Option(names = {"--all", "-a"}, description = "open all midi devices", scope = CommandLine.ScopeType.INHERIT)
        protected boolean all;
    }

    @Command(name = "systemInfo", description = "shows infomation over the current midi system")
    public void systemInfo() {
        getControlCli().stdout().println(getControlCli().getControl().getMidiDeviceMgr().getSystemInfo());
    }

    @Command(name = "open", description = "open midi device")
    public void open(@ArgGroup(exclusive = true) MidiDeviceDefinition midiDeviceDefinition) {
        onSpecifiedMidiDevice(midiDeviceDefinition, midiDevice -> !midiDevice.isOpen(), (midiDevice)
                -> getControlCli().getControl().getMidiDeviceMgr().open(midiDevice)
        );
    }

    @Command(name = "close", description = "close midi device")
    public void close(@ArgGroup(exclusive = true) MidiDeviceDefinition midiDeviceDefinition) {
        onSpecifiedMidiDevice(midiDeviceDefinition,
                midiDevice -> midiDevice.isOpen(),
                (midiDevice) -> {
                    midiDevice.close();
                });
    }

    protected void onSpecifiedMidiDevice(MidiDeviceDefinition midiDeviceDefinition, Predicate<MidiDevice> filter, Consumer<MidiDevice> action) {
        if (midiDeviceDefinition.all) {
            filter = midiDevice -> true;
        } else if (midiDeviceDefinition.name != null) {
            filter = midiDevice -> midiDeviceDefinition.name.equals(MidiDeviceMgr.getDisplayName(midiDevice));
        } else {
            throw new IllegalStateException("should never get here");
        }
        List<MidiDevice> toOpen = getControlCli().getControl().getMidiDeviceMgr().getMidiDevices().stream()
                .filter(filter)
                .collect(Collectors.toList());
        if (toOpen.isEmpty()) {
            getControlCli().stdout().println("no MidiDevice(s) to process");
        } else {
            toOpen.forEach(midiDevice -> action.accept(midiDevice));
        }
    }

    @Override
    public void run() {
        List<MidiDevice> openDevices = getControlCli().getControl().getMidiDeviceMgr().getMidiDevices().stream()
                .filter(midiDevice -> midiDevice.isOpen()).collect(Collectors.toList());
        if (openDevices.isEmpty()) {
            getControlCli().stdout().println("no open MidiDevices");
        } else {
            getControlCli().stdout().println("open MidiDevices:");
            openDevices.forEach(midiDevice -> printName(midiDevice));
        }
        List<MidiDevice> closedDevices = getControlCli().getControl().getMidiDeviceMgr().getMidiDevices().stream()
                .filter(midiDevice -> !midiDevice.isOpen()).collect(Collectors.toList());
        if (closedDevices.isEmpty()) {
            getControlCli().stdout().println("no closed MidiDevices");
        } else {
            getControlCli().stdout().println("closed MidiDevices:");
            closedDevices.forEach(midiDevice -> printName(midiDevice));
        }
    }

    protected void printName(MidiDevice midiDevice) {
        getControlCli().stdout().print("    \"");
        getControlCli().stdout().print(MidiDeviceMgr.getDisplayName(midiDevice));
        getControlCli().stdout().println("\"");
    }
}
