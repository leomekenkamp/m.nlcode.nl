package nl.nlcode.m.cli;

import picocli.CommandLine.ParentCommand;

/**
 *
 * @author jq59bu
 */
public abstract class ChildCommand<P extends CliArgument> implements CliArgument {

    @ParentCommand
    private P parent; // 'injected' by picocli

    public P getParent() {
        return parent;
    }
    
    public ControlCli getControlCli() {
        return parent.getControlCli();
    }
}
