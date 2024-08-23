package nl.nlcode.m.ui;

import java.util.List;

/**
 *
 * @author leo
 */
public class MidiInOutUiStringConverter extends ListItemStringConverter<MidiInOutUi<?>> {

    public MidiInOutUiStringConverter(List<MidiInOutUi<?>> midiInOutUiList) {
        super(midiInOutUiList);
    }

    @Override
    public String toString(MidiInOutUi midiInOutUi) {
        return midiInOutUi == null ? "" : midiInOutUi.getName();
    }

}
