package nl.nlcode.m.cli;

/**
 * The 'invisible' command that precedes any command on the cli.
 *
 * @author jq59bu
 */
public class RootCommand extends Token {

    private ControlCli controlCli;
    private String[] tokens;
    private Token[] matches;
    private boolean force;

    private HelpCommand helpCommand;

    private RootCommand() {
        super(null, null);
    }

    public static RootCommand createRootCommand() {
        RootCommand result = new RootCommand();
        result.setHelp(new HelpCommand(result));
        result.addChild(new VerbosityCommand(result));
        result.addChild(new ProjectCommand(result));
        Token exit = new Token.Builder(result)
                .command("exit")
                .execute(() -> result.getControlCli().exit(result.force))
                .create();
        new Token.Builder(exit)
                .option("force")
                .matched(() -> {result.force = true;})
                .create();
        return result;
    }

    @Override
    protected boolean matches(int tokenIndex) {
        if (tokenIndex != -1) {
            throw new IllegalArgumentException("RootCommand must have index -1");
            // RootCommand always matches at location -1, so the 'next' (actually the first entered) token will be at index 0.
        }
        return true; 
    }

    public final void execute() {
        for(Token token : matches) {
            token.execute();
        }
    }
    
    public void process(String[] tokens, ControlCli controlCli) {
        this.tokens = tokens;
        this.controlCli = controlCli;
        matches = new Token[tokens.length];
        parse(-1);
        verifyAllRawTokensMatched();
        verify();
        execute();
        this.controlCli = null;
    }

    private void verifyAllRawTokensMatched() {
        Token[] matchedTokens = getMatchedTokens();
        for (int i = 0; i < matchedTokens.length; i++) {
            if (matchedTokens[i] == null) {
                throw new Token.MissingAtException(getRawTokens(), i, getControlCli().getVerbosity());
            }
        }
    }
    public void setHelp(HelpCommand helpCommand) {
        this.helpCommand = helpCommand;
        addChild(helpCommand);
    }

    @Override
    public void addChild(Token child) {
        super.addChild(child);
        helpCommand.addChild(child);
    }

    @Override
    protected String[] getRawTokens() {
        return tokens;
    }

    @Override
    protected Token[] getMatchedTokens() {
        return matches;
    }

    protected int getDepth() {
        return -1;
    }

    protected String getHelpId() {
        return getClass().getPackageName();
    }

    @Override
    protected ControlCli getControlCli() {
        return controlCli;
    }
}
