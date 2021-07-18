package nl.nlcode.m.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class MidiLayerAndSplit extends MidiInOut {

    private static final long serialVersionUID = 0L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiLayerAndSplit.class);

    private static final MidiMessageFormat MIDI_FORMAT = new MidiMessageFormat();

    public class Layer implements Serializable {

        private static final long serialVersionUID = 0L;

        private int fromNote;

        private int toNote;

        private int inputChannel;

        private int outputChannel;

        private int transpose;

        public Layer(int channel) {
            fromNote = 0;
            toNote = 127;
            inputChannel = channel;
            outputChannel = channel;
            transpose = 0;
        }

        public int getFromNote() {
            return fromNote;
        }

        public void setFromNote(int fromNote) {
            this.fromNote = fromNote;
        }

        public int getToNote() {
            return toNote;
        }

        public void setToNote(int toNote) {
            this.toNote = toNote;
        }

        public int getInputChannel() {
            return inputChannel;
        }

        public void setInputChannel(int inputChannel) {
            this.inputChannel = inputChannel;
        }

        public int getInputChannelOneBased() {
            return getInputChannel() + 1;
        }

        public void setInputChannelOneBased(int inputChannelOneBased) {
            setInputChannel(inputChannelOneBased - 1);
        }

        public int getOutputChannel() {
            return outputChannel;
        }

        public void setOutputChannel(int outputChannel) {
            this.outputChannel = outputChannel;
        }

        public int getOutputChannelOneBased() {
            return getOutputChannel() + 1;
        }

        public void setOutputChannelOneBased(int outputChannelOneBased) {
            setOutputChannel(outputChannelOneBased - 1);
        }

        public int getTranspose() {
            return transpose;
        }

        public void setTranspose(int transpose) {
            this.transpose = transpose;
        }

        private boolean noteInRange(int note) {
            if (fromNote <= toNote) {
                return fromNote <= note && note <= toNote;
            } else {
                return fromNote <= note || note <= toNote;
            }
        }

        /**
         *
         * @param message
         * @param channelsSent to keep track on which channel the message has already functionally
         * been sent
         */
        void maybeSend(ShortMessage message, boolean[] channelsSent) {
            if (noteRelated(message)) {
                LOGGER.debug("note message {}", MIDI_FORMAT.format(message));
                if (noteInRange(message.getData1()) && inputChannel == message.getChannel()) {
                    LOGGER.debug("message in layer");
                    int data1 = message.getData1();
                    data1 += transpose;
                    if (validNote(data1)) {
                        LOGGER.debug("note valid {}", data1);
                        try {
                            ShortMessage copy = (ShortMessage) message.clone();
                            copy.setMessage(message.getCommand(), outputChannel, data1, message.getData2());
                            sendOnce(copy, channelsSent);
                        } catch (InvalidMidiDataException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                }
            } else {
                sendOnce(message, channelsSent);
            }
        }

        private void sendOnce(ShortMessage message, boolean[] channelsSent) {
            if (channelsSent[message.getChannel()]) {
                LOGGER.debug("already send to channel {}", MIDI_FORMAT.format(message));
            } else {
                LOGGER.debug("send first in channel {}", MIDI_FORMAT.format(message));
                send(message);
                channelsSent[message.getChannel()] = true;
            }
        }

        public void remove() {
            getLayers().remove(this);
        }
    }

    private List<Layer> layers;

    public MidiLayerAndSplit(Project project) {
        super(project);
        layers = Collections.synchronizedList(new ArrayList());
        for (int i = 0; i < 16; i++) {
            layers.add(new Layer(i));
        }
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public Layer createLayer() {
        Layer result = new Layer(0);
        return result;
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage) {
            ShortMessage shortMessage = (ShortMessage) message;
            if (noteRelated(shortMessage)) {
                boolean[] channelsSent = new boolean[16];
                LOGGER.debug("through layers {}", MIDI_FORMAT.format(message));
                for (Layer layer : layers) {
                    layer.maybeSend(shortMessage, channelsSent);
                }
            } else {
                LOGGER.debug("non-note message {}, relaying to super", MIDI_FORMAT.format(message));
                super.send(message, timeStamp);
            }
        } else {
            LOGGER.debug("non-note message {}, relaying to super", MIDI_FORMAT.format(message));
            super.send(message, timeStamp);
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

}
