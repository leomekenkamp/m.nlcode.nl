package nl.nlcode.m.cli;

import nl.nlcode.m.engine.Project;
import picocli.CommandLine.ITypeConverter;

/**
 *
 * @author jq59bu
 */
public class ConvIdToProject implements ITypeConverter<Project> {

    private ControlCli controlCli;
    
    public ConvIdToProject(ControlCli controlCli) {
        this.controlCli = controlCli;
    }
    
    @Override
    public Project convert(String id) throws Exception {
        return controlCli.getIdToProject().get(Integer.valueOf(id));
    }

}
