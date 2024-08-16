package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

/**
 * @author leo
 */
public final class MidiDeviceMgr {

    private static class NoneMidiDevice implements MidiDevice {

        private class DefaultInfo extends MidiDevice.Info {

            public DefaultInfo(String name) {
                super(name, null, null, null);
            }
        }

        private MidiDevice.Info info;

        public NoneMidiDevice(String name) {
            info = new DefaultInfo(name);
        }

        @Override
        public MidiDevice.Info getDeviceInfo() {
            return info;
        }

        @Override
        public void open() throws MidiUnavailableException {
            throw new UnsupportedOperationException("This is a NULL item.");
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("This is a NULL item.");
        }

        @Override
        public boolean isOpen() {
            throw new UnsupportedOperationException("This is a NULL item.");
        }

        @Override
        public long getMicrosecondPosition() {
            throw new UnsupportedOperationException("This is a NULL item.");
        }

        @Override
        public int getMaxReceivers() {
            return 0;
        }

        @Override
        public int getMaxTransmitters() {
            return 0;
        }

        @Override
        public Receiver getReceiver() throws MidiUnavailableException {
            throw new UnsupportedOperationException("This is a NULL item.");
        }

        @Override
        public List<Receiver> getReceivers() {
            throw new UnsupportedOperationException("This is a NULL item.");
        }

        @Override
        public Transmitter getTransmitter() throws MidiUnavailableException {
            throw new UnsupportedOperationException("This is a NULL item.");
        }

        @Override
        public List<Transmitter> getTransmitters() {
            throw new UnsupportedOperationException("This is a NULL item.");
        }

    }

    public static MidiDevice NONE_MIDI_DEVICE = new NoneMidiDevice(I18n.msg().getString("none"));

    public interface Listener {

        void midiDeviceAdded(MidiDevice added);

        void midiDeviceRemoved(MidiDevice added);

        void midiDeviceOpened(MidiDevice added);

        void midiDeviceClosed(MidiDevice added);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final Comparator<? super MidiDevice> COMPARE_BY_DISPLAY_NAME = new Comparator<>() {
        @Override
        public int compare(MidiDevice o1, MidiDevice o2) {
            if (o1 == o2) {
                return 0;
            } else if (o1 == NONE_MIDI_DEVICE) {
                return -1;
            } else if (o2 == NONE_MIDI_DEVICE) {
                return 1;
            }
            return getDisplayName(o1).compareTo(getDisplayName(o2));
        }
    };

    private final boolean USE_COREMIDI4j = true;

    private final List<MidiDevice> midiDevices = new ArrayList<>();

    private final List<MidiDevice> openMidiDevices = new ArrayList<>();

    private static MidiDeviceMgr instance;

    private static final int MAX_PREFS_KEY_AND_NAME_LENGTH = 80;

    private Set<Listener> listeners = new HashSet<>();

    private MidiDeviceMgr() {
        if (USE_COREMIDI4j) {
            try {
                CoreMidiDeviceProvider.addNotificationListener(() -> refreshMidiDevices());
            } catch (CoreMidiException e) {
                LOGGER.error("Could not register listener. No auto refresh of MIDI (usb) devices.");
            }
        }
    }

    public void addListener(Listener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public static synchronized MidiDeviceMgr getInstance() {
        if (instance == null) {
            instance = new MidiDeviceMgr();
        }
        return instance;
    }

    public MidiDevice.Info[] getMidiDeviceInfo() {
        return USE_COREMIDI4j
                ? CoreMidiDeviceProvider.getMidiDeviceInfo()
                : MidiSystem.getMidiDeviceInfo();
    }

    public final void refreshMidiDevices() {
        Collection<String> errors = new ArrayList<>();
        MidiDevice.Info[] midiDeviceInfos = getMidiDeviceInfo();
        System.out.println("0y");
        List<MidiDevice> newList = new ArrayList();
        int retried = 0;
        System.out.println("0b");

        for (MidiDevice.Info info : midiDeviceInfos) {
            System.out.println(1);
            LOGGER.info("adding/refreshing <{}>", info);
            try {
                MidiDevice midiDevice = MidiSystem.getMidiDevice(info);
                newList.add(midiDevice);
                retried = 0;
            } catch (MidiUnavailableException | IllegalArgumentException e) {
                LOGGER.error("could not get midi device <{}>", info);
                LOGGER.error("caught", e);
                errors.add(e.getMessage() + " (" + info + ")");
                if (retried++ >= 5) {
                    continue;
                }
                LOGGER.info("retrying");
                System.out.println(2);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException i) {
                    Thread.interrupted(); // Yes, I have read the javadoc!
                }
            }
        }
        List<MidiDevice> oldList = new ArrayList(midiDevices);

        List<MidiDevice> deltaAddList = removeByMidiDeviceInfo(oldList, newList);

        List<MidiDevice> deltaRemoveList = removeByMidiDeviceInfo(newList, oldList);

        LOGGER.debug("refreshing 'old' set <{}>", oldList);
        LOGGER.debug("... by removing <{}>", deltaRemoveList);
        LOGGER.debug("... by adding <{}>", deltaAddList);

        List<MidiDevice> buffer = new ArrayList(midiDevices);
        for (MidiDevice remove : deltaRemoveList) {
            if (remove.isOpen()) {
                remove.close();
            }
            synchronized (openMidiDevices) {
                openMidiDevices.remove(remove);
            }
            synchronized (listeners) {
                listeners.forEach(listener -> {
                    listener.midiDeviceClosed(remove);
                    listener.midiDeviceRemoved(remove);
                });
            }
            buffer.remove(remove);
        }
        //buffer.addAll(deltaAddList);
        //buffer.sort(COMPARE_BY_DISPLAY_NAME);
        //midiDevicesBacking.clear();
        //midiDevicesBacking.addAll(buffer);

        midiDevices.removeAll(deltaRemoveList);
        midiDevices.addAll(deltaAddList);
        midiDevices.sort(COMPARE_BY_DISPLAY_NAME);
        synchronized (listeners) {
            listeners.forEach(listener -> {
                for (MidiDevice removed : deltaRemoveList) {
                    listener.midiDeviceClosed(removed);
                    listener.midiDeviceRemoved(removed);
                }
                for (MidiDevice added : deltaAddList) {
                    listener.midiDeviceRemoved(added);
                }
            });
        }

        LOGGER.debug("set is now <{}>", midiDevices);

        if (!errors.isEmpty()) {
            throw new FunctionalException("one or more exceptions: " + String.join(", ", errors));
        }
    }

    // MidiDevice.equals returns false for two MidiDevice instances opened with the same MidiDevice.Info.
    // So we must do comparisons via MidiDevice.Info instead of using set.removeAll(Collection).
    private static List<MidiDevice> removeByMidiDeviceInfo(List<MidiDevice> removeThis, List<MidiDevice> fromThis) {
        Map<MidiDevice.Info, MidiDevice> fromThisMap = new HashMap<>();
        for (MidiDevice from : fromThis) {
            fromThisMap.put(from.getDeviceInfo(), from);
        }
        for (MidiDevice remove : removeThis) {
            fromThisMap.remove(remove.getDeviceInfo());
        }
        return new ArrayList<MidiDevice>(fromThisMap.values());
    }

    public static List<String> getCapabilities(MidiDevice device) {
        List<String> result = new ArrayList<>();
        if (device.getMaxReceivers() != 0) {
            result.add("receiver");
        }
        if (device.getMaxTransmitters() != 0) {
            result.add(("sender"));
        }
        return result;
    }

    public static String getCapabilitiesDecription(MidiDevice device) {
        List<String> caps = getCapabilities(device);
        if (caps.isEmpty()) {
            return "";
        } else {
            return "(" + String.join(", ", caps) + ")";
        }
    }

    public static String getDisplayName(MidiDevice device) {
        if (device == null) {
            return "<empty>";
        } else {
            return getDisplayName(device.getDeviceInfo()) + getCapabilitiesDecription(device);
        }
    }

    public static String getPrefsName(MidiDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("device cannot be null");
        } else {
            String result = getDisplayName(device.getDeviceInfo()) + getCapabilitiesDecription(device);
            if (result.length() > MAX_PREFS_KEY_AND_NAME_LENGTH) {
                return result.substring(0, MAX_PREFS_KEY_AND_NAME_LENGTH);
            } else {
                return result;
            }
        }
    }

    public static String getDisplayName(MidiDevice.Info info) {
        StringBuilder result = new StringBuilder(info.getName());
        if (info.getDescription() != null) {
            result.append(", ").append(info.getDescription());
        }
        if (info.getVendor() != null) {
            result.append(" - ").append(info.getVendor());
        }
        // We skip the version, because this display name is used to identify devices over different
        // executions of the program. So a newer version does probably mean that the same settings should
        // be used.
        return result.toString();
    }

    public List<MidiDevice> getMidiDevices() {
        return new ArrayList(midiDevices);
    }

    public List<MidiDevice> getOpenMidiDevices() {
        return new ArrayList(openMidiDevices);
    }

    public void close() {
        // We explicitly close ONLY the java midi devices. We want our devices to be reopened automatically
        // the next time the application starts.
        for (MidiDevice midiDevice : midiDevices) {
            if (midiDevice.isOpen()) {
                LOGGER.debug("closing <{}>", midiDevice);
                midiDevice.close();
            }
        }
        ;
    }

    public void open(MidiDevice device) {
        try {
            if (device.isOpen()) {
                throw new IllegalArgumentException("already open: <" + device + ">");
            } else {
                LOGGER.debug("opening <{}>", device);
                device.open();
                synchronized (openMidiDevices) {
                    openMidiDevices.add(device);
                    LOGGER.debug("opened <{}>", openMidiDevices);
                    Collections.sort(openMidiDevices, COMPARE_BY_DISPLAY_NAME);
                }
                synchronized (listeners) {
                    listeners.forEach(listener -> listener.midiDeviceOpened(device));
                }
            }
        } catch (MidiUnavailableException e) {
            throw new IllegalStateException(e);
        }
    }

    public void close(MidiDevice device) {
        if (device.isOpen()) {
            LOGGER.debug("closing <{}>", device);
            device.close();
            synchronized (openMidiDevices) {
                openMidiDevices.remove(device);
            }
            synchronized (listeners) {
                listeners.forEach(listener -> listener.midiDeviceClosed(device));
            }
        } else {
            throw new IllegalArgumentException("already closed: <" + device + ">");
        }
    }

}
