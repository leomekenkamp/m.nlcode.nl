package nl.nlcode.m.cli;

import picocli.CommandLine.Command;

/**
 *
 * @author jq59bu
 */
@Command(name = "renum", description = "renumber / re-index the open projects (index 0 is the default project)")
public class ProjectRenumCommand extends WithProjectCommand<BaseCommand> implements Runnable {

    @Override
    public void run() {
        if (getProjectDefinition() == null || (getProjectDefinition().projectId == null && getProjectDefinition().projectName == null)) {
            getControlCli().renumProjects(null);
        } else {
            withSelectedProjects(project -> getControlCli().renumProjects(project));
        }
    }

}
