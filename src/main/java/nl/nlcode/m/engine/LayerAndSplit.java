package nl.nlcode.m.engine;

import java.beans.PropertyChangeSupport;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.m.linkui.IntUpdateProperty;
import nl.nlcode.m.linkui.UpdateProperty;
import nl.nlcode.marshalling.MarshalHelper;
import nl.nlcode.marshalling.Marshallable;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static nl.nlcode.m.engine.MidiInOut.NOTE_MAX;
import static nl.nlcode.m.engine.MidiInOut.NOTE_MIN;
import static nl.nlcode.m.engine.MidiInOut.CHANNEL_MIN;
import static nl.nlcode.m.engine.MidiInOut.CHANNEL_MAX;

/**
 *
 * @author leo
 */
public class LayerAndSplit<U extends LayerAndSplit.Ui> extends MidiInOut<U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public interface Ui extends MidiInOut.Ui {

        void layerAdded(Layer layer);

        void layerRemoved(Layer layer);

    }

    private static final MidiMessageFormat MIDI_FORMAT = new MidiMessageFormat();

    public static class Layer implements Marshallable, UpdateProperty.Holder<Layer.Ui> {

        @Override
        public void uiUpdate(Consumer<Ui> action) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void register(UpdateProperty<?, Ui, ? extends UpdateProperty.Holder<Ui>> updateProperty) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void unregister(UpdateProperty<?, Ui, ? extends UpdateProperty.Holder<Ui>> updateProperty) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDirty() {
            if (layerAndSplit != null) {
                layerAndSplit.setDirty();
            }
        }

        @Override
        public PropertyChangeSupport getPropertyChangeSupport() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        public static interface Ui {

        }

        private transient LayerAndSplit layerAndSplit;

        private IntUpdateProperty<Layer.Ui, Layer> fromNote;
        private IntUpdateProperty<Layer.Ui, Layer> toNote;
        private IntUpdateProperty<Layer.Ui, Layer> inputChannel;
        private IntUpdateProperty<Layer.Ui, Layer> outputChannel;
        private IntUpdateProperty<Layer.Ui, Layer> transpose;

        public static final record SaveData0(
                int id,
                int fromNote,
                int toNote,
                int inputChannel,
                int outputChannel,
                int transpose) implements Marshalled<Layer> {

            @Override
            public void unmarshalInto(Marshalled.Context context, Layer target) {
                target.fromNote.set(fromNote());
                target.toNote.set(toNote());
                target.inputChannel.set(inputChannel());
                target.outputChannel.set(outputChannel());
                target.transpose.set(transpose());
            }

            @Override
            public Layer createMarshallable() {
                return new Layer();
            }
        }

        @Override
        public Marshalled marshalInternal(int id, Context context) {
            return new SaveData0(
                    id,
                    fromNote.get(),
                    toNote.get(),
                    inputChannel.get(),
                    outputChannel.get(),
                    transpose.get()
            );
        }

        public Layer() {
            this(CHANNEL_MIN);
        }

        public Layer(int channel) {
            fromNote = new IntUpdateProperty<>(NOTE_MIN, NOTE_MIN, NOTE_MAX);
            toNote = new IntUpdateProperty<>(NOTE_MAX, NOTE_MIN, NOTE_MAX);
            inputChannel = new IntUpdateProperty<>(0, CHANNEL_MIN, CHANNEL_MAX);
            outputChannel = new IntUpdateProperty<>(0, CHANNEL_MIN, CHANNEL_MAX);
            transpose = new IntUpdateProperty<>(0);
        }

        public int getFromNote() {
            return fromNote.get();
        }

        public void setFromNote(int fromNote) {
            this.fromNote.set(fromNote);
        }

        public IntUpdateProperty<Layer.Ui, Layer> fromNote() {
            return fromNote;
        }

        public int getToNote() {
            return toNote.get();
        }

        public void setToNote(int toNote) {
            this.toNote.set(toNote);
        }

        public IntUpdateProperty<Layer.Ui, Layer> toNote() {
            return toNote;
        }

        public int getInputChannel() {
            return inputChannel.get();
        }

        public void setInputChannel(int inputChannel) {
            this.inputChannel.set(inputChannel);
        }

        public IntUpdateProperty<Layer.Ui, Layer> inputChannel() {
            return inputChannel;
        }

        public int getOutputChannel() {
            return outputChannel.get();
        }

        public void setOutputChannel(int outputChannel) {
            this.outputChannel.set(outputChannel);
        }

        public IntUpdateProperty<Layer.Ui, Layer> outputChannel() {
            return outputChannel;
        }

        public int getTranspose() {
            return transpose.get();
        }

        public void setTranspose(int transpose) {
            this.transpose.set(transpose);
        }

        public IntUpdateProperty<Layer.Ui, Layer> transpose() {
            return transpose;
        }

        private boolean noteInRange(int note) {
            if (fromNote.get() <= toNote.get()) {
                return fromNote.get() <= note && note <= toNote.get();
            } else {
                return fromNote.get() <= note || note <= toNote.get();
            }
        }

        /**
         *
         * @param message
         * @param channelsSent to keep track on which channel the message has already functionally
         * been sent
         */
        ShortMessage alter(ShortMessage message) {
            ShortMessage result = null;
            if (noteRelated(message)) {
                LOGGER.debug("note message <{}>", MIDI_FORMAT.format(message));
                if (noteInRange(message.getData1()) && inputChannel.get() == message.getChannel()) {
                    LOGGER.debug("message in layer");
                    int data1 = message.getData1();
                    data1 += transpose.get();
                    if (validNote(data1)) {
                        LOGGER.debug("note valid <{}>", data1);
                        try {
                            result = (ShortMessage) message.clone();
                            result.setMessage(message.getCommand(), outputChannel.get(), data1, message.getData2());
                        } catch (InvalidMidiDataException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                }
            } else {
                result = message;
            }
            return result;
        }
    }

    protected List<Layer> layers;

    public static record SaveData0(
            int id,
            Marshalled<Layer>[] layers,
            Marshalled<MidiInOut> s) implements Marshalled<LayerAndSplit> {

        @Override
        public void unmarshalInto(Marshalled.Context context, LayerAndSplit target) {
            MarshalHelper.unmarshalAddAll(context, layers(), target.layers);
            target.layers.forEach(layer -> {
                ((Layer) layer).layerAndSplit = target; // TODO: why tf is a cast needed?
            });
            s.unmarshalInto(context, target);
        }

        @Override
        public LayerAndSplit createMarshallable() {
            return new LayerAndSplit();
        }
    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new LayerAndSplit.SaveData0(
                id,
                MarshalHelper.marshallToArray(context, layers),
                super.marshalInternal(-1, context)
        );
    }

    public LayerAndSplit() {
        layers = Collections.synchronizedList(new ArrayList());
    }

    public static LayerAndSplit createWithDefaultSettings() {
        LayerAndSplit result = new LayerAndSplit();
        result.defaultLayers();
        return result;
    }

    public void remove(Layer layer) {
        layers.remove(layer);
        uiUpdate(ui -> ui.layerRemoved(layer));
        setDirty();
    }

    public void defaultLayers() {
        while (!layers.isEmpty()) {
            remove(layers.get(0));
        }
        forAllChannels(channel -> createLayer(channel));
    }

    public List<Layer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    public Layer createLayer() {
        return createLayer(0);
    }

    public Layer createLayer(int channel) {
        Layer result = new Layer(channel);
        result.layerAndSplit = this;
        layers.add(result);
        uiUpdate(ui -> ui.layerAdded(result));
        setDirty();
        return result;
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage shortMessage) {
            if (noteRelated(shortMessage)) {
                LOGGER.debug("through layers <{}>", MIDI_FORMAT.format(message));
                for (Layer layer : layers) {
                    ShortMessage altered = layer.alter(shortMessage);
                    if (altered != null) {
                        send(altered, timeStamp);
                    }
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
