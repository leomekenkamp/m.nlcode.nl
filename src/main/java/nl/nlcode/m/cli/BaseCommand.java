package nl.nlcode.m.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

/**
 *
 * @author jq59bu
 */
@Command(name = "m.nlcode.nl>", version = "1.0alfa",
subcommands = {
    HelpCommand.class,
    ProjectCommand.class,
    MidiDeviceCommand.class,
    NewCommand.class,
    RenameCommand.class,
    VerbosityCommand.class,
    ConnectCommand.class,
    DisconnectCommand.class,
    ListCommand.class
}) 
public class BaseCommand implements Runnable, CliArgument { 

    private ControlCli controlCli;

    public BaseCommand() {
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

}
