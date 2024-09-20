package nl.nlcode.m.cli;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jq59bu
 */
public class MidiInOutCommandTest extends CliTest {

    public MidiInOutCommandTest() {
        execute("verbosity minimal");
        execute("project new");
    }

    @Test
    public void rename() {
        execute("new lights \"one\"");
        assertThat(execute("list"), containsString("one"));
        assertThat(execute("list"), not(containsString("two")));
        execute("one rename two");
        assertThat(execute("list"), not(containsString("one")));
        assertThat(execute("list"), containsString("two"));
    }

    @Test
    public void connect() {
        execute("new midiDeviceLink one");
        execute("new midiDeviceLink two");
        assertThat(execute("one connect"), is(""));
        execute("one connect two");
        assertThat(execute("one connect"), is("\nmidiDeviceLink \"two\"\n"));
    }

    @Test
    public void connect_does_not_allow_circular_links() {
        execute("new midiDeviceLink one");
        execute("new midiDeviceLink two");
        execute("new midiDeviceLink three");
        execute("new midiDeviceLink four");
        execute("verbosity brief");
        assertThat(execute("one connect two"), startsWith("midiDeviceLink \"one\" ━━━▶ midiDeviceLink \"two\""));
        assertThat(execute("two connect three"), startsWith("midiDeviceLink \"two\" ━━━▶ midiDeviceLink \"three\""));
        assertThat(execute("three connect four"), startsWith("midiDeviceLink \"three\" ━━━▶ midiDeviceLink \"four\""));
        assertThat(execute("four connect one"), not(containsString("midiDeviceLink \"four\" ━━━▶ midiDeviceLink \"one\"")));
    }

}
