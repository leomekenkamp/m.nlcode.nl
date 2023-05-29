package nl.nlcode.m.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 *
 * @author leo
 */
public class MidiMessageDump extends MidiInOut {

    private static final long serialVersionUID = 0L;

    private ShowTicks showTicks = ShowTicks.ALL;

    private transient int tickCount = 0;

    public static class MessageAndTime {

        public MidiMessage message;
        public LocalTime time;

        public MessageAndTime(MidiMessage message) {
            this(message, LocalTime.now());
        }

        public MessageAndTime(MidiMessage message, LocalTime time) {
            this.message = message;
            this.time = time;
        }
    }

    private transient ObservableList<MessageAndTime> midiMessageList;

    public MidiMessageDump() {
        midiMessageList = FXCollections.observableArrayList();
        if (showTicks == null) {
            showTicks = ShowTicks.ALL;
        }
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        boolean addToList = false;
        if (message.getStatus() == ShortMessage.TIMING_CLOCK) {
            switch (getShowTicks()) {
                case NONE:
                    break;
                case FIRST:
                    if (tickCount == 0) {
                        tickCount++;
                        addToList = true;
                    }
                    break;
                case EVERY24:
                    if (++tickCount >= 24) {
                        tickCount = 0;
                        addToList = true;
                    }
                    break;
                case EVERY384:
                    if (++tickCount >= 384) {
                        tickCount = 0;
                        addToList = true;
                    }
                    break;
                case ALL:
                    addToList = true;
            }
        } else {
            addToList = true;
        }
        if (addToList) {
            addToList(message, timeStamp);
        }
    }

    private void addToList(MidiMessage message, long timeStamp) {
        synchronized (midiMessageList) { // ugly as hell
            midiMessageList.add(0, new MessageAndTime(message));
            final int MAX_SIZE = 100;
            while (midiMessageList.size() > MAX_SIZE) {
                midiMessageList.remove(100);
            }
        }
    }

    public ObservableList<MessageAndTime> getMidiMessageList() {
        return midiMessageList;
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

    public ShowTicks getShowTicks() {
        return showTicks;
    }

    public void setShowTicks(ShowTicks showTicks) {
        tickCount = 0;
        this.showTicks = showTicks;
    }

}
