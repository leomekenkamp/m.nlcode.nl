package nl.nlcode.m.linkui;

import static nl.nlcode.m.engine.MidiInOut.MIDI_DATA_MAX;
import static nl.nlcode.m.engine.MidiInOut.MIDI_DATA_MIN;

/**
 *
 * @author leo
 */
public class MidiDataUpdater<U, H extends Updater.Holder<U>> extends IntUpdater<U, H> {

    public MidiDataUpdater(String name, H holder) {
        this(name, holder, 0);
    }
    
    public MidiDataUpdater(String name, H holder, int value) {
        super(name, holder, value, MIDI_DATA_MIN, MIDI_DATA_MAX);
    }

}
