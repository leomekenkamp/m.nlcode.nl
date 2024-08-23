package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import nl.nlcode.m.linkui.ObjectUpdater;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import nl.nlcode.marshalling.Marshalled;

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
public final class MidiDeviceLink<U extends MidiDeviceLink.Ui> extends MidiInOut<U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static record MidiDeviceChanged(MidiInOut source, MidiDevice newDevice) implements Event {

    }

    public static interface Ui extends MidiInOut.Ui {

        void midiDeviceChanged();

    }

    private final ObjectUpdater<MidiDevice, U, MidiDeviceLink<U>> midiDevice;

    private transient SendReceiver sendReceiver;

    private transient Transmitter transmitter;

    private transient Receiver receiver;

    public static record SaveData0(
            int id,
            String midiDeviceName,
            Marshalled<MidiInOut> s) implements Marshalled<MidiDeviceLink> {

        @Override
        public void unmarshalInto(Context context, MidiDeviceLink target) {
            s.unmarshalInto(context, target);
            if (midiDeviceName() == null) {
                LOGGER.debug("no midiDevice name, so not trying to find matching midiDevice");
            } else {
                // FIXME: is there an other way than through a static? s has been unmarshalled already...
                // Yes, can override startListening and use project to get to control.
                for (MidiDevice search : Control.getInstance().getMidiDeviceMgr().getOpenMidiDevices()) {
                    if (midiDeviceName().equals(MidiDeviceMgr.getDisplayName(search))) {
                        target.setMidiDevice(search);
                        LOGGER.debug("found match for <{}> in <{}>", midiDeviceName(), search);
                        break;
                    }
                }
                if (target.getMidiDevice() == null) {
                    LOGGER.warn("could not match <{}> to any of the open system midi devices", midiDeviceName());
                }
            }

        }

        @Override
        public MidiDeviceLink createMarshallable() {
            return new MidiDeviceLink();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                midiDevice.get() == null ? null : MidiDeviceMgr.getDisplayName(midiDevice.get()),
                super.marshalInternal(-1, context)
        );
    }

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

    public MidiDeviceLink() {
        sendReceiver = new SendReceiver();
        midiDevice = new ObjectUpdater<>(this, null);
        midiDevice.setAfterChange(this, ui -> ui.midiDeviceChanged());
    }

    public MidiDevice getMidiDevice() {
        return midiDevice.get();
    }

    public void setMidiDevice(MidiDevice midiDevice) {
        LOGGER.debug("setting midi device <{}>", MidiDeviceMgr.getDisplayName(midiDevice));
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
        if (midiDevice != null) {
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
                this.midiDevice.set(null);
                if (transmitter != null) {
                    transmitter.close();
                    transmitter = null;
                }
                if (receiver != null) {
                    receiver.close();
                    receiver = null;
                }
                throw new IllegalStateException(e);
            }
        }
        this.midiDevice.set(midiDevice);
        if (midiDevice != null) {
            toSendersRecursive(new MidiDeviceChanged(this, midiDevice));
        }
    }

    public boolean isActiveSender() {
        // Even if we are not linked. So you might think 'false' here if we are not linked.
        // However, we do want to end up in overviews of active senders, since we could get linked
        // to active senders at any time. If we get 'unlinked' for some reason, we do not want to
        // risk being removed somewhere; if we are 'relinked' things should work as before.
        return true;
    }

    @Override
    public boolean isActiveReceiver() {
        // Even if we are not linked. So you might think 'false' here if we are not linked.
        // However, we do want to end up in overviews of active receivers, since we could get linked
        // to active receivers at any time. If we get 'unlinked' for some reason, we do not want to
        // risk being removed somewhere; if we are 'relinked' things should work as before.
        return true;
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
            ShortMessage toSend = (ShortMessage) message.clone(); // Is this really necessary?
            receiver.send(toSend, timeStamp);
        }
    }

}
