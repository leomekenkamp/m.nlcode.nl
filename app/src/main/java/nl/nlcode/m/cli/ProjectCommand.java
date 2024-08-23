package nl.nlcode.m.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import static nl.nlcode.m.cli.Verbosity.informative;
import static nl.nlcode.m.cli.Verbosity.minimal;
import static nl.nlcode.m.cli.Verbosity.newbie;
import nl.nlcode.m.engine.Project;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 *
 * @author jq59bu
 */
@Command(name = "project", description="project manipulation commands")
public class ProjectCommand extends ChildCommand<BaseCommand> {
    
    //public static final String RESOURCE_BUNDLE = "nl.nlcode.m.cli.picocli";
    
    @Command(name = "new", description="create a new project from scratch")
    public void create() {
        getControlCli().getControl().createProject();
        getControlCli().commandOutput("new.done");
    }

    @Command(name = "list", description="display list of open projects")
    public void list() {
        Set<Map.Entry<Integer, Project>> entries = getControlCli().getIdToProject().entrySet();
        switch (getControlCli().getVerbosity()) {
            case newbie:
            case informative:
                if (entries.isEmpty()) {
                    getControlCli().stdout().println("There are no open projects.");
                } else {
                    getControlCli().stdout().println("List of open projects:");
                }
            case minimal:
                for (Map.Entry<Integer, Project> entry : entries) {
                    getControlCli().stdout().print(entry.getKey());
                    getControlCli().stdout().print(" ");
                    getControlCli().stdout().println(entry.getValue().getPath());
                }
        };
    }

    @Command(name = "renum", description="renumber / reindex the open projects")
    public void renum(@Parameters(paramLabel = "<project name>", description = "project name", arity = "0..1")
            Project project) {
        getControlCli().renumProjects(project);
    }
    
    @Command(name = "open", description="reads an existing project from file")
    public void open(
            @Parameters(paramLabel = "<project name>", description = "project name")
            Path projectPath
    ) throws IOException {
        getControlCli().getControl().loadProject(projectPath);
    }
    
    @Command(name = "save", description="write project to file")
    public void save() throws IOException {
        getProject().save();
    }
    
    @Command(name = "close", description="close project")
    public void close() throws IOException {
        getProject().close();
    }
    
    private Project getProject() {
        return getControlCli().getDefaultProject();
    }

}
