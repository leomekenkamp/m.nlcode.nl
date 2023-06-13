package nl.nlcode.m.engine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.m.linkui.ObjectUpdateProperty;
import nl.nlcode.marshalling.Marshalled;

/**
 *
 * @author leo
 */
public class MidiMessageDump<U extends MidiMessageDump.Ui> extends MidiInOut<U> {

    public static record EnhancedMessage(
            byte[] rawSource,
            long timestamp,
            LocalDateTime receivedAt,
            String description) {

        private static EnhancedMessage[] EMPTY_ARRAY = new EnhancedMessage[]{};
    }

    public static interface Ui extends MidiInOut.Ui {

        void received(EnhancedMessage message);

    }

    private transient int tickCount = 0;

    private List<EnhancedMessage> enhancedMessageList = Collections.synchronizedList(new ArrayList<>());

    private ObjectUpdateProperty<ShowTicks, U, MidiMessageDump<U>> showTicks = new ObjectUpdateProperty(ShowTicks.ALL);

    public static record SaveData0(
            int id,
            ShowTicks showTicks,
            EnhancedMessage[] enhancedMessageList,
            Marshalled<MidiInOut> s) implements Marshalled<MidiMessageDump> {

        @Override
        public void unmarshalInto(Marshalled.Context context, MidiMessageDump target) {
            target.setShowTicks(showTicks());
            target.enhancedMessageList.addAll(Arrays.asList(enhancedMessageList()));
            s.unmarshalInto(context, target);
        }

        @Override
        public MidiMessageDump createMarshallable() {
            return new MidiMessageDump();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new MidiMessageDump.SaveData0(
                id,
                showTicks.get(),
                enhancedMessageList.toArray(EnhancedMessage.EMPTY_ARRAY),
                super.marshalInternal(-1, context)
        );
    }

    public MidiMessageDump() {
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

    private void addToList(MidiMessage source, long timeStamp) {
        EnhancedMessage message = new EnhancedMessage(source.getMessage(), timeStamp, LocalDateTime.now(), MIDI_FORMAT.format(source));
        enhancedMessageList.add(0, message);
        uiUpdate(ui -> ui.received(message));
        final int MAX_SIZE = 100;
        while (enhancedMessageList.size() > MAX_SIZE) {
            enhancedMessageList.remove(enhancedMessageList.size() - 1);
        }
    }

    public List<EnhancedMessage> getEnhancedMessageList() {
        return enhancedMessageList;
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

    public ShowTicks getShowTicks() {
        return showTicks.get();
    }

    public void setShowTicks(ShowTicks showTicks) {
        tickCount = 0;
        this.showTicks.set(showTicks);
    }

}
