package nl.nlcode.m.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isOneOf;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiException;

/**
 *
 * @author jq59bu
 */
public class MidiDeviceMgrTest {

    static class Ref<T> {

        private T t;

        public Ref() {
        }

        public Ref(T t) {
            this();
            set(t);
        }

        public T get() {
            return t;
        }

        public void set(T t) {
            this.t = t;
        }
    }

    static class Listener implements MidiDeviceMgr.Listener {

        private Set<MidiDevice> _added = new HashSet<>();
        private Set<MidiDevice> _removed = new HashSet<>();
        private Set<MidiDevice> _opened = new HashSet<>();
        private Set<MidiDevice> _closed = new HashSet<>();

        public Set<MidiDevice> added() {
            Set<MidiDevice> result = _added;
            _added = new HashSet<>();
            return result;
        }

        public Set<MidiDevice> removed() {
            Set<MidiDevice> result = _removed;
            _removed = new HashSet<>();
            return result;
        }

        public Set<MidiDevice> opened() {
            Set<MidiDevice> result = _opened;
            _opened = new HashSet<>();
            return result;
        }

        public Set<MidiDevice> closed() {
            Set<MidiDevice> result = _closed;
            _closed = new HashSet<>();
            return result;
        }

        @Override
        public void midiDeviceAdded(MidiDevice dev) {
            _added.add(dev);
        }

        @Override
        public void midiDeviceRemoved(MidiDevice dev) {
            _removed.add(dev);
        }

        @Override
        public void midiDeviceOpened(MidiDevice dev) {
            _opened.add(dev);
        }

        @Override
        public void midiDeviceClosed(MidiDevice dev) {
            _closed.add(dev);
        }

    }
    Listener listener = new Listener();

    MidiDeviceMgr instance;

    MidiDevice oneDev = new MidiDeviceMgr.NoneMidiDevice("one");
    MidiDevice twoDev = new MidiDeviceMgr.NoneMidiDevice("two");
    MidiDevice threeDev = new MidiDeviceMgr.NoneMidiDevice("three");
    MidiDevice fourDev = new MidiDeviceMgr.NoneMidiDevice("four");
    MidiDevice fiveDev = new MidiDeviceMgr.NoneMidiDevice("five");

    Map<MidiDevice.Info, MidiDevice> infoToDev = new HashMap<>();

    final Ref<MidiDevice.Info[]> givenDeviceInfo;

    public MidiDeviceMgrTest() {
        infoToDev.put(oneDev.getDeviceInfo(), oneDev);
        infoToDev.put(twoDev.getDeviceInfo(), twoDev);
        infoToDev.put(threeDev.getDeviceInfo(), threeDev);
        infoToDev.put(fourDev.getDeviceInfo(), fourDev);
        infoToDev.put(fiveDev.getDeviceInfo(), fiveDev);
        givenDeviceInfo = new Ref<>(new MidiDevice.Info[]{});

        instance = new MidiDeviceMgr() {
            @Override
            MidiDevice.Info[] getDeviceInfo() {
                return givenDeviceInfo.get();
            }

            @Override
            MidiDevice getMidiDevice(MidiDevice.Info info) throws MidiUnavailableException {
                return infoToDev.get(info);
            }
        };

        instance.addListener(listener);

    }

    private void givenDeviceInfo(MidiDevice.Info... info) {
        givenDeviceInfo.set(info);
    }

    private void givenDeviceInfo(MidiDevice ... devices) {
        
        MidiDevice.Info[] infos = new MidiDevice.Info[devices.length];
        for (int i = 0; i < devices.length; i++) {
            infos[i] = devices[i].getDeviceInfo();
        }
        givenDeviceInfo(infos);
    }
    
    private static final MidiDevice.Info[] NONE = new MidiDevice.Info[] {};
    
    @Test
    void system_info_should_match() throws CoreMidiException {
        instance = MidiDeviceMgr.getInstance();
        assertThat(instance.getSystemInfo(), isOneOf(
                "uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider, libraryVersion: 1.6, libraryLoaded: true, scanInterval: 500",
                "javax.sound.midi.MidiSystem"
        ));
    }

    @Nested
    class refresh {

        @Test
        public void should_do_nothing_when_there_are_no_devices() {
            assertThat(instance.getMidiDevices(), is(empty()));
            assertThat(listener.added(), is(empty()));
            assertThat(listener.removed(), is(empty()));

            instance.refreshMidiDevices();

            assertThat(listener.added(), is(empty()));
            assertThat(listener.removed(), is(empty()));
            assertThat(instance.getMidiDevices(), is(empty()));
        }

        @Test
        public void should_add_a_new_device() {
            givenDeviceInfo(oneDev.getDeviceInfo());

            instance.refreshMidiDevices();

            assertThat(listener.added(), containsInAnyOrder(oneDev));
            assertThat(listener.removed(), is(empty()));
            assertThat(instance.getMidiDevices(), containsInAnyOrder(oneDev));
        }

        @Test
        public void should_remove_a_device() {
            givenDeviceInfo(oneDev.getDeviceInfo());
            instance.refreshMidiDevices();
            assertThat(listener.added(), containsInAnyOrder(oneDev));
            assertThat(listener.removed(), is(empty()));

            givenDeviceInfo(NONE);
            instance.refreshMidiDevices();

            assertThat(listener.added(), is(empty()));
            assertThat(listener.removed(), containsInAnyOrder(oneDev));
            assertThat(instance.getMidiDevices(), is(empty()));
        }

        @Test
        public void should_add_two_devices() {
            givenDeviceInfo(oneDev.getDeviceInfo());
            instance.refreshMidiDevices();
            assertThat(listener.added(), containsInAnyOrder(oneDev));
            assertThat(listener.removed(), is(empty()));

            givenDeviceInfo(oneDev, twoDev);
            instance.refreshMidiDevices();

            assertThat(listener.added(), containsInAnyOrder(twoDev));
            assertThat(listener.removed(), is(empty()));
            assertThat(instance.getMidiDevices(), containsInAnyOrder(oneDev, twoDev));
        }

        @Test
        public void should_add_five_devices_and_remove_first_and_last() {
            givenDeviceInfo(oneDev.getDeviceInfo(), twoDev.getDeviceInfo(), threeDev.getDeviceInfo(), fourDev.getDeviceInfo(), fiveDev.getDeviceInfo());
            instance.refreshMidiDevices();
            assertThat(listener.added(), containsInAnyOrder(oneDev, twoDev, threeDev, fourDev, fiveDev));
            assertThat(listener.removed(), is(empty()));

            givenDeviceInfo(twoDev, threeDev, fourDev);
            instance.refreshMidiDevices();

            assertThat(listener.added(), is(empty()));
            assertThat(listener.removed(), containsInAnyOrder(oneDev, fiveDev));
            assertThat(instance.getMidiDevices(), containsInAnyOrder(twoDev, threeDev, fourDev));
        }

        @Test
        public void should_add_five_devices_and_remove_middle_one() {
            givenDeviceInfo(oneDev.getDeviceInfo(), twoDev.getDeviceInfo(), threeDev.getDeviceInfo(), fourDev.getDeviceInfo(), fiveDev.getDeviceInfo());
            instance.refreshMidiDevices();
            assertThat(listener.added(), containsInAnyOrder(oneDev, twoDev, threeDev, fourDev, fiveDev));
            assertThat(listener.removed(), is(empty()));

            givenDeviceInfo(oneDev, twoDev, fourDev, fiveDev);
            instance.refreshMidiDevices();

            assertThat(listener.added(), is(empty()));
            assertThat(listener.removed(), containsInAnyOrder(threeDev));
            assertThat(instance.getMidiDevices(), containsInAnyOrder(oneDev, twoDev, fourDev, fiveDev));
        }

        @Test
        public void should_add_and_remove_multiple_times() {
            givenDeviceInfo(oneDev.getDeviceInfo(), twoDev.getDeviceInfo(), threeDev.getDeviceInfo(), fourDev.getDeviceInfo(), fiveDev.getDeviceInfo());
            instance.refreshMidiDevices();
            assertThat(listener.added(), containsInAnyOrder(oneDev, twoDev, threeDev, fourDev, fiveDev));
            assertThat(listener.removed(), is(empty()));

            givenDeviceInfo(oneDev, twoDev);
            instance.refreshMidiDevices();

            assertThat(listener.added(), is(empty()));
            assertThat(listener.removed(), containsInAnyOrder(threeDev, fourDev, fiveDev));
            assertThat(instance.getMidiDevices(), containsInAnyOrder(oneDev, twoDev));

            givenDeviceInfo(oneDev, threeDev, fourDev, fiveDev);
            instance.refreshMidiDevices();

            assertThat(listener.added(), containsInAnyOrder(threeDev, fourDev, fiveDev));
            assertThat(listener.removed(), containsInAnyOrder(twoDev));
            assertThat(instance.getMidiDevices(), containsInAnyOrder(oneDev, threeDev, fourDev, fiveDev));

            givenDeviceInfo(twoDev, threeDev, fourDev);
            instance.refreshMidiDevices();

            assertThat(listener.added(), containsInAnyOrder(twoDev));
            assertThat(listener.removed(), containsInAnyOrder(oneDev, fiveDev));
            assertThat(instance.getMidiDevices(), containsInAnyOrder(twoDev, threeDev, fourDev));

            givenDeviceInfo(twoDev, threeDev, fourDev, fiveDev);
            instance.refreshMidiDevices();

            assertThat(listener.added(), containsInAnyOrder(fiveDev));
            assertThat(listener.removed(), is(empty()));
            assertThat(instance.getMidiDevices(), containsInAnyOrder(twoDev, threeDev, fourDev, fiveDev));
        }

        @Test
        public void does_not_close_removed_devices_that_are_not_opened() {
            givenDeviceInfo(oneDev.getDeviceInfo());
            instance.refreshMidiDevices();

            assertThat(listener.opened(), is(empty()));
            assertThat(listener.closed(), is(empty()));

            givenDeviceInfo(NONE);
            instance.refreshMidiDevices();

            assertThat(listener.opened(), is(empty()));
            assertThat(listener.closed(), is(empty()));
        }

        @Test
        public void closes_open_removed_devices_automatically() throws MidiUnavailableException {
            givenDeviceInfo(oneDev.getDeviceInfo());
            instance.refreshMidiDevices();

            instance.open(oneDev);
            assertThat(listener.opened(), containsInAnyOrder(oneDev));
            givenDeviceInfo(NONE);
            instance.refreshMidiDevices();

            assertThat(listener.opened(), is(empty()));
            assertThat(listener.closed(), containsInAnyOrder(oneDev));
        }
    }
}
