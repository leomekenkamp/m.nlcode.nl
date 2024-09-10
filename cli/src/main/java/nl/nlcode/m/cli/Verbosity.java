package nl.nlcode.m.cli;

import java.util.EnumSet;

/**
 *
 * @author jq59bu
 */
public enum Verbosity {

    newbie,
    informative,
    brief,
    minimal,
    ;
    
    public EnumSet<Verbosity> withFallback() {
        return switch (this) {
            case newbie:
                yield EnumSet.of(newbie, informative, brief, minimal);
            case informative:
                yield EnumSet.of(informative, brief, minimal);
            case brief:
                yield EnumSet.of(brief, minimal);
            case minimal:
                yield EnumSet.of(minimal);
        };
    }
    
}
