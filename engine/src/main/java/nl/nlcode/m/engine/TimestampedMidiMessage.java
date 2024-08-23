package nl.nlcode.m.engine;

import javax.sound.midi.MidiMessage;

/**
 *
 * @author leo
 */
public record TimestampedMidiMessage (MidiMessage midiMessage, long timestamp) {
}