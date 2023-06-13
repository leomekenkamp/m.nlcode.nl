package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import nl.nlcode.javafxutil.FxmlController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiDeviceSelector extends Pane implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static class DefaultItem implements MidiDevice {

        private class DefaultInfo extends MidiDevice.Info {

            public DefaultInfo(String name) {
                super(name, null, null, null);
            }
        }

        private MidiDevice.Info info;

        public DefaultItem(String name) {
            info = new DefaultInfo(name);
        }

        @Override
        public Info getDeviceInfo() {
            return info;
        }

        @Override
        public void open() throws MidiUnavailableException {
            throw new UnsupportedOperationException("This is a menu item.");
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("This is a menu item.");
        }

        @Override
        public boolean isOpen() {
            throw new UnsupportedOperationException("This is a menu item.");
        }

        @Override
        public long getMicrosecondPosition() {
            throw new UnsupportedOperationException("This is a menu item.");
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
            throw new UnsupportedOperationException("This is a menu item.");
        }

        @Override
        public List<Receiver> getReceivers() {
            throw new UnsupportedOperationException("This is a menu item.");
        }

        @Override
        public Transmitter getTransmitter() throws MidiUnavailableException {
            throw new UnsupportedOperationException("This is a menu item.");
        }

        @Override
        public List<Transmitter> getTransmitters() {
            throw new UnsupportedOperationException("This is a menu item.");
        }

    }

    public static MidiDevice NONE = new MidiDeviceSelector.DefaultItem(App.MESSAGES.getString("none"));

    private static final ObservableList<MidiDevice> DEFAULT_SINGLE_ITEMS = FXCollections.observableArrayList();

    static {
        DEFAULT_SINGLE_ITEMS.add(NONE);
    }

    @FXML
    private ComboBox comboBox;

    private ObjectProperty<ObservableList<MidiDevice>> itemsProperty = new SimpleObjectProperty<>();

    public MidiDeviceSelector() {
        loadFxml(App.MESSAGES);
//        ObservableList<MidiDevice> midiDeviceList = CONTENT.getList();
//        ObservableList<MidiDevice> itemsx = FXCollections.observableArrayList();
//        ObservableUtil.merge(itemsx, DEFAULT_SINGLE_ITEMS, midiDeviceList);
//
//        comboBox.setItems(itemsx);
//        comboBox.set(NONE);
//        comboBox.setConverter(CONTENT.getConverter());

        itemsProperty.addListener((ov, oldValue, newValue) -> {
            ObservableList<MidiDevice> allItems = FXCollections.observableArrayList();
            ObservableUtil.merge(allItems, DEFAULT_SINGLE_ITEMS, newValue);
            comboBox.setItems(allItems);
            comboBox.setValue(NONE);
            comboBox.setConverter(App.createMidiDeviceConverter(allItems));
        });
    }

    public ObjectProperty<ObservableList<MidiDevice>> itemsProperty() {
        return itemsProperty;
    }

    public ObservableList<MidiDevice> getItems() {
        return itemsProperty.get();
    }

    public void setItems(ObservableList<MidiDevice> items) {
        itemsProperty.set(items);
    }

    public ComboBox<MidiDevice> getComboBox() {
        return comboBox;
    }

}
