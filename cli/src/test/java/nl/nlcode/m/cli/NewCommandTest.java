package nl.nlcode.m.cli;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
        assertThat(execute("list"), not(containsString("Lights \"lights_0\"")));
        
        assertThat(execute("new lights"), is("\"lights_0\"\n"));
        
        assertThat(execute("list"), containsString("lights \"lights_0\""));
    }

    @Test
    public void new_creates_a_new_named_miniInOut_device() {
        assertThat(execute("list"), not(containsString("lights \"lights_0\"")));
        assertThat(execute("list"), not(containsString("lights \"xyzzy\"")));
        assertThat(execute("list"), not(containsString("\"xyzzy\"")));

        assertThat(execute("new lights xyzzy"), is("\"xyzzy\"\n"));

        assertThat(execute("list"), not(containsString("lights \"lights_0\"")));
        assertThat(execute("list"), containsString("lights \"xyzzy\""));
    }

    @Test
    public void new_creates_a_new_spaced_named_miniInOut_device() {
        assertThat(execute("new lights \"gordon sumner\""), is("\"gordon sumner\"\n"));
        assertThat(execute("list"), containsString("lights \"gordon sumner\""));
    }


}
