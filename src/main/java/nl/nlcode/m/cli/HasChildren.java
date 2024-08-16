package nl.nlcode.m.cli;

import java.util.SortedSet;

/**
 *
 * @author jq59bu
 */
public interface HasChildren {

    SortedSet<Token> getChildren();

    void addChild(Token child);
    
}
