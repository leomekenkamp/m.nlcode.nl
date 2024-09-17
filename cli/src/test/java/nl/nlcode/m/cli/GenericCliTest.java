package nl.nlcode.m.cli;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;
import java.util.function.BooleanSupplier;;

/**
 *
 * @author jq59bu
 */
public class GenericCliTest extends CliTest {

    @Test
    public void testNoInput() {
        assertThat(execute(""), is(""));
    }

    @Test
    public void testExit() throws InterruptedException {
        assertThat(instanceThread.isAlive(), is(true));
        execute("exit");
        assertThat(controlCli.getApplicationStatus(), is(ApplicationStatus.EXIT));
        waitFor(() -> !instanceThread.isAlive(), 100);
        assertThat(instanceThread.isAlive(), is(false));
    }

}
