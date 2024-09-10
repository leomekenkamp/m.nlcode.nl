package nl.nlcode.m.cli;

import java.util.Arrays;
import nl.nlcode.m.engine.Control;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jq59bu
 */
public class ControlCliTest {

    private static final Object NO_PARAMS = new Object[]{};

    private ControlCli instance = ControlCli.createUnitTestInstance();

    @Nested
    class commandMessage {

        @Test
        public void falls_back_to_minimal() {
            instance.setVerbosity(Verbosity.newbie);
            assertThat(instance.commandMessage("__fallbackTest_minimal", NO_PARAMS), is("minimal"));
            instance.setVerbosity(Verbosity.informative);
            assertThat(instance.commandMessage("__fallbackTest_minimal", NO_PARAMS), is("minimal"));
            instance.setVerbosity(Verbosity.brief);
            assertThat(instance.commandMessage("__fallbackTest_minimal", NO_PARAMS), is("minimal"));
            instance.setVerbosity(Verbosity.minimal);
            assertThat(instance.commandMessage("__fallbackTest_minimal", NO_PARAMS), is("minimal"));
        }

        @Test
        public void falls_back_to_brief() {
            instance.setVerbosity(Verbosity.newbie);
            assertThat(instance.commandMessage("__fallbackTest_brief", NO_PARAMS), is("brief"));
            instance.setVerbosity(Verbosity.informative);
            assertThat(instance.commandMessage("__fallbackTest_brief", NO_PARAMS), is("brief"));
            instance.setVerbosity(Verbosity.brief);
            assertThat(instance.commandMessage("__fallbackTest_brief", NO_PARAMS), is("brief"));
            instance.setVerbosity(Verbosity.minimal);
            assertThat(instance.commandMessage("__fallbackTest_brief", NO_PARAMS), startsWith("???"));
        }

        @Test
        public void falls_back_to_informative() {
            instance.setVerbosity(Verbosity.newbie);
            assertThat(instance.commandMessage("__fallbackTest_informative", NO_PARAMS), is("informative"));
            instance.setVerbosity(Verbosity.informative);
            assertThat(instance.commandMessage("__fallbackTest_informative", NO_PARAMS), is("informative"));
            instance.setVerbosity(Verbosity.brief);
            assertThat(instance.commandMessage("__fallbackTest_informative", NO_PARAMS), startsWith("???"));
            instance.setVerbosity(Verbosity.minimal);
            assertThat(instance.commandMessage("__fallbackTest_informative", NO_PARAMS), startsWith("???"));
        }

        @Test
        public void falls_back_to_newbie() {
            instance.setVerbosity(Verbosity.newbie);
            assertThat(instance.commandMessage("__fallbackTest_newbie", NO_PARAMS), is("newbie"));
            instance.setVerbosity(Verbosity.informative);
            assertThat(instance.commandMessage("__fallbackTest_newbie", NO_PARAMS), startsWith("???"));
            instance.setVerbosity(Verbosity.brief);
            assertThat(instance.commandMessage("__fallbackTest_newbie", NO_PARAMS), startsWith("???"));
            instance.setVerbosity(Verbosity.minimal);
            assertThat(instance.commandMessage("__fallbackTest_newbie", NO_PARAMS), startsWith("???"));
        }
    }
}
