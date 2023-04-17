package nl.nlcode.m.engine;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import java.util.*;

/**
 * @author leo
 */
public final class MidiDeviceMgr {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiDeviceMgr.class);

    public static final Comparator<? super MidiDevice> COMPARE_BY_DISPLAY_NAME = new Comparator<>() {
        @Override
        public int compare(MidiDevice o1, MidiDevice o2) {
            return getDisplayName(o1).compareTo(getDisplayName(o2));
        }
    };

    private final boolean USE_COREMIDI4j = true;

    private final ObservableList<MidiDevice> midiDevicesBacking;

    private final ObservableList<MidiDevice> midiDevices;

    private final ObservableList<MidiDevice> openMidiDevicesBacking;

    private final ObservableList<MidiDevice> openMidiDevices;

    private static final MidiDeviceMgr instance = new MidiDeviceMgr();

    private MidiDeviceMgr() {
        midiDevicesBacking = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        midiDevices = FXCollections.unmodifiableObservableList(midiDevicesBacking);
        openMidiDevicesBacking = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        openMidiDevices = FXCollections.unmodifiableObservableList(openMidiDevicesBacking);
        refreshMidiDevices();
        if (USE_COREMIDI4j) {
            try {
                CoreMidiDeviceProvider.addNotificationListener(() -> refreshMidiDevices());
            } catch (CoreMidiException e) {
                LOGGER.error("Could not register listener. No auto refresh of MIDI (usb) devices.");
            }
        }
    }

    public static MidiDeviceMgr getInstance() {
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
        List<MidiDevice> newList = new ArrayList();
        int retried = 0;
        for (MidiDevice.Info info : midiDeviceInfos) {
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
                try {
                    Thread.sleep(50);
                } catch (InterruptedException i) {
                    Thread.interrupted(); // Yes, I have read the javadoc!
                }
            }
        }
        List<MidiDevice> oldList = new ArrayList(midiDevicesBacking);

        List<MidiDevice> deltaAddList = removeByMidiDeviceInfo(oldList, newList);

        List<MidiDevice> deltaRemoveList = removeByMidiDeviceInfo(newList, oldList);

        LOGGER.debug("refreshing 'old' set <{}>", oldList);
        LOGGER.debug("... by removing <{}>", deltaRemoveList);
        LOGGER.debug("... by adding <{}>", deltaAddList);

        List<MidiDevice> buffer = new ArrayList(midiDevicesBacking);
        for (MidiDevice remove : deltaRemoveList) {
            if (remove.isOpen()) {
                remove.close();
            }
            synchronized (openMidiDevicesBacking) {
                openMidiDevicesBacking.remove(remove);
            }
            buffer.remove(remove);
        }
        //buffer.addAll(deltaAddList);
        //buffer.sort(COMPARE_BY_DISPLAY_NAME);
        //midiDevicesBacking.clear();
        //midiDevicesBacking.addAll(buffer);

        midiDevicesBacking.removeAll(deltaRemoveList);
        midiDevicesBacking.addAll(deltaAddList);
        midiDevicesBacking.sort(COMPARE_BY_DISPLAY_NAME);

        LOGGER.debug("set is now <{}>", midiDevicesBacking);

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

    public ObservableList<MidiDevice> getMidiDevices() {
        return midiDevices;
    }

    public ObservableList<MidiDevice> getOpenMidiDevices() {
        return openMidiDevices;
    }

    public void close() {
        // We explicitly close ONLY the java midi devices. We want our devices to be reopened automatically
        // the next time the application starts.
        for (MidiDevice midiDevice : midiDevicesBacking) {
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
                synchronized (openMidiDevicesBacking) {
                    openMidiDevicesBacking.add(device);
                    LOGGER.debug("opened <{}>", openMidiDevicesBacking);
                    Collections.sort(openMidiDevicesBacking, COMPARE_BY_DISPLAY_NAME);
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
            synchronized (openMidiDevicesBacking) {
                openMidiDevicesBacking.remove(device);
            }
        } else {
            throw new IllegalArgumentException("already closed: <" + device + ">");
        }
    }
}
