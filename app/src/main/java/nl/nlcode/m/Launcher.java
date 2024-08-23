package nl.nlcode.m;

import nl.nlcode.m.cli.CliApp;
import nl.nlcode.m.ui.FxApp;

/**
 * Main entry point for starting the application.
 *
 * @author jq59bu
 */
public class Launcher {

    public static void main(String... args) {
        args = new String[] {"--console"};
        if (args.length == 0 || args[0].contentEquals("--javafx")) {
            try {
                FxApp.main(args);
            } catch (RuntimeException e) {
                System.err.println("cannot start with command option --javafx (or no option); try --console instead");
            }
        } else if (args[0].contentEquals("--console")) {
            CliApp.main(args);
        }
    }
}
