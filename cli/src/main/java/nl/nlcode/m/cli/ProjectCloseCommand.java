package nl.nlcode.m.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 *
 * @author jq59bu
 */
@Command(name = "close", description = "close a project")
public class ProjectCloseCommand extends WithProjectCommand<BaseCommand> implements Runnable {

    @CommandLine.Option(names = "--force", description = "immediately closes the project, without saving any changes")
    private boolean force;

    @Override
    public void run() {
        withProject(project -> {
            if (!project.close(force)) {
                getControlCli().commandOutput("project.dirty", project.getPath());
            }
        });
    }

}
