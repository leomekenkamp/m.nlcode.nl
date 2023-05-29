package nl.nlcode.m.engine;

import javax.sound.midi.MidiMessage;

/**
 *
 * @author leo
 */
public record TimestampedMessage (MidiMessage midiMessage, long timestamp) {
}