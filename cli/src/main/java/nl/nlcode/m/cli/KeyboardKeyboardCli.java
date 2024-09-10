package nl.nlcode.m.cli;

import nl.nlcode.m.engine.KeyboardKeyboard;

/**
 *
 * @author jq59bu
 */
public class KeyboardKeyboardCli<M extends KeyboardKeyboard> extends MidiInOutCli<M> implements KeyboardKeyboard.Ui {
    
    public KeyboardKeyboardCli(M midiInOut, ControlCli controlCli) {
        super(midiInOut, controlCli);
    }
    
}
