package nl.nlcode.m.cli;

import java.io.PrintWriter;
import java.util.SortedSet;

/**
 *
 * @author jq59bu
 */
public class HelpCommand extends Token {


    public HelpCommand(RootCommand parent) {
        super("help", parent);
    }

    @Override
    public void execute() {
        ControlCli controlCli = getControlCli();
        Token[] matches = getMatchedTokens();
        if (matches.length == 1) {
            PrintWriter writer = controlCli.stdout();
            writer.println(helpDirect("nl.nlcode.m.cli.HelpCommand_root"));
            SortedSet<Token> children = getParent().getChildren();
            for (Token command : children) {
                writer.println(command.getToken());
            }
        } else {
            matches[matches.length - 1].help(controlCli);
        }
    }

    @Override
    protected void verify() {
    }

    @Override
    protected void printHelpChildTokens(PrintWriter writer) {
    }

}
