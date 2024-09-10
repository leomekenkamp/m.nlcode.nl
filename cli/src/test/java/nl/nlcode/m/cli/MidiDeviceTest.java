package nl.nlcode.m.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jq59bu
 */
public class MidiDeviceTest extends CliTest {

    
    public MidiDeviceTest() {
        execute("project new");
    }
    
    @Test
    public void open_opens_a_device() {
    }


}
