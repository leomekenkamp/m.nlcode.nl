package nl.nlcode.m.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.MidiDevice;
import static nl.nlcode.m.cli.Verbosity.informative;
import static nl.nlcode.m.cli.Verbosity.minimal;
import static nl.nlcode.m.cli.Verbosity.newbie;
import nl.nlcode.m.engine.MidiDeviceMgr;
import nl.nlcode.m.engine.MidiInOut;
import nl.nlcode.m.engine.Project;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 *
 * @author jq59bu
 */
@Command(name = "project", description = "project manipulation commands or display list of MidiInOut instances\"",
        subcommands = {
            ProjectSaveCommand.class,
            ProjectCloseCommand.class,
            ProjectRenumCommand.class
        })
public class ProjectCommand extends ChildCommand<BaseCommand> implements Runnable {

    //public static final String RESOURCE_BUNDLE = "nl.nlcode.m.cli.picocli";
    @Command(name = "new", description = "create a new project from scratch")
    public void create() {
        getControlCli().getControl().createProject();
        getControlCli().commandOutput("new.done");
    }

    @Command(name = "list", description = "display list of open projects")
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

    @Command(name = "open", description = "reads an existing project from file")
    public void open(
            @Parameters(paramLabel = "<project name>", description = "project name") Path projectPath
    ) throws IOException {
        getControlCli().getControl().loadProject(projectPath);
    }

    private Project getProject() {
        return getControlCli().getDefaultProject();
    }

    @Override
    public void run() {
        if (getProject() == null) {
            getControlCli().commandOutput("project.none");
        } else {
            PrintWriter stdout = getControlCli().stdout();
            StringBuilder items = new StringBuilder();
            for (MidiInOut midiInOut : getProject().getMidiInOutLookup()) {
                items.append(getControlCli().commandMessage("project.midiInOut", midiInOut.getClass().getSimpleName(), midiInOut.getName()));
            }
            stdout.print(getControlCli().commandMessage("project", items.toString(), getProject().getPath()));
        }
    }

}
