package nl.nlcode.m.cli;

import java.util.Arrays;
import java.util.List;
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
    
    @Nested
    class commandList {

        @Test
        public void empty_with_empty() {
            instance.setVerbosity(Verbosity.newbie);
            assertThat(instance.commandList("__listTest", List.of("overall"), List.of(), (item) -> List.of("s")), is("empty overall"));
        }
        
        @Test
        public void list_one_item() {
            instance.setVerbosity(Verbosity.newbie);
            assertThat(instance.commandList("__listTest", List.of("overall"), List.of("x"), (item) -> List.of(item + "1", item + "2")), 
                    is("list overall item_x1 item_x2 "));
        }
        
        @Test
        public void list_three_item() {
            instance.setVerbosity(Verbosity.newbie);
            assertThat(instance.commandList("__listTest", List.of("overall"), List.of("x", "y", "z"), (item) -> List.of(item + "1", item + "2")),
                    is("list overall item_x1 item_x2 item_y1 item_y2 item_z1 item_z2 "));
        }
        
        @Test
        public void empty_without_empty() {
            instance.setVerbosity(Verbosity.newbie);
            assertThat(instance.commandList("__listTestNoEmpty", List.of("overall"), List.of(), (item) -> List.of("s")), is("list overall "));
        }
        
        @Test
        public void list_no_empty_two_item() {
            instance.setVerbosity(Verbosity.newbie);
            assertThat(instance.commandList("__listTest", List.of("overall"), List.of("x", "y"), (item) -> List.of(item + "1", item + "2")),
                    is("list overall item_x1 item_x2 item_y1 item_y2 "));
        }
        
        @Test
        public void newline_handling() {
            assertThat(instance.commandList("__listTestNewlines", List.of(), List.of("1", "2"), (item) -> List.of(item)),
                    is("list\nitem 1\nitem 2"));
        }        
        
    }
}
