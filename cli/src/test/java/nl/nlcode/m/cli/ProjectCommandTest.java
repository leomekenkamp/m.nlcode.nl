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
public class ProjectCommandTest extends CliTest {

    @Test
    public void testProject() {
        assertThat(execute("project"), is(""));
    }

    @Test
    public void testProjectList_empty() {
        assertThat(execute("project list"), is("There are no open projects.\n"));
    }

    @Test
    public void testProjectNew() {
        String out = execute("project new");
        assertThat(out, startsWith("A project has been opened:"));
        assertThat(out, containsString("noname"));
    }

    @Test
    public void testProjectList() {
        execute("project new");
        execute("project new");
        String listOutput = execute("project list");
        assertThat(listOutput, startsWith("List of open projects:"));
        assertThat(listOutput, containsString("0 " + pwd() + "noname"));
        assertThat(listOutput, containsString("1 " + pwd() + "noname"));
        assertThat(listOutput, not(containsString("2 " + pwd() + "noname")));
    }

    @Test
    public void testProjectClose_ProjectList() {
        execute("project new");
        execute("project new");
        execute("project new");
        String closeOutput = execute("project close");
        assertThat(closeOutput, startsWith("A project has been closed: <" + pwd() + "noname"));
        String listOutput = execute("project list");
        assertThat(listOutput, startsWith("List of open projects:"));
        assertThat(listOutput, containsString("1 " + pwd() + "noname"));
        assertThat(listOutput, containsString("2 " + pwd() + "noname"));
    }

    @Test
    public void testProjectClose_id() {
        execute("project new");
        execute("project new");
        execute("project new");
        String closeOutput = execute("project close --id 1");
        assertThat(closeOutput, startsWith("A project has been closed: <" + pwd() + "noname01.m>."));
        String listOutput = execute("project list");
        assertThat(listOutput, containsString("0 " + pwd() + "noname"));
        assertThat(listOutput, not(containsString("1 " + pwd() + "noname")));
        assertThat(listOutput, containsString("2 " + pwd() + "noname"));
    }

    @Test
    public void testProjectClose_id_missing() {
        execute("project new");
        execute("project new");
        execute("project new");
        String closeOutput = execute("project close --id");
        assertThat(closeOutput, startsWith(
                "missing required token after position 3\n"
                + "              ↓↓↓↓\n"
                + "project close --id\n"
                + "              ↑↑↑↑\n"
                + "Please use the command 'help' without quotes to get help on using this application.\n"));

        String listOutput = execute("project list");
        assertThat(listOutput, startsWith("List of open projects:"));
        assertThat(listOutput, containsString("0 " + pwd() + "noname00.m"));
        assertThat(listOutput, containsString("1 " + pwd() + "noname01.m"));
        assertThat(listOutput, containsString("2 " + pwd() + "noname02.m"));
    }

    @Test
    public void testProjectClose_both_name_and_id() {
        execute("project new");
        String closeOutput = execute("project close --name " + pwd() + "noname00.m --id 0");
        assertThat(closeOutput, startsWith("bla"));
        String listOutput = execute("project list");
        assertThat(listOutput, containsString("0 " + pwd() + "noname00.m"));
    }

    @Test
    public void testProjectClose_both_id_and_name() {
        String out = execute("project new");
        assertThat(out, startsWith("A project has been opened: <" + pwd() + "noname00.m>."));
        String s = execute("project list");
        System.out.println(s);
        String closeOutput = execute("project close --id 0 --name " + pwd() + "noname00.m");
        assertThat(closeOutput, startsWith(
                "invalid token on position 5; can only specify project once\n"
                + "                     ↓↓↓↓↓↓\n"
                + "project close --id 0 --name /Users/jq59bu/noname00.m\n"
                + "                     ↑↑↑↑↑↑\n"));

        String listOutput = execute("project list");
        assertThat(listOutput, containsString("0 " + pwd() + "noname00.m"));
    }

    @Test
    public void testProjectClose_name_missing() {
        execute("project new");
        execute("project new");
        execute("project new");
        String closeOutput = execute("project close --name");
        assertThat(closeOutput, startsWith(
                "missing required token after position 3\n"
                + "              ↓↓↓↓↓↓\n"
                + "project close --name\n"
                + "              ↑↑↑↑↑↑\n"
                + "Please use the command 'help' without quotes to get help on using this application.\n"));

        String listOutput = execute("project list");
        assertThat(listOutput, containsString("0 " + pwd() + "noname00.m"));
        assertThat(listOutput, containsString("1 " + pwd() + "noname01.m"));
        assertThat(listOutput, containsString("2 " + pwd() + "noname02.m"));
    }

    @Test
    public void testProjectClose_name() {
        execute("project new");
        execute("project new");
        execute("project new");
        String closeOutput = execute("project close --name " + pwd() + "noname01.m");
        assertThat(closeOutput, startsWith("A project has been closed: <" + pwd() + "noname01.m>."));

        String listOutput = execute("project list");
        assertThat(listOutput, startsWith("List of open projects:"));
        assertThat(listOutput, containsString("0 " + pwd() + "noname00.m"));
        assertThat(listOutput, not(containsString("1 " + pwd() + "noname01.m")));
        assertThat(listOutput, containsString("2 " + pwd() + "noname02.m"));
    }

    @Test
    public void testProjectRenum() {
        execute("project new");
        execute("project new");
        execute("project new");
        execute("project new");
        execute("project new");
        String closeNameOutput = execute("project close --name " + pwd() + "noname03.m");
        String closeIdOutput = execute("project close");

        execute("project renum");
        String listOutput = execute("project list");
        assertThat(listOutput, not(containsString(pwd() + "noname00.m")));
        assertThat(listOutput, containsString("0 " + pwd() + "noname01.m"));
        assertThat(listOutput, containsString("1 " + pwd() + "noname02.m"));
        assertThat(listOutput, not(containsString(pwd() + "noname03.m")));
        assertThat(listOutput, containsString("2 " + pwd() + "noname04.m"));
    }

    @Test
    public void testProjectSave_ProjectLoad() throws IOException {
        String out = execute("project new");
        assertThat(out, startsWith("A project has been opened: <" + pwd() + "noname00.m>."));
        assertThat(Files.exists(Paths.get(pwd() + "noname00.m")), is(false));
        execute("project save");
        assertThat(Files.exists(Paths.get(pwd() + "noname00.m")), is(true));
        
        String open = execute("project open " + pwd() + "noname00.m");
        assertThat(open, startsWith("A project has been opened: <" + pwd() + "noname00.m>."));
        execute("project close");
        
        Files.delete(Paths.get(pwd() + "noname00.m"));
    }

}
