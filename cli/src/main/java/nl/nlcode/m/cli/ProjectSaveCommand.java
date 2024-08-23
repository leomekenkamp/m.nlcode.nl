package nl.nlcode.m.cli;

import java.io.IOException;
import picocli.CommandLine.Command;

/**
 *
 * @author jq59bu
 */
@Command(name = "save", description = "write project to file")
public class ProjectSaveCommand extends WithProjectCommand<BaseCommand> implements Runnable {

    @Override
    public void run() {
        withProject(project -> {
            try {
                project.save();
            } catch (IOException e){
                getControlCli().commandOutput("save.error", project.getPath(), e.getClass().getSimpleName(), e.getMessage());
            }
        });
    }

}
