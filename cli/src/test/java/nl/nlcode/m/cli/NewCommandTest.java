package nl.nlcode.m.cli;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jq59bu
 */
public class NewCommandTest extends CliTest {

    public NewCommandTest() {
        execute("verbosity minimal");
        execute("project new");
    }
    
    @Test
    public void new_creates_a_new_miniInOut_device() {
        assertThat(execute("new lights"), is("\"lights_0\"\n"));
        assertThat(execute("project"), is("Lights \"lights_0\"\n"));
    }


}
