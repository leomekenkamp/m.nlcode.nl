package nl.nlcode.m.cli;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private ProjectDefinition projectDefinition;

    protected static class OpenProjectIds implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            Set<Map.Entry<Integer, Project>> entries = ControlCli.getInstance().getIdToProject().entrySet();
            List<String> result = new ArrayList<>();
            for (Map.Entry<Integer, Project> entry : entries) {
                result.add("" + entry.getKey());
            }
            return result.iterator();
        }
    }

    protected static class OpenProjectNames implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            Set<Map.Entry<Integer, Project>> entries = ControlCli.getInstance().getIdToProject().entrySet();
            List<String> result = new ArrayList<>();
            for (Map.Entry<Integer, Project> entry : entries) {
                result.add("\"" + entry.getValue() + "\"");
            }
            return result.iterator();
        }
    }

    protected static class ProjectDefinition {

        @Option(names = {"--projectName", "-n"}, completionCandidates = OpenProjectNames.class, scope = ScopeType.INHERIT, required = false)
        protected String projectName;

        @Option(names = {"--projectId", "-i"}, completionCandidates = OpenProjectIds.class, scope = ScopeType.INHERIT, required = false)
        protected Integer projectId;

        @Option(names = {"--all", "-a"}, scope = ScopeType.INHERIT, required = false)
        protected boolean allProjects;
    }
    
    protected ProjectDefinition getProjectDefinition() {
        return projectDefinition;
    }
    
    public void withSelectedProjects(Consumer<Project> withProject) {
        if (projectDefinition == null) {
            Project project = getControlCli().getCurrentProject();
            if (project != null) {
                withProject.accept(project);
            }
        } else {
            if (projectDefinition.projectId != null) {
                Project project = getControlCli().getProjectById(projectDefinition.projectId);
                if (project == null) {
                    getControlCli().commandOutput("project.id.none", projectDefinition.projectId);
                } else {
                    withProject.accept(project);
                }
            } else if (projectDefinition.projectName != null) {
                Project project = getControlCli().getProjectByName(projectDefinition.projectName);
                if (project == null) {
                    getControlCli().commandOutput("project.name.none", projectDefinition.projectId);
                } else {
                    withProject.accept(project);                    
                }
            } else if (projectDefinition.allProjects) {
                getControlCli().getIdToProject().forEach((id, project) -> withProject.accept(project));
            }
        }
    }

}
