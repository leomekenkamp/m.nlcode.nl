package nl.nlcode.m.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import nl.nlcode.m.engine.Control;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jq59bu
 */
public class ProjectCommandTest extends CliTest {

    public ProjectCommandTest() {
        if (!execute("project new").contains(filename("00"))) {
            fail("There is at least one " + filename("??") + " file in " + pwd() + ". Please delete them all.");
        }
        execute("project close");
    }

    @Test
    public void testProject() {
        assertThat(execute("project"), startsWith("There is no default project"));
    }

    @Test
    public void project_list_starts_empty() {
        assertThat(execute("project list"), is("There are no open projects.\n"));
    }

    @Test
    public void project_new_creates_a_project() {
        String out = execute("project new");
        assertThat(out, startsWith("A project has been opened:"));
        assertThat(out, containsString(filename("00")));
    }

    @Test
    public void project_shows_information() {
        execute("project new");
        execute("new lights");
        execute("new midiDeviceLink");
        execute("verbosity minimal");
        String info = execute("project");
        assertThat(info, startsWith("\"" + pwd() + filename("00") + "\"\n"));
        assertThat(info, containsString("-- midiInOut\n"));
        assertThat(info, containsString("Lights \"lights_0\""));
        assertThat(info, containsString("MidiDeviceLink \"midiDeviceLink_0\""));
    }

    @Test
    public void project_list_shows_open_projects() {
        execute("project new");
        execute("project new");
        String listOutput = execute("project list");
        assertThat(listOutput, startsWith("List of open projects:"));
        assertThat(listOutput, containsString("0 " + pwd() + filename("00")));
        assertThat(listOutput, containsString("1 " + pwd() + filename("01")));
        assertThat(listOutput, not(containsString("2 " + pwd() + filename("02"))));
    }

    @Test
    public void close_default_project() {
        execute("project new");
        execute("project new");
        execute("project new");
        String closeOutput = execute("project close");
        assertThat(closeOutput, startsWith("A project has been closed: <" + pwd() + filename("00") + ">"));
        String listOutput = execute("project list");
        assertThat(listOutput, startsWith("List of open projects:"));
        assertThat(listOutput, containsString("1 " + pwd() + filename("01")));
        assertThat(listOutput, containsString("2 " + pwd() + filename("02")));
    }

    @Test
    public void close_project_by_id() {
        execute("project new");
        execute("project new");
        execute("project new");
        String closeOutput = execute("project close --projectId 1");
        assertThat(closeOutput, startsWith("A project has been closed: <" + pwd() + filename("01") + ">"));
        String listOutput = execute("project list");
        assertThat(listOutput, containsString("0 " + pwd() + filename("00")));
        assertThat(listOutput, not(containsString("1 " + pwd() + filename("01"))));
        assertThat(listOutput, containsString("2 " + pwd() + filename("02")));
    }

    @Test
    public void close_project_by_name() {
        execute("project new");
        execute("project new");
        execute("project new");
        String closeOutput = execute("project close --projectName " + pwd() + filename("01"));
        assertThat(closeOutput, startsWith("A project has been closed: <" + pwd() + filename("01") + ">."));

        String listOutput = execute("project list");
        assertThat(listOutput, startsWith("List of open projects:"));
        assertThat(listOutput, containsString("0 " + pwd() + filename("00")));
        assertThat(listOutput, not(containsString("1 " + pwd() + filename("01"))));
        assertThat(listOutput, containsString("2 " + pwd() + filename("02")));
    }

    @Test
    public void close_all_projects() {
        execute("project new");
        execute("project new");
        execute("project new");
        String closeOutput = execute("project close --all");
        assertThat(closeOutput, containsString("A project has been closed: <" + pwd() + filename("00") + ">"));
        assertThat(closeOutput, containsString("A project has been closed: <" + pwd() + filename("01") + ">"));
        assertThat(closeOutput, containsString("A project has been closed: <" + pwd() + filename("02") + ">"));
        String listOutput = execute("project list");
        assertThat(listOutput, not(containsString("0 " + pwd() + filename("00"))));
        assertThat(listOutput, not(containsString("1 " + pwd() + filename("01"))));
        assertThat(listOutput, not(containsString("2 " + pwd() + filename("02"))));
    }

    @Test
    public void renum_projects_after_removals() {
        execute("project new");
        execute("project new");
        execute("project new");
        execute("project new");
        execute("project new");
        String closeNameOutput = execute("project close --projectName " + pwd() + filename("03"));
        String closeIdOutput = execute("project close");

        execute("project renum");
        String listOutput = execute("project list");
        assertThat(closeIdOutput, containsString(pwd() + filename("00")));
        assertThat(listOutput, not(containsString(pwd() + filename("00"))));
        assertThat(listOutput, containsString("0 " + pwd() + filename("01")));
        assertThat(listOutput, containsString("1 " + pwd() + filename("02")));
        assertThat(closeNameOutput, containsString(pwd() + filename("03")));
        assertThat(listOutput, not(containsString(pwd() + filename("03"))));
        assertThat(listOutput, containsString("2 " + pwd() + filename("04")));
    }

    @Test
    public void renum_can_set_default_project() {
        execute("project new");
        execute("project new");
        execute("project new");
        execute("project renum -i 1");
        String listOutput = execute("project list");
        assertThat(listOutput, startsWith("List of open projects:\n0 " + pwd() + filename("01")));
        assertThat(listOutput, containsString("1 " + pwd() + filename("00")));
        assertThat(listOutput, containsString("2 " + pwd() + filename("02")));
    }

    @Test
    public void project_can_be_saved_and_reloaded() throws IOException {
        String out = execute("project new");
        assertThat(out, startsWith("A project has been opened: <" + pwd() + filename("00") + ">."));
        assertThat(Files.exists(Paths.get(pwd() + filename("00"))), is(false));
        execute("project save");
        try {
            assertThat(Files.exists(Paths.get(pwd() + filename("00"))), is(true));
            execute("project close");

            String open = execute("project open " + pwd() + filename("00"));
            assertThat(open, startsWith("A project has been opened: <" + pwd() + filename("00") + ">."));
            execute("project close");
        } finally {
            Files.delete(Paths.get(pwd() + filename("00")));
        }
    }

    // @Test FIXME either allow or disallow double opening
    public void project_cannot_be_opened_twice() throws IOException {
        String out = execute("project new");
        assertThat(out, startsWith("A project has been opened: <" + pwd() + filename("00") + ">."));
        assertThat(Files.exists(Paths.get(pwd() + filename("00"))), is(false));
        execute("project save");
        try {
            assertThat(Files.exists(Paths.get(pwd() + filename("00"))), is(true));

            String open = execute("project open " + pwd() + filename("00"));
            assertThat(open, not(containsString("A project has been opened: <" + pwd() + filename("00") + ">.")));
            execute("project close");
        } finally {
            Files.delete(Paths.get(pwd() + filename("00")));
        }
    }

}
