package nl.nlcode.m.cli;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jq59bu
 */
public class VerbosityCommandTest extends CliTest {

    
    public VerbosityCommandTest() {
        execute("project new");
    }
    
    @Test
    public void newbie_is_default() {
        assertThat(execute("verbosity"), startsWith("Verbosity is currently set to \"newbie\".\nThis setting will"));
    }

    @Test
    public void can_be_set_to_minimal() {
        assertThat(execute("verbosity minimal"), is(EMPTY_RESPONSE));
        assertThat(execute("verbosity"), startsWith("minimal"));
    }

    @Test
    public void can_be_set_to_brief() {
        assertThat(execute("verbosity brief"), startsWith("verbosity \"brief\""));
        assertThat(execute("verbosity"), startsWith("brief"));
    }

    @Test
    public void can_be_set_to_informative() {
        assertThat(execute("verbosity informative"), startsWith("Verbosity is now set to <informative>."));
        assertThat(execute("verbosity"), startsWith("Verbosity is currently set to <informative>."));
    }


}
