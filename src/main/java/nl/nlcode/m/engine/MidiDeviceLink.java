package nl.nlcode.m.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Links a MIDI device that exists outside of this application to an internal MIDI device. Acts like
 * a bridge between the world outside the JVM and the devices inside this application. Note that the
 * device used can change during the lifetime of an instance, e.g. when {@link setMidiDevice} is
 * called.
 * <p>
 * While 'outside' devices may 'suddenly' disappear, instances of this class will always remain.
 * They can also be persisted. When de-serialized it will try to reconnect to the device with the
 * same name. This indirection helps in portability of projects over time and over different
 * computers.
 *
 * @author leo
 */
public final class MidiDeviceLink extends MidiInOut {

    private static final long serialVersionUID = 0L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiDeviceLink.class);

    private static final MidiMessageFormat MIDI_FORMAT = new MidiMessageFormat();

    private class SendReceiver implements Receiver {

        @Override
        public void send(MidiMessage message, long timeStamp) {
            // This is what we receive from the delegate, so this must be send out to all 'sendingTo'
            // instances.
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("received from system midi device: <{}>", MIDI_FORMAT.format(message));
            }
            MidiDeviceLink.this.send(message, -1);
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("Not supported");
        }
    };

    private transient SendReceiver sendReceiver;

    private transient MidiDevice midiDevice;

    private transient Transmitter transmitter;

    private transient Receiver receiver;

    private String midiDeviceName;

    public MidiDeviceLink(Project project) {
        super(project);
        serializationInit();
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        serializationInit();
        if (midiDeviceName == null) {
            LOGGER.debug("no midiDeviceName, so not trying to find matching midiDevice");
        } else {
            // FIXME: is there an other way than through a static?
            for (MidiDevice search : MidiDeviceMgr.getInstance().getMidiDevices()) {
                if (midiDeviceName.equals(MidiDeviceMgr.getDisplayName(search))) {
                    setMidiDevice(search);
                    LOGGER.debug("found match for <{}> in <{}>", midiDeviceName, midiDevice);
                    break;
                }
            }
            if (midiDevice == null) {
                LOGGER.warn("could not match <{}> to any of the open system midi devices", midiDeviceName);
            }
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void serializationInit() {
        sendReceiver = new SendReceiver();
    }

    public MidiDevice getMidiDevice() {
        return midiDevice;
    }

    public void setMidiDevice(MidiDevice midiDevice) {
        LOGGER.debug("setting midi device <{}>", MidiDeviceMgr.getDisplayName(midiDevice));
        if (this.midiDevice != null) {
        }
        if (receiver != null) {
            LOGGER.debug("closing <{}> on <{}>", receiver, MidiDeviceMgr.getDisplayName(midiDevice));
            receiver.close();
            receiver = null;
        }
        if (transmitter != null) {
            LOGGER.debug("closing <{}> on <{}>", transmitter, MidiDeviceMgr.getDisplayName(midiDevice));
            transmitter.close();
            transmitter = null;
        }
        this.midiDevice = midiDevice;
        if (this.midiDevice == null) {
            midiDeviceName = null;
        } else {
            midiDeviceName = MidiDeviceMgr.getDisplayName(midiDevice);
            try {
                if (midiDevice.getMaxReceivers() != 0) {
                    LOGGER.debug("opening receiver for <{}>", MidiDeviceMgr.getDisplayName(midiDevice));
                    receiver = midiDevice.getReceiver();
                }
                if (midiDevice.getMaxTransmitters() != 0) {
                    LOGGER.debug("opening transmitter for <{}>", MidiDeviceMgr.getDisplayName(midiDevice));
                    transmitter = midiDevice.getTransmitter();
                    transmitter.setReceiver(sendReceiver);
                }
            } catch (MidiUnavailableException e) {
                // FIXME: must be refactored, because this is too late
                // exception must be thrown earlier in this method, before the internal state has been
                // changed; it is now fubar when this exception occurs
                throw new IllegalStateException(e);
            }
        }
    }

    public boolean isActiveSender() {
        if (midiDevice == null) {
            // We are not linked, so you might think 'false' here, since we are not doing anything.
            // However, we do want to end up in overviews of active senders, since we could get linked
            // to an active senders at any time. If we get 'unlinked' for some reason, we do not want to
            // risk being removed somewhere; if we are 'relinked' things should work as before.
            return true;
        } else {
            return transmitter != null;
        }
    }

    @Override
    public boolean isActiveReceiver() {
        if (midiDevice == null) {
            // We are not linked, so you might think 'false' here, since we are not doing anything.
            // However, we do want to end up in overviews of active receivers, since we could get linked
            // to an active receiver at any time. If we get 'unlinked' for some reason, we do not want to
            // risk being removed somewhere; if we are 'relinked' things should work as before.
            return true;
        } else {
            return receiver != null;
        }
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        if (receiver == null) {
            LOGGER.info("eating message in <{}>: <{}> <{}>", getName(), message, timeStamp);
        } else {
            // What we receive must actually be send to our delegate; the Java MIDI api is a bit weird.
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("actually relaying to a receiver: <{}>, <{}>", MIDI_FORMAT.format(message), timeStamp);
            }
            ShortMessage toSend = (ShortMessage) message.clone();
            receiver.send(toSend, timeStamp);
        }
    }

}
