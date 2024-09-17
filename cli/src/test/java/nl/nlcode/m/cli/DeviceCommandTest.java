package nl.nlcode.m.cli;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jq59bu
 */
public class DeviceCommandTest extends CliTest {

    public DeviceCommandTest() {
        execute("verbosity minimal");
        execute("project new");
    }
    
    @Test
    public void device_open_shows_open_devices_and_opens_devices() {
        assertThat(execute("device open"), not(containsString("\"OpenJDK Gervill (receiver)\"")));
        execute("device open -n \"OpenJDK Gervill (receiver)\""); // TODO: make -n optional? If possible with completion and --all option...
        assertThat(execute("device open"), containsString("\"OpenJDK Gervill (receiver)\""));
    }
    
    @Test
    public void device_close_closes_a_device() {
        execute("device open -n \"OpenJDK Gervill (receiver)\"");
        assertThat(execute("device open"), containsString("\"OpenJDK Gervill (receiver)\""));
        execute("device close -n \"OpenJDK Gervill (receiver)\"");
        assertThat(execute("device open"), not(containsString("\"OpenJDK Gervill (receiver)\"")));
    }

}
