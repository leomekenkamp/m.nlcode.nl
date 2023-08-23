package nl.nlcode.m.ui;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import nl.nlcode.m.engine.MidiInOut;

/**
 *
 * @author leo
 */
public class ChannelSpinner extends Spinner<Integer> {
    
    public ChannelSpinner() {
        setEditable(true);
        setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MidiInOut.CHANNEL_MIN, MidiInOut.CHANNEL_MAX));
    }
    
    public void initialize(MidiInOutUi ui) {
        getValueFactory().setConverter(ui.getMidiChannelStringConverter());
    }
}
