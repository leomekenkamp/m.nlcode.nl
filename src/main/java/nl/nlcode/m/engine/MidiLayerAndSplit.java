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
            if (fromNote < 0 || fromNote > 127) {
                throw new IllegalArgumentException();
            }
            this.fromNote = fromNote;
        }

        public int getToNote() {
            return toNote;
        }

        public void setToNote(int toNote) {
            if (toNote < 0 || toNote > 127) {
                throw new IllegalArgumentException();
            }
            this.toNote = toNote;
        }

        public int getInputChannel() {
            return inputChannel;
        }

        public void setInputChannel(int inputChannel) {
            if (inputChannel < 0 || inputChannel > 15) {
                throw new IllegalArgumentException();
            }
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
            if (outputChannel < 0 || outputChannel > 15) {
                throw new IllegalArgumentException();
            }
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
        void maybeSend(ShortMessage message) {
            if (noteRelated(message)) {
                LOGGER.debug("note message <{}>", MIDI_FORMAT.format(message));
                if (noteInRange(message.getData1()) && inputChannel == message.getChannel()) {
                    LOGGER.debug("message in layer");
                    int data1 = message.getData1();
                    data1 += transpose;
                    if (validNote(data1)) {
                        LOGGER.debug("note valid <{}>", data1);
                        try {
                            ShortMessage copy = (ShortMessage) message.clone();
                            copy.setMessage(message.getCommand(), outputChannel, data1, message.getData2());
                            send(copy);
                        } catch (InvalidMidiDataException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                }
            } else {
                send(message);
            }
        }

        public void remove() {
            getLayers().remove(this);
        }
    }

    private List<Layer> layers;

    public MidiLayerAndSplit() {
        layers = Collections.synchronizedList(new ArrayList());
    }

    public void detaultLayers() {
        for (int i = 0; i < 16; i++) {
            layers.add(new Layer(i));
        }
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public Layer createLayer() {
        Layer result = new Layer(0);
        getLayers().add(result);
        return result;
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage shortMessage) {
            if (noteRelated(shortMessage)) {
                boolean[] channelsSent = new boolean[16];
                LOGGER.debug("through layers <{}>", MIDI_FORMAT.format(message));
                for (Layer layer : layers) {
                    layer.maybeSend(shortMessage);
                }
            } else {
                LOGGER.debug("non-note message <{}>, relaying to super", MIDI_FORMAT.format(message));
                super.processReceive(message, timeStamp);
            }
        } else {
            LOGGER.debug("non-note message <{}>, relaying to super", MIDI_FORMAT.format(message));
            super.processReceive(message, timeStamp);
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
