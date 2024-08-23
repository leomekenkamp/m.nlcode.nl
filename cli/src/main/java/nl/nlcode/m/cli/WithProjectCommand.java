package nl.nlcode.m.cli;

import java.util.Map;
import java.util.function.Consumer;
import nl.nlcode.m.engine.Project;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

/**
 *
 * @author jq59bu
 */
public abstract class WithProjectCommand<P extends CliArgument> extends ChildCommand {

    @ArgGroup(exclusive = true)
    ProjectDefinition projectDefinition;

    protected static class ProjectDefinition {

        @Option(names = "--projectName", scope = ScopeType.INHERIT)
        protected String name;

        @Option(names = "--projectId", scope = ScopeType.INHERIT)
        protected Integer id;
    }

    protected Project getProject() {
        Project result = null;
        if (projectDefinition != null) {
            if (projectDefinition.id != null) {
                result = getControlCli().getIdToProject().get(projectDefinition.id);
                if (result == null) {
                    getControlCli().commandOutput("project.id.none", projectDefinition.id);
                }
            } else if (projectDefinition.name != null) {
                result = getControlCli().getProjectByName(projectDefinition.name);
                if (result == null) {
                    getControlCli().commandOutput("project.name.none", projectDefinition.id);
                }
            }
        } else {
            result = getControlCli().getCurrentProject();
        }
        return result;
    }
    
    public void withProject(Consumer<Project> withProject) {
        Project project = getProject();
        if (project != null) {
            withProject.accept(project);
        }
    }

}
