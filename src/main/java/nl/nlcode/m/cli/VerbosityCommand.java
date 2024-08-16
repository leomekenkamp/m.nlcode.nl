package nl.nlcode.m.cli;

import java.io.PrintWriter;
import static nl.nlcode.m.cli.Verbosity.informative;
import static nl.nlcode.m.cli.Verbosity.minimal;
import static nl.nlcode.m.cli.Verbosity.newbie;

/**
 *
 * @author jq59bu
 */
public class VerbosityCommand extends Token implements EnumValue.Parent<Verbosity> {

    private Verbosity verbosity;

    public VerbosityCommand(RootCommand parent) {
        super("verbosity", parent);
        addChild(new EnumValue(this, Verbosity.class));
    }

    @Override
    public void execute() {
        ControlCli controlCli = getControlCli();
        PrintWriter writer = controlCli.stdout();
        if (verbosity == null) {
            switch (controlCli.getVerbosity()) {
                case minimal:
                    writer.println(controlCli.getVerbosity().name());
                    break;
                case informative:
                    writer.println("Verbosity is currently set to <" + controlCli.getVerbosity().name() + ">.");
                    break;
                case newbie:
                    writer.println("Verbosity is currently set to <" + controlCli.getVerbosity().name() + ">.");
                    writer.println("You can use the command 'verbosity <value>' to change verbosity.");
                    break;
            }
        } else {
            controlCli.setVerbosity(verbosity);
            switch (verbosity) {
                case minimal:
                    break;
                case informative:
                    writer.println("Verbosity is now set to <" + controlCli.getVerbosity().name() + ">.");
                    break;
                case newbie:
                    writer.println("Verbosity is now set to <" + controlCli.getVerbosity().name() + ">.");
                    writer.println("This setting will give you the most information and will guide you in use of this application.\n"
                            + "You can check on the current verbosity value by simply typing 'verbosity' without quotes.");
                    break;
            }
        }
    }

    @Override
    public void setValue(Verbosity verbosity) {
        this.verbosity = verbosity;
    }

}
