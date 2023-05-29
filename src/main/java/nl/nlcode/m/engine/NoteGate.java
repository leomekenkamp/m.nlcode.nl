package nl.nlcode.m.engine;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class NoteGate extends MidiInOut {

    private static final long serialVersionUID = 0L;

    private static final Logger LOGGER = LoggerFactory.getLogger(NoteGate.class);

    private static final MidiMessageFormat MIDI_FORMAT = new MidiMessageFormat();

    private IntInterval velocityInterval = new IntInterval();
    
    public NoteGate() {
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        boolean sendMessage = true;
        if (message instanceof ShortMessage shortMessage) {
            if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
                sendMessage = velocityInterval.contains(shortMessage.getData2());
            }
        }
        if (sendMessage) {
            send(message, timeStamp);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("gating <{}>", MIDI_FORMAT.format(message));
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

    public int getFromVelocity() {
        return velocityInterval.getLow();
    }

    public void setFromVelocity(int fromVelocity) {
        if (fromVelocity < 0 || fromVelocity > 127) {
            throw new IllegalArgumentException("fromVelocity must be >= 0 and <= 127");
        }
        velocityInterval.setLow(fromVelocity);
    }

    public int getToVelocity() {
        return velocityInterval.getHigh();
    }

    public void setToVelocity(int toVelocity) {
        if (toVelocity < 0 || toVelocity > 127) {
            throw new IllegalArgumentException("toVelocity must be >= 0 and <= 127");
        }
        velocityInterval.setHigh(toVelocity);
    }

    public IntervalClosure getIntervalClosure() {
        return velocityInterval.getIntervalClosure();
    }

    public void setIntervalClosure(IntervalClosure intervalClosure) {
        velocityInterval.setIntervalClosure(intervalClosure);
    }


}
