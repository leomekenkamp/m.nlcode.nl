package nl.nlcode.m;

import java.util.prefs.Preferences;

/**
 *
 * @author jq59bu
 */
public class Prefs {
    
    private static Preferences forApp = Preferences.userNodeForPackage(Prefs.class);
        
    public static Preferences forApp() {
        return forApp;
    }
}
