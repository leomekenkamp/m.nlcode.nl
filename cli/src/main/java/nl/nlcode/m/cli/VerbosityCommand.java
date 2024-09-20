package nl.nlcode.m.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 *
 * @author jq59bu
 */
@Command(name = "verbosity", description = "control chattyness of the application")
public class VerbosityCommand extends ChildCommand<BaseCommand> implements Runnable {

    @Parameters(paramLabel = "<verbosity>", description = "one of ${COMPLETION-CANDIDATES}", arity = "0..1")
    private Verbosity verbosity;

    @Override
    public void run() {
        if (verbosity == null) {
            getControlCli().printMessage("verbosity", getControlCli().getVerbosity().name());
        } else {
            getControlCli().setVerbosity(verbosity);
            getControlCli().printMessage("verbosity.set", verbosity);
        }
    }

}
