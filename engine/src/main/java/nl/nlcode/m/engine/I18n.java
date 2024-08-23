package nl.nlcode.m.engine;

import java.util.ResourceBundle;

/**
 *
 * @author jq59bu
 */
public class I18n {

    private static ResourceBundle MESSAGES = ResourceBundle.getBundle("nl.nlcode.m.engine.Messages");
    
    public static ResourceBundle msg() {
        return MESSAGES;
    }
}
