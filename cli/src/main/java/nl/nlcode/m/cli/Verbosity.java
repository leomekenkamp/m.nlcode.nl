package nl.nlcode.m.cli;

import java.util.EnumSet;

/**
 *
 * @author jq59bu
 */
public enum Verbosity {

    newbie,
    informative,
    minimal,
    ;
    
    public EnumSet<Verbosity> withFallback() {
        return switch (this) {
            case newbie:
                yield EnumSet.of(newbie, informative, minimal);
            case informative:
                yield EnumSet.of(informative, minimal);
            case minimal:
                yield EnumSet.of(minimal);
        };
    }
    
}
