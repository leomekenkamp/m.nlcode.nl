package nl.nlcode.m.cli;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jq59bu
 */
public class ListCommandTest extends CliTest {

    public ListCommandTest() {
        execute("verbosity minimal");
        if (!execute("project new").contains(filename("00"))) {
            fail("There is at least one " + filename("??") + " file in " + pwd() + ". Please delete them all.");
        }
    }

    @Test
    public void list_shows_open_midiInOut_instances() {
        execute("new lights");
        execute("new midiDeviceLink");
        String info = execute("list");
        assertThat(info, startsWith("\"" + pwd() + filename("00") + "\"\n"));
        assertThat(info, containsString("Lights \"lights_0\""));
        assertThat(info, containsString("MidiDeviceLink \"midiDeviceLink_0\""));
    }

    @Test
    public void list_shows_open_midiInOut_instancesin_other_project() {
        execute("new lights");
        execute("new midiDeviceLink");
        execute("project new");
        execute("project renum -i 1");
        String listSecondProject = execute("list");
        assertThat(listSecondProject, startsWith("\"" + pwd() + filename("01") + "\"\n"));
        assertThat(listSecondProject, not(containsString("Lights \"lights_0\"")));
        assertThat(listSecondProject, not(containsString("MidiDeviceLink \"midiDeviceLink_0\"")));
        
        String listFirstProject = execute("list --projectId 1");
        assertThat(listFirstProject, startsWith("\"" + pwd() + filename("00") + "\"\n"));
        assertThat(listFirstProject, containsString("Lights \"lights_0\""));
        assertThat(listFirstProject, containsString("MidiDeviceLink \"midiDeviceLink_0\""));
        
    }

}
