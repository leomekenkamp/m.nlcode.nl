package nl.nlcode.m.cli;

import nl.nlcode.m.engine.MidiInOut;
import nl.nlcode.m.engine.Project;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model.CommandSpec;

/**
 *
 * @author jq59bu
 */
@Command(name = "m.nlcode.nl>", version = "1.0alfa", subcommands = {
    ExitCommand.class,
    HelpCommand.class,
    MidiDeviceCommand.class,
    NewCommand.class,
    ProjectCommand.class,
    VerbosityCommand.class
})
public class BaseCommand implements Runnable, CliArgument {

    private ControlCli controlCli;

    public Project getProject() {
        return getControlCli().getCurrentProject();
    }

    @Override
    public ControlCli getControlCli() {
        return controlCli;
    }

    public BaseCommand(ControlCli controlCli) {
        this.controlCli = controlCli;
    }

    @Override
    public void run() {
    }

    public static CommandSpec createCommandSpec(ControlCli controlCli) {
        CommandSpec baseSpec = CommandLine.Model.CommandSpec.forAnnotatedObject(new BaseCommand(controlCli));
        Project project = controlCli.getCurrentProject(false);
        if (project != null) {
            for (MidiInOut midiInOut : project.getMidiInOutList()) {
                CommandSpec midiInOutSpec = MidiInOutCommand.createCommandSpec(midiInOut);

                baseSpec.addSubcommand(midiInOut.getName(), midiInOutSpec);
            }
        }
        return baseSpec;
    }
}
