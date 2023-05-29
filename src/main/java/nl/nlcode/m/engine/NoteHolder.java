package nl.nlcode.m.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class NoteHolder extends MidiInOut<NoteHolder.Ui> {

    public static interface Ui extends MidiInOut.Ui {

        default void notesHeldCount(int channel, int nrOfNotes) {
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NoteHolder.class);

    private static final long serialVersionUID = 0L;

    private transient Map<Integer, ShortMessage>[] bufferedNoteOffMessages;

    private transient Set<Integer>[] receivedNotesOn;

    public NoteHolder() {
        bufferedNoteOffMessages = new LinkedHashMap[CHANNEL_COUNT];
        receivedNotesOn = new Set[CHANNEL_COUNT];
        for (int channel = CHANNEL_MIN_ZERO_BASED; channel <= CHANNEL_MAX_ZERO_BASED; channel++) {
            bufferedNoteOffMessages[channel] = new LinkedHashMap<>();
            receivedNotesOn[channel] = new LinkedHashSet<>();
        }
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

    @Override
    public boolean isActiveSender() {
        return true;
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage incoming) {
            int channel = incoming.getChannel();
            Map<Integer, ShortMessage> noteOffBuffer = bufferedNoteOffMessages[channel];
            int note = incoming.getData1();
            LOGGER.debug("received <{}>", new MidiMessageFormat().format(incoming));
            switch (incoming.getCommand()) {
                case ShortMessage.NOTE_ON -> {
                    receivedNotesOn[channel].add(note);
                    if (!noteOffBuffer.containsKey(note)) {
                        LOGGER.debug("note <{}> is not on, turning on", note);
                        clearNotCurrentlyDownNotes(channel);
                        send(incoming);
                        noteOffBuffer.put(note, null);
                        uiUpdate(ui -> ui.notesHeldCount(channel, noteOffBuffer.size()));
                    }
                }
                case ShortMessage.NOTE_OFF -> {
                    receivedNotesOn[channel].remove(note);
                    ShortMessage previousOff = noteOffBuffer.get(note);
                    if (previousOff == null && noteOffBuffer.containsKey(note)) {
                        LOGGER.debug("adding note <{}> to noteOffBuffer", note);
                        noteOffBuffer.put(note, incoming);
                        uiUpdate(ui -> ui.notesHeldCount(channel, noteOffBuffer.size()));

                    } else {
                        LOGGER.debug("sending note off for <{}>", note);
                        send(incoming);
                        noteOffBuffer.remove(note);
                        uiUpdate(ui -> ui.notesHeldCount(channel, noteOffBuffer.size()));
                        if (receivedNotesOn[channel].isEmpty()) {
                            clearNotCurrentlyDownNotes(channel);
                        }
                    }
                }
                default -> {
                    super.processReceive(message, timeStamp);
                }
            }
        } else {
            super.processReceive(message, timeStamp);
        }
    }

    public void clearNotCurrentlyDownNotes(int channel) {
        verifyChannelZeroBased(channel);
        Map<Integer, ShortMessage> noteOffBuffer = bufferedNoteOffMessages[channel];
        for (Iterator<Map.Entry<Integer, ShortMessage>> noteOffIterator = noteOffBuffer.entrySet().iterator(); noteOffIterator.hasNext();) {
            Map.Entry<Integer, ShortMessage> entry = noteOffIterator.next();
            int note = entry.getKey();
            if (receivedNotesOn[channel].contains(note)) {
                LOGGER.debug("not clearing <{}>", note);
                noteOffBuffer.put(note, null);
                continue;
            }
            ShortMessage noteOff = entry.getValue();
            LOGGER.debug("clearing <{}>", note);
            send(noteOff);
            noteOffIterator.remove();
        }
        uiUpdate(ui -> ui.notesHeldCount(channel, noteOffBuffer.size()));
    }

    public void clearNotCurrentlyDownNotes() {
        for (int channel = CHANNEL_MIN_ZERO_BASED; channel <= CHANNEL_MAX_ZERO_BASED; channel++) {
            NoteHolder.this.clearNotCurrentlyDownNotes(channel);
        }
    }

    public int countCurrentlyDownNotes(int channel) {
        verifyChannelZeroBased(channel);
        return bufferedNoteOffMessages[channel].size();
    }

}
