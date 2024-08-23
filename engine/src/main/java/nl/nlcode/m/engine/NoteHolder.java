package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.m.linkui.BooleanUpdater;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class NoteHolder<U extends NoteHolder.Ui> extends MidiInOut<U> {

    public static interface Ui extends MidiInOut.Ui {

        default void notesHeldChanged(int channel, int nrOfNotes) {
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final BooleanUpdater<U, NoteHolder<U>> newNoteOnBeforeOldNoteOff;

    public static record SaveData0(
            int id,
            boolean newNoteOnBeforeOldNoteOff,
            Marshalled<MidiInOut> s) implements Marshalled<NoteHolder> {

        @Override
        public void unmarshalInto(Context context, NoteHolder target) {
            s.unmarshalInto(context, target);
            target.setNewNoteOnBeforeOldNoteOff(newNoteOnBeforeOldNoteOff());
        }

        @Override
        public NoteHolder createMarshallable() {
            return new NoteHolder();
        }

    }

    private transient Map<Integer, ShortMessage>[] bufferedNoteOffMessages;

    private transient Set<Integer>[] receivedNotesOn;

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                newNoteOnBeforeOldNoteOff.get(),
                super.marshalInternal(-1, context));
    }

    public NoteHolder() {
        newNoteOnBeforeOldNoteOff = new BooleanUpdater<>(this, false);
        bufferedNoteOffMessages = new LinkedHashMap[CHANNEL_COUNT];
        receivedNotesOn = new Set[CHANNEL_COUNT];
        for (int channel = CHANNEL_MIN; channel <= CHANNEL_MAX; channel++) {
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
                        if (newNoteOnBeforeOldNoteOff.get()) {
                            send(incoming);
                            clearNotCurrentlyDownNotes(channel);
                        } else {
                            clearNotCurrentlyDownNotes(channel);
                            send(incoming);
                        }
                        noteOffBuffer.put(note, null);
                        uiUpdate(ui -> ui.notesHeldChanged(channel, noteOffBuffer.size()));
                    }
                }
                case ShortMessage.NOTE_OFF -> {
                    receivedNotesOn[channel].remove(note);
                    ShortMessage previousOff = noteOffBuffer.get(note);
                    if (previousOff == null && noteOffBuffer.containsKey(note)) {
                        LOGGER.debug("adding note <{}> to noteOffBuffer", note);
                        noteOffBuffer.put(note, incoming);
                        uiUpdate(ui -> ui.notesHeldChanged(channel, noteOffBuffer.size()));

                    } else {
                        LOGGER.debug("sending note off for <{}>", note);
                        send(incoming);
                        noteOffBuffer.remove(note);
                        uiUpdate(ui -> ui.notesHeldChanged(channel, noteOffBuffer.size()));
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
        verifyChannel(channel);
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
        uiUpdate(ui -> ui.notesHeldChanged(channel, noteOffBuffer.size()));
    }

    public void clearNotCurrentlyDownNotes() {
        forAllChannels(channel -> clearNotCurrentlyDownNotes(channel));
    }

    public int countCurrentlyDownNotes(int channel) {
        verifyChannel(channel);
        return bufferedNoteOffMessages[channel].size();
    }

    public boolean getNewNoteOnBeforeOldNoteOff() {
        return newNoteOnBeforeOldNoteOff.get();
    }

    public void setNewNoteOnBeforeOldNoteOff(boolean newNoteOnBeforeOldNoteOff) {
        this.newNoteOnBeforeOldNoteOff.set(newNoteOnBeforeOldNoteOff);
    }

    public BooleanUpdater<U, NoteHolder<U>> newNoteOnBeforeOldNoteOffProperty() {
        return newNoteOnBeforeOldNoteOff;
    }

}
