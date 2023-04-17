package nl.nlcode.m.engine;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.m.ui.MidiChannelMatrixUi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class MidiChannelMatrix extends MidiInOut {

    private static final long serialVersionUID = 0L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiChannelMatrixUi.class);

    private volatile AtomicBoolean fromTo[][] = new AtomicBoolean[16][16];

    public MidiChannelMatrix(Project project) {
        super(project);
        for (int from = 0; from < 16; from++) {
            for (int to = 0; to < 16; to++) {
                fromTo[from][to] = new AtomicBoolean();
            }
        }
        defaultFromTo();
    }

    public void defaultFromTo() {
        for (int fromTo = 0; fromTo < 16; fromTo++) {
            zeroBasedFromTo(fromTo, fromTo, true);
        }
    }

    public void clearFromTo() {
        for (int from = 0; from < 16; from++) {
            for (int to = 0; to < 16; to++) {
                zeroBasedFromTo(from, from, false);
            }
        }
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

    @Override
    public boolean isActiveSender() {
        return true;
    }

    public boolean zeroBasedFromTo(int from, int to) {
        return fromTo[from][to].get();
    }

    public void zeroBasedFromTo(int from, int to, boolean connected) {
        boolean was = fromTo[from][to].getAndSet(connected);
    }

    public boolean oneBasedFromTo(int from, int to) {
        return zeroBasedFromTo(from - 1, to - 1);
    }

    public void oneBasedFromTo(int from, int to, boolean connected) {
        zeroBasedFromTo(from - 1, to - 1, connected);
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage) {
            ShortMessage original = (ShortMessage) message;
            for (int to = 0; to < 16; to++) {
                if (MidiChannelMatrix.this.zeroBasedFromTo(original.getChannel(), to)) {
                    super.send(forChannel(to, original), timeStamp);
                }
            }
        } else {
            LOGGER.debug("non-channel message, relaying to super");
            super.send(message, timeStamp);
        }
    }

    private ShortMessage forChannel(int channel, ShortMessage original) {
        ShortMessage result;
        if (channel == original.getChannel()) {
            LOGGER.debug("using original for channel <{}>", channel);
            result = original;
        } else {
            try {
                LOGGER.debug("making copy for channel <{}>", channel);
                result = new ShortMessage(original.getCommand(), channel, original.getData1(), original.getData2());
            } catch (InvalidMidiDataException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }
}
