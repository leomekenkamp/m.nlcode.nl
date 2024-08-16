package nl.nlcode.m.cli;

import java.lang.invoke.MethodHandles;
import nl.nlcode.m.engine.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jq59bu
 */
public class CliApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    
    public static void main(String... args) {
        Control control = new Control(false);
        ControlCli controlCli = ControlCli.createInstance(Control.getInstance());
        controlCli.run();
    }

}
