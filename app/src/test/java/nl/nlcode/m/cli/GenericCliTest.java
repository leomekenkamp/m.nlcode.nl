package nl.nlcode.m.cli;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

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
        assertThat(instance.getApplicationStatus(), is(ApplicationStatus.EXIT));
        Thread.sleep(1); // Crude way to give thread some time to end.
        assertThat(instanceThread.isAlive(), is(false));
    }

    @Test
    public void testInvalidCommand1() throws InterruptedException {
        assertThat(execute("qdfqpbirh"), is(
                "missing or illegal value on position 1\n"
                + "↓↓↓↓↓↓↓↓↓\n"
                + "qdfqpbirh\n"
                + "↑↑↑↑↑↑↑↑↑\n"
                + "Please use the command 'help' without quotes to get help on using this application.\n"));
    }

    @Test
    public void testInvalidCommand2() throws InterruptedException {
        assertThat(execute("qdfqpbirh sdfg"), is(
                "missing or illegal value on position 1\n"
                + "↓↓↓↓↓↓↓↓↓\n"
                + "qdfqpbirh sdfg\n"
                + "↑↑↑↑↑↑↑↑↑\n"
                + "Please use the command 'help' without quotes to get help on using this application.\n"));
    }

}
