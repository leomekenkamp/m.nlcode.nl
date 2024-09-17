package nl.nlcode.m.cli;

import java.util.HashSet;
import java.util.Set;
import nl.nlcode.m.engine.MidiInOut;
import nl.nlcode.m.engine.Project;
import nl.nlcode.m.linkui.IntUpdater;
import nl.nlcode.m.linkui.Updater;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 *
 * @author jq59bu
 */
@Command(description = "manipulates the midiInOut instance with this name")
public class MidiInOutCommand<M extends MidiInOut> extends ChildCommand<BaseCommand> implements Runnable {

    @Command(name = "connect", description = "Either shows output connections to other midiInOut instances (no parameter), or connects a sending midiInOut instances to a receiving one.")
    public static class ConnectCommand extends ChildCommand<MidiInOutCommand> implements Runnable {

        @Command(description = "name of the receiving MidiInOut instance")
        public static class ReceiverCommand extends ChildCommand<ConnectCommand> implements Runnable {

            @Spec
            CommandSpec commandSpec;

            @Override
            public void run() {
                getParent().connect(commandSpec.name());
            }
        }

//        @Option(names = {"--reverse", "-r"}, description = "Show or set input instead of output connections.")
        // Does not work (yet) because the receivers would be different.
        private boolean reverse;

        public static CommandSpec createCommandSpec(MidiInOut midiInOut) {
            CommandSpec result = CommandSpec.forAnnotatedObject(new ConnectCommand());
            Set<MidiInOut> currentReceivers = midiInOut.sendingTo();
            for (MidiInOut other : midiInOut.getProject().getMidiInOutList()) {
                if (!currentReceivers.contains(other) && !other.isRecursiveSendingTo(midiInOut)) {
                    result.addSubcommand(other.getName(), CommandSpec.forAnnotatedObject(new ReceiverCommand()));
                }
            }
            return result;
        }

        private void connect(String secondName) {
            MidiInOut first = getParent().getMidiInOut();
            MidiInOut second = getParent().lookup(getControlCli().getCurrentProject(), secondName);
            if (reverse) {
                second.startSendingTo(first);
            } else {
                first.startSendingTo(second);
            }
        }

        @Override
        public void run() {
            getParent().printConnected(reverse);
        }
    }

    @Command(name = "disconnect", description = "Disconnects a sending midiInOut instance from to a receiving ones.")
    public static class DisconnectCommand extends ChildCommand<MidiInOutCommand> implements Runnable {

        @Command(description = "name of the receiving MidiInOut instance")
        public static class ReceiverCommand extends ChildCommand<DisconnectCommand> implements Runnable {

            @Spec
            CommandSpec commandSpec;

            @Override
            public void run() {
                getParent().disconnect(commandSpec.name());
            }
        }

//        @Option(names = {"--reverse", "-r"}, description = "Remove input instead of output connections.")
        // Does not work (yet) because the receivers would be different.
        private boolean reverse;

        public static CommandSpec createCommandSpec(MidiInOut midiInOut) {
            CommandSpec result = CommandSpec.forAnnotatedObject(new DisconnectCommand());
            Set<MidiInOut> receivers = midiInOut.sendingTo();
            for (MidiInOut receiver : receivers) {
                result.addSubcommand(receiver.getName(), CommandSpec.forAnnotatedObject(new ReceiverCommand()));
            }
            return result;
        }

        private void disconnect(String secondName) {
            MidiInOut first = getParent().getMidiInOut();
            MidiInOut second = lookup(getControlCli().getCurrentProject(), secondName);
            if (reverse) {
                second.stopSendingTo(first);
            } else {
                first.stopSendingTo(second);
            }
        }

        private MidiInOut lookup(Project project, String name) {
            return getParent().lookup(project, name);
        }

        @Override
        public void run() {
            getParent().printConnected(reverse);
        }
    }

    @Spec
    private CommandSpec commandSpec;

    private MidiInOutCommand() {
    }

    private void printConnected(boolean reverse) {
        final Set<MidiInOut> others = reverse ? getMidiInOut().receivingFrom() : getMidiInOut().sendingTo();
        for (MidiInOut r : others) {
            getControlCli().stdout().println(r.getName());
        }
    }

    private MidiInOut lookup(Project project, String name) {
        MidiInOut result = project.getMidiInOutLookup().get(name);
        if (result == null) {
            getControlCli().commandOutput("MidiInOut.name.none", name, project.getPath());
        }
        return result;
    }

    /**
     * @return {@code MidiInOut#getName}
     */
    private String getName() {
        return commandSpec.name();
    }

    public M getMidiInOut() {
        return (M) getControlCli().getCurrentProject().getMidiInOutLookup().get(getName());
    }

    @Command(name = "rename", description = "give the MidiInOut instance a different name")
    public void rename(
            @Parameters(paramLabel = "<new name>", description = "new name for the MidiInOut instance") String newName) {
        getMidiInOut().setName(newName);
    }

    public static CommandSpec createCommandSpec(MidiInOut midiInOut) {
        CommandSpec result = CommandSpec.forAnnotatedObject(new MidiInOutCommand());
        Iterable<Updater> allUpdaters = midiInOut.getAllUpdaters();
        for (Updater updater : allUpdaters) {
            if (updater instanceof IntUpdater intUpdater) {
                CommandSpec updaterSpec = CommandSpec.forAnnotatedObject(new IntUpdaterCommand(intUpdater));
                result.addSubcommand(updater.getName(), updaterSpec);
            }
        }
        result.addSubcommand("connect", CommandSpec.forAnnotatedObject(ConnectCommand.createCommandSpec(midiInOut)));
        result.addSubcommand("disconnect", CommandSpec.forAnnotatedObject(DisconnectCommand.createCommandSpec(midiInOut)));
        return result;
    }

    public void run() {
        getControlCli().stdout().println("settings:");
        Iterable<Updater> allUpdaters = getMidiInOut().getAllUpdaters();
        for (Updater updater : allUpdaters) {
            getControlCli().stdout().println(updater.getName() + ": " + updater.getValue());
        }
    }
}
