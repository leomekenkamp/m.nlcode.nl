package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.MidiInOut.forAllChannels;
import nl.nlcode.m.linkui.BooleanUpdater;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class ChannelMatrix<U extends ChannelMatrix.Ui> extends MidiInOut<U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static interface Ui extends MidiInOut.Ui {

        void matrixChanged(int from, int to, boolean linked);
    }

    public static record SaveData0(
            int id,
            boolean[][] fromTo,
            Marshalled<MidiInOut> s) implements Marshalled<ChannelMatrix> {

        @Override
        public void unmarshalInto(Context context, ChannelMatrix target) {
            forAllChannels(from -> forAllChannels(to -> target.fromTo[from][to].set(fromTo()[from][to])));
            s.unmarshalInto(context, target);
        }

        @Override
        public ChannelMatrix createMarshallable() {
            return new ChannelMatrix();
        }
    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        SaveData0 result = new SaveData0(
                id,
                new boolean[CHANNEL_COUNT][CHANNEL_COUNT],
                super.marshalInternal(-1, context)
        );
        forAllChannels(from -> forAllChannels(to -> result.fromTo()[from][to] = fromTo[from][to].get()));
        return result;
    }

    private final BooleanUpdater<U, ChannelMatrix<U>> fromTo[][] = new BooleanUpdater[CHANNEL_COUNT][CHANNEL_COUNT];

    public ChannelMatrix() {
        forAllChannels(from -> forAllChannels(to -> {
            fromTo[from][to] = new BooleanUpdater(this, false);
            fromTo[from][to].setAfterChange(this, ui -> ui.matrixChanged(from, to, fromTo[from][to].get()));
        }));
        defaultFromTo();
    }

    public void defaultFromTo() {
        clearFromTo();
        forAllChannels(channel -> setFromTo(channel, channel, true));
    }

    public void clearFromTo() {
        forAllChannels(from -> forAllChannels(to -> setFromTo(from, to, false)));
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

    @Override
    public boolean isActiveSender() {
        return true;
    }

    public boolean getFromTo(int from, int to) {
        return fromTo[from][to].get();
    }

    public void setFromTo(int from, int to, boolean connected) {
        fromTo[from][to].set(connected);
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage original) {
            forAllChannels(to -> {
                if (ChannelMatrix.this.getFromTo(original.getChannel(), to)) {
                    super.send(forChannel(to, original), timeStamp);
                }
            });
        } else {
            LOGGER.debug("non-channel message, relaying to super");
            super.processReceive(message, timeStamp);
        }
    }

    private ShortMessage forChannel(int channel, ShortMessage original) {
        ShortMessage result;
        if (channel == original.getChannel()) {
            LOGGER.debug("using original for channel <{}>", channel);
            result = original;
        } else {
            try {
                LOGGER.debug("making copy for channel <{}>", channel);
                result = new ShortMessage(original.getCommand(), channel, original.getData1(), original.getData2());
            } catch (InvalidMidiDataException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }
}
