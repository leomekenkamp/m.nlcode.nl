package nl.nlcode.m.cli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import static nl.nlcode.m.cli.Verbosity.informative;
import static nl.nlcode.m.cli.Verbosity.minimal;
import static nl.nlcode.m.cli.Verbosity.newbie;
import nl.nlcode.m.engine.Project;

/**
 *
 * @author jq59bu
 */
public class ProjectCommand extends Token {

    public static class ProjectUnspecifiedException extends Token.TokenException {

        public ProjectUnspecifiedException(String[] tokens, int tokenIndex, Verbosity verbosity) {
            super(switch (verbosity) {
                case minimal:
                    yield "no default project";
                case informative:
                    yield "You did not specify a project and there is no default project.";
                case newbie:
                    yield "You did not specify a project and there is no default project."
                    + "\nEither specify a project name or project id (--id and --name), or set a project on index 0 to make that the default.";
            }, tokens, tokenIndex, verbosity);
        }
    }

    private boolean commandSet;

    private Project project;

    private Path projectPath;

    public ProjectCommand(Token parent) {
        super("project", parent);
        setMustHaveMatchedChild();

        new Token.Builder(this)
                .command("new")
                .execute(() -> doNew())
                .create();

        new Token.Builder(this)
                .command("list")
                .execute(() -> doList())
                .create();

        new Token.Builder(this)
                .command("renum")
                .execute(() -> getControlCli().renumProjects())
                .create();

        Token open = new Token.Builder(this)
                .command("open")
                .execute(() -> {
                    try {
                        getControlCli().getControl().loadProject(getProjectPath());
                    } catch (IOException e) {
                        String msg = e.getMessage();
                        getControlCli().stdout().println(msg);
                    }
                })
                .create();

        new Token.Builder(open)
                .variable("<project path>")
                .mustBeMatched()
                .matches((token) -> {
                    setProjectPath(Paths.get(token));
                    return Files.exists(getProjectPath());
                })
                .create();

        new Token.Builder(this)
                .command("save")
                .execute(() -> {
                    try {
                        getProject().save();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .create();

        Token close = new Token.Builder(this)
                .command("close")
                .execute(() -> getProject().close())
                .create();

        Token closeId = new Token.Builder(close)
                .option("id")
                .create();

        new Token.Builder(closeId)
                .variable("project id")
                .mustBeMatched()
                .matches((token) -> {
                    try {
                        int id = Integer.parseInt((String) token);
                        Project project = getControlCli().getIdToProject().get(id);
                        if (project == null) {
                            throw new Token.InvalidException("no project with that id", getRawTokens(), closeId.getMatchIndex() + 1, getControlCli().getVerbosity());
                        } else {
                            setProject(project, closeId.getMatchIndex());
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        throw new Token.InvalidException("should be an integer", getRawTokens(), closeId.getMatchIndex(), getControlCli().getVerbosity());
                    }
                })
                .create();

        Token closeName = new Token.Builder(close)
                .option("name")
                .create();

        new Token.Builder(closeName)
                .variable("project name")
                .mustBeMatched()
                .matches((token) -> {
                    for (Project project : getControlCli().getIdToProject().values()) {
                        if (project.getPath().toString().contentEquals((String) token)) {
                            setProject(project, closeName.getMatchIndex());
                            return true;
                        }
                    }
                    return false;
                })
                .create();

    }

    private void setProjectPath(Path projectPath) {
        this.projectPath = projectPath;
    }

    private Path getProjectPath() {
        return projectPath;
    }

    public void doNew() {
        Project project = getControlCli().getControl().createProject();
        switch (getControlCli().getVerbosity()) {
            case minimal:
                break;
            case informative:
                break;
            case newbie:
                getControlCli().stdout().println("Note that a file with the same path does not yet exist: you need to save first with 'project save'.");
                break;
        }
    }

    public void doList() {
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

    private Project getProject() {
        Project result = project;
        if (result == null) {
            result = getControlCli().getDefaultProject();
        }
        if (result == null) {
            throw new ProjectUnspecifiedException(getRawTokens(), getMatchIndex(), getControlCli().getVerbosity());
        } else {
            return result;
        }
    }

    private void setProject(Project project, int matchIndex) {
        if (this.project != null) {
            throw new Token.InvalidException("can only specify project once", getRawTokens(), matchIndex, getControlCli().getVerbosity());
        }
        this.project = project;
    }

}
