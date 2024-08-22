package nl.nlcode.m.cli;

import java.util.Map;
import nl.nlcode.m.engine.Project;
import picocli.CommandLine.ITypeConverter;

/**
 *
 * @author jq59bu
 */
public class ConvNameToProject implements ITypeConverter<Project> {

    private ControlCli controlCli;
    
    public ConvNameToProject(ControlCli controlCli) {
        this.controlCli = controlCli;
    }
    
    @Override
    public Project convert(String path) throws Exception {
        for (Map.Entry<Integer, Project> entry : controlCli.getIdToProject().entrySet()) {
            if (entry.getValue().getPath().toString().equals(path)) {
                return entry.getValue();
            }
        }
        return null;
    }

}
