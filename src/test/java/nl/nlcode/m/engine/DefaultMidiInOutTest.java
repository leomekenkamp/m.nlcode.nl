package nl.nlcode.m.engine;

import java.util.Random;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
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
        Control control = Control.getInstance();
        project = control.createProject();
    }

    @AfterAll
    public static void tearDownClass() {
        project.close();
    }

    protected abstract T createInstance();

    @BeforeEach
    public void setUp() {
        instance = createInstance();
        defaultTestIn = new DebugMidiInOut(project);
        defaultTestOut = new DebugMidiInOut(project);
        defaultTestIn.startSendingTo(instance);
        instance.startSendingTo(defaultTestOut);
    }

    @AfterEach
    public void tearDown() {
        instance.close();
        defaultTestIn.close();
        defaultTestOut.close();
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
        return random.nextInt(126) + 1;
    }

    public int randomData1() {
        return random.nextInt(127);
    }

    public int randomData2() {
        return random.nextInt(127);
    }

}
