package nl.nlcode.m.cli;

import java.io.PrintWriter;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * verbosity [minimal | informative | newbie]
 *
 * help
 *
 * exit [--force]
 *
 * project new close [--name <name> | --index <num>] [--force] save [--name
 * <name> | --index <num>] saveAs [--name <name> | --index <num>] <new name>
 * list index [--name <name> | --index <num>] <new index>
 *
 * midiinout midiDeviceLink new delete [--name <name> | --index <num>] [--force]
 * index [--name <name> | --index <num>] <new index>
 * list
 *
 * linkout [--name <name> | --index <num>] to [--name <name> | --index <num>]
 * linkin [--name <name> | --index <num>] from [--name <name> | --index <num>]
 * unlinkout [--name <name> | --index <num>] to [--name <name> | --index <num>]
 * unlinkin [--name <name> | --index <num>] from [--name <name> | --index <num>]
 * show [mididevice] set mididevice <name>
 *
 * @author jq59bu
 */
public class Token {

    protected static ResourceBundle HELP_MESSAGES = ResourceBundle.getBundle("nl.nlcode.m.cli.HelpMessages");

    public static enum Type {
        COMMAND,
        OPTION,
        VARIABLE,
    }

    public static class Builder {

        private Token result;

        public Builder(Token parent) {
            result = new Token(parent);
        }

        public Builder matches(Function<String, Boolean> matches) {
            result.matches = matches;
            return this;
        }

        public Builder execute(Runnable execute) {
            result.execute = execute;
            return this;
        }

        public Builder matched(Runnable matched) {
            result.matched = matched;
            return this;
        }

        public Builder token(String token) {
            result.token = token;
            return this;
        }

        public Builder type(Type type) {
            result.type = type;
            return this;
        }

        public Builder command(String token) {
            token(token);
            type(Type.COMMAND);
            return this;
        }

        public Builder fixed(String token) {
            token(token);
            return this;
        }

        public Builder variable(String description) {
            token("<" + description + ">");
            type(Type.VARIABLE);
            return this;
        }

        public Builder option(String token) {
            token("--" + token);
            type(Type.OPTION);
            return this;
        }

        
        public Builder mustBeMatched() {
            result.mustBeMatched = true;
            return this;
        }

        public Builder mustHaveMatchedChild() {
            result.mustHaveMatchedChild = true;
            return this;
        }

        public Token create() {
            result.getParent().addChild(result);
            return result;
        }
    }

    public static abstract class TokenException extends RuntimeException {

        private static String markToken(String[] tokens, int tokenIndex) {
            int startCharPos = 0;
            int startIndex;
            for (startIndex = 0; startIndex < tokenIndex; startIndex++) {
                startCharPos += tokens[startIndex].length() + 1;
            }
            int endCharPos = startCharPos + tokens[startIndex].length();
            StringBuilder startLine = new StringBuilder();
            StringBuilder endLine = new StringBuilder();
            for (int i = 0; i < startCharPos; i++) {
                startLine.append(' ');
                endLine.append(' ');
            }
            for (int i = startCharPos; i < endCharPos; i++) {
                startLine.append('↓');
                endLine.append('↑');
            }
            StringBuilder result = new StringBuilder("\n");
            result.append(startLine).append("\n");
            StringJoiner joiner = new StringJoiner(" ");
            for (int i = 0; i < tokens.length; i++) {
                joiner.add(tokens[i]);
            }
            result.append(joiner.toString()).append("\n");
            result.append(endLine);
            return result.toString();
        }

        private static String markToken(String[] tokens, int tokenIndex, Verbosity verbosity) {
            return switch (verbosity) {
                case minimal:
                    yield "";
                case informative:
                    yield markToken(tokens, tokenIndex);
                case newbie:
                    yield markToken(tokens, tokenIndex) + "\n"
                    + "Please use the command 'help' without quotes to get help on using this application.";
                //+ "You can also try to use the <tab> key for command completion.";

            };
        }

        public TokenException(String message, String[] tokens, int tokenIndex, Verbosity verbosity) {
            super(message + markToken(tokens, tokenIndex, verbosity));
        }
    }

    public static class MissingAtException extends TokenException {

        public MissingAtException(String[] tokens, int tokenIndex, Verbosity verbosity) {
            super("missing or illegal value on position " + (1 + tokenIndex), tokens, tokenIndex, verbosity);
        }
    }

    public static class MissingAfterException extends TokenException {

        public MissingAfterException(String[] tokens, int tokenIndex, Verbosity verbosity) {
            super("missing required token after position " + (1 + tokenIndex), tokens, tokenIndex, verbosity);
        }
    }

    public static class InvalidException extends TokenException {

        public InvalidException(String msg, String[] tokens, int tokenIndex, Verbosity verbosity) {
            super("invalid token on position " + (1 + tokenIndex) + "; " + msg, tokens, tokenIndex, verbosity);
        }
    }

    public static class UnexpectedException extends TokenException {

        public UnexpectedException(String msg, String[] tokens, int tokenIndex, Verbosity verbosity) {
            super(msg, tokens, tokenIndex, verbosity);
        }
    }

    private Type type;
    
    private String token;

    private String match;

    private int matchIndex = Integer.MIN_VALUE;

    private Token parent;

    private boolean mustBeMatched;
    
    private boolean mustHaveMatchedChild;
        
    private Function<String, Boolean> matches = (token) -> {
        return token.contentEquals(getToken());
    };

    private Runnable matched = () -> {
    };
    
    private Runnable execute = () -> {
    };

    private final SortedSet<Token> children = new TreeSet<>(
            (Token o1, Token o2) -> o1.getToken().compareTo(o2.getToken())
    );

    private Token(Token parent) {
        this.parent = parent;
    }

    protected Token(String token, Token parent) {
        this.token = token;
        this.parent = parent;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    protected ControlCli getControlCli() {
        return getParent().getControlCli();
    }

    public String getToken() {
        return token;
    }

    protected void setToken(String token) {
        this.token = token;
    }

    public Type getType() {
        return type;
    }
    
    /**
     * @return the actual part of the command line that was matched on this
     * token (or null if not matched)
     */
    public String getMatch() {
        return match;
    }

    /**
     * @return the index of the actual part of the command line that was matched
     * on this token (or Integer.MIN_VALUE if not matched)
     */
    public int getMatchIndex() {
        return matchIndex;
    }

    public boolean isMatched() {
        return matchIndex != Integer.MIN_VALUE;
    }

    public Token getParent() {
        return parent;
    }

    protected void addChild(Token child) {
        children.add(child);
    }

    protected boolean matches(int tokenIndex) {
        boolean result = false;
        String[] tokens = getRawTokens();
        if (tokenIndex < tokens.length && matches(tokens[tokenIndex])) {
            Token[] matches = getMatchedTokens();
            if (matches[tokenIndex] != null) {
                throw new IllegalStateException("tokenIndex " + tokenIndex + " was already matched");
            }
            matches[tokenIndex] = this;
            match = tokens[tokenIndex];
            matchIndex = tokenIndex;
            matched();
            result = true;
        }
        return result;
    }

    protected void matched() {
        matched.run();
    }

    protected boolean matches(String token) {
        return matches.apply(token);
    }

    protected final void parse(int tokenIndex) {
        if (!isMatched() && matches(tokenIndex)) {
            for (int childTokenIndex = tokenIndex + 1; childTokenIndex < getRawTokens().length; childTokenIndex++) {
                if (getMatchedTokens()[childTokenIndex] != null) {
                    continue;
                }
                // childTokenIndex now points at first unmatched token
                for (Token child : children) {
                    child.parse(childTokenIndex);
                }
            }
        }
    }

    /**
     * Recursively verifies<ul>
     * <li>{@code mustHaveMatchedChild} on all matched tokens</li>
     * <li>{@code mustBeMatched} on _all_ children that have a matched parent</li>
     * <ul>
     */
    protected void verify() {
        if (isMatched()) {
            if (mustHaveMatchedChild()) {
                if (!getChildren().stream().anyMatch(child -> child.isMatched())) {
                    throw new Token.MissingAfterException(getRawTokens(), getMatchIndex(), getControlCli().getVerbosity());
                }
            }
        } else if (mustBeMatched() && getParent().isMatched()) {
            throw new Token.MissingAfterException(getRawTokens(), getParent().getMatchIndex(), getControlCli().getVerbosity());
        }
        for (Token child : getChildren()) {
            child.verify();
        }
    }

    protected boolean mustHaveMatchedChild() {
        return mustHaveMatchedChild;
    }

    protected void setMustHaveMatchedChild() {
        this.mustHaveMatchedChild = true;
    }

    protected boolean mustBeMatched() {
        return mustBeMatched;
    }

    protected void setMustBeMatched() {
        mustBeMatched = true;
    }

    protected SortedSet<Token> getChildren() {
        return children;
    }

    protected void printHelpChildTokens(PrintWriter writer, Token.Type type, String header) {
        boolean headerPrinted = false;
        for (Token child : getChildren()) {
            if (child.getType() == type) {
                if (!headerPrinted) {
                    writer.println(header);
                    headerPrinted = true;
                }
                writer.println(child.getToken());
            }
        }
    }

    protected void printHelpChildTokens(PrintWriter writer) {
        printHelpChildTokens(writer, Type.COMMAND, "possible values:");
        printHelpChildTokens(writer, Type.OPTION, "possible options:");
        //printHelpChildTokens(stdout, Type.VARIABLE, "possible subcommands:");
    }

    protected String getHelpId() {
        return parent.getHelpId() + "." + getClass().getSimpleName();
    }

    protected String helpDirect(String key) {
        return HELP_MESSAGES.getString(key);
    }

    protected String getHelpDescription() {
        try {
            return HELP_MESSAGES.getString(getHelpId());
        } catch (MissingResourceException e) {
            return "";
        }
    }

    protected void help(ControlCli controlCli) {
        controlCli.stdout().println(getHelpDescription());
        Token[] matches = getMatchedTokens();
        if (matches.length == getDepth() + 2) {
            PrintWriter writer = controlCli.stdout();
            printHelpChildTokens(writer);
        }
    }

    protected String[] getRawTokens() {
        return getParent().getRawTokens();
    }

    protected Token[] getMatchedTokens() {
        return getParent().getMatchedTokens();
    }

    /**
     * Depth is defines as the tree distance to the first command that the user
     * types. E.g. in "help --force verbosity newbie" depth of "help" is 0, of
     * "verbosity" and "--force" is 1 and "newbie" is 2.
     *
     * @return distance to first command on cli
     */
    protected int getDepth() {
        return getParent().getDepth() + 1;
    }

    public void execute() {
        execute.run();
    }

}
