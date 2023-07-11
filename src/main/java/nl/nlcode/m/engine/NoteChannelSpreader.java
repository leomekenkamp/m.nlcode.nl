package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.MidiInOut.forAllChannels;
import nl.nlcode.m.linkui.IntUpdateProperty;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class NoteChannelSpreader<U extends NoteChannelSpreader.Ui> extends MidiInOut<U> {

    private volatile Set<Integer>[] channelToKeyDown;

    private IntUpdateProperty<U, NoteChannelSpreader<U>> inputChannel;

    private transient int prevChannelIndex = CHANNEL_MAX;

    private transient Map<Integer, Integer> playingNoteToChannel;

    private transient Object mutex = new Object();

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static interface Ui extends MidiInOut.Ui {

    }

    public static record SaveData0(
            int id,
            int inputChannel,
            boolean[] outputChannels,
            Marshalled<MidiInOut> s) implements Marshalled<NoteChannelSpreader> {

        @Override
        public void unmarshalInto(Context context, NoteChannelSpreader target) {
            target.inputChannel.set(inputChannel);
            forAllChannels(channel -> target.setOutputChannel(channel, outputChannels[channel]));
            s.unmarshalInto(context, target);
        }

        @Override
        public NoteChannelSpreader createMarshallable() {
            return new NoteChannelSpreader();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        boolean[] outputChannels = new boolean[CHANNEL_COUNT];
        forAllChannels(channel -> outputChannels[channel] = isOutputChannel(channel));
        return new SaveData0(
                id,
                inputChannel.get(),
                outputChannels,
                super.marshalInternal(-1, context)
        );
    }

    public NoteChannelSpreader() {
        playingNoteToChannel = new HashMap<>();
        inputChannel = new IntUpdateProperty<>(0, CHANNEL_MIN, CHANNEL_MAX);
        channelToKeyDown = new Set[CHANNEL_COUNT];
        setOutputChannel(0, true);
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

    @Override
    public boolean isActiveSender() {
        return true;
    }

    protected Integer nextChannelForNoteOn() {
        int newChannelIndex = prevChannelIndex;
        do {
            newChannelIndex = newChannelIndex == CHANNEL_MAX ? CHANNEL_MIN : newChannelIndex + 1;
            if (channelToKeyDown[newChannelIndex] != null) {
                prevChannelIndex = newChannelIndex;
                return newChannelIndex;
            }
        } while (newChannelIndex != prevChannelIndex);
        return null;
    }

    public int getInputChannel() {
        return inputChannel.get();
    }

    public void setInputChannel(int inputChannel) {
        verifyChannel(inputChannel);
        this.inputChannel.set(inputChannel);
    }

    public IntUpdateProperty inputChannel() {
        return inputChannel;
    }

    public boolean isOutputChannel(int channel) {
        verifyChannel(channel);
        return channelToKeyDown[channel] != null;
    }

    public void setOutputChannel(int channel, boolean set) {
        verifyChannel(channel);
        synchronized (mutex) {
            if (channelToKeyDown[channel] == null && set) {
                channelToKeyDown[channel] = new HashSet<>();
            } else if (channelToKeyDown[channel] != null && !set) {
                channelToKeyDown[channel].forEach(key -> send(createShortMessage(ShortMessage.NOTE_OFF, channel, key, 64)));
                channelToKeyDown[channel] = null;
            }
        }
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        LOGGER.warn("HI!");
        synchronized (mutex) {
            if (message instanceof ShortMessage incoming) {
                if (incoming.getCommand() == ShortMessage.NOTE_ON) {
                    if (incoming.getChannel() == inputChannel.get()) {
                        Integer channel = nextChannelForNoteOn();
                        if (channel == null) {
                            LOGGER.warn("no channel to send to, received on ch {} but ", incoming);
                        } else {
                            if (playingNoteToChannel.put(incoming.getData1(), channel) != null) {
                                LOGGER.warn("received on note {}, but that note was already on ", toString(incoming));
                            } else {
                                ShortMessage otherChannel = createShortMessage(incoming.getCommand(), channel, incoming.getData1(), incoming.getData2());
                                LOGGER.warn("sending on {}", toString(otherChannel));
                                send(otherChannel);
                                playingNoteToChannel.put(otherChannel.getData1(), channel);
                                channelToKeyDown[channel].add(otherChannel.getData1());
                            }
                        }
                    }
                } else if (incoming.getCommand() == ShortMessage.NOTE_OFF) {
                    if (incoming.getChannel() == inputChannel.get()) {
                        Integer channel = playingNoteToChannel.remove(incoming.getData1());
                        if (channel == null) {
                            LOGGER.warn("received off note {}, but there was no on on note; perhaps there was no output channel active when the on note was processed", toString(incoming));
                        } else {
                            ShortMessage otherChannel = createShortMessage(ShortMessage.NOTE_OFF, channel, incoming.getData1(), incoming.getData2());
                            LOGGER.warn("sending off {}", toString(otherChannel));
                            send(otherChannel);
                            channelToKeyDown[channel].remove(otherChannel.getData1());
                        }
                    }
                }
            } else {
                send(message, timeStamp);
            }
        }
    }

}
