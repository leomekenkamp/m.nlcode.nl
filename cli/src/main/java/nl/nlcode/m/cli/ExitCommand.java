package nl.nlcode.m.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *
 * @author jq59bu
 */
@Command(name = "exit", description = "exits the application")
public class ExitCommand extends ChildCommand<BaseCommand> implements Runnable {

    @Option(names = "--force", description = "immediately exits, without saving any changes")
    private boolean force;

    @Option(names = {"-s", "--saveAll"}, description = "saves all open and changed projects before exiting")
    private boolean saveAll;

    @Override
    public void run() {
        boolean exit = true;
        if (saveAll) {
            exit = getControlCli().saveAllDirtyProjects();
        }
        if (exit) {
            getControlCli().exit(force);
        }
    }

}
