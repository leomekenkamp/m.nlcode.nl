package nl.nlcode.m.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author leo
 */
public abstract class DefaultMidiInOutTest<T extends MidiInOut> {

    protected static Project project;

    protected T instance;

    protected DebugMidiInOut defaultTestIn;

    protected DebugMidiInOut defaultTestOut;

    protected Random random = new Random();

    @BeforeAll
    public static void setUpClass() {
        Control control = new Control(true);
        project = control.createProject();
    }

    @AfterAll
    public static void tearDownClass() {
        project.close();
    }

    protected abstract T createInstance();

    @BeforeEach
    public void setUp() {
        if (instance != null) {
            throw new IllegalStateException();
        }
        instance = createInstance();
        instance.openWith(project);
        defaultTestIn = new DebugMidiInOut(project);
        defaultTestOut = new DebugMidiInOut(project);
        defaultTestIn.startSendingTo(instance);
        instance.startSendingTo(defaultTestOut);
        
        assertThat(instance.getProject().getControl().getProjectCount(), is(1));
        assertThat(instance.getProject().getMidiInOutLookup().size(), is(3));
        assertThat(defaultTestIn.sendingTo().size(), is(1));
        assertThat(defaultTestIn.sendingTo().contains(instance), is(true));
        assertThat(instance.sendingTo().size(), is(1));
        assertThat(instance.sendingTo().contains(defaultTestOut), is(true));
        assertThat(defaultTestOut.sendingTo().isEmpty(), is(true));
        assertThat(instance.getProject().getMidiInOutList().size(), is(3));
        assertThat(instance.getProject().getMidiInOutLookup().size(), is(3));
    }

    @AfterEach
    public void tearDown() {
        instance.close();
        defaultTestIn.close();
        defaultTestOut.close();
        instance = null;
        defaultTestIn = null;
        defaultTestOut = null;
        assertThat(project.getMidiInOutList().size(), is(0));
        assertThat(project.getMidiInOutLookup().size(), is(0));
    }

    public DefaultMidiInOutTest() {
    }

    public void settle() {
        settle(150);
    }

    public void settle(int msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException ignore) {
            Thread.interrupted();
        }
    }

    public int randomTimeStamp() {
        return random.nextInt(200);
    }

    public ShortMessage randomNoteOn() throws InvalidMidiDataException {
        return new ShortMessage(ShortMessage.NOTE_ON, randomChannel(), randomData1(), randomData2());
    }

    public ShortMessage randomNoteOff() throws InvalidMidiDataException {
        return new ShortMessage(ShortMessage.NOTE_OFF, randomChannel(), randomData1(), randomData2());
    }

    public int randomChannel() {
        return random.nextInt(16);
    }

    public int randomNote() {
        return random.nextInt(127);
    }

    public int randomData1() {
        return random.nextInt(127);
    }

    public int randomData2() {
        return random.nextInt(127);
    }

    public void clearBuffers() {
        defaultTestIn.clearReceived();
        defaultTestOut.clearReceived();
    }

    protected Path getTestFilePath() {
        return Path.of("deleteme_" + getClass().getName());
    }

    protected void persistAndLoad(boolean defaultAssertions) throws IOException {
        project.saveAs(getTestFilePath());
        project.close();
        instance = null;
        defaultTestIn = null;
        defaultTestOut = null;
        project = new Control(true).loadProject(getTestFilePath());
        Files.delete(getTestFilePath());

        // RISKY: order may change...
        defaultTestIn = (DebugMidiInOut) project.getMidiInOutList().get(0);
        defaultTestOut = (DebugMidiInOut) project.getMidiInOutList().get(1);
        instance = (T) project.getMidiInOutList().get(2);

        if (defaultAssertions) {
            assertThat(instance.sendingTo().size(), is(1));
            assertThat(defaultTestIn.sendingTo().size(), is(1));
            assertThat(defaultTestOut.sendingTo().size(), is(0));
        }
    }
}
