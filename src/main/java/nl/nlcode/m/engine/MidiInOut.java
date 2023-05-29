package nl.nlcode.m.engine;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.marshalling.Marshallable;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instead of using the Java MIDI API to create software instruments, we do it the other way round.
 * We define our own: this one. The Java MIDI API device is accessed through {@link MidiDeviceLink}.
 * The reason to define an alternative way of defining instruments, is the simple reason that two
 * {@code MidiInOut} devices need to know of each other. The user needs to know which device
 * transmits to which other devices, but also which device receives from which other devices. It was
 * too cumbersome to 'enhance' the Java MIDI API to do this; bridging via {@code MidiDeviceLink}
 * gives far simpler code.
 * <p>
 * Note while there is a public {@link asyncReceive(MidiMessage, int)}, there is no public 'send'
 * method. Implementations need to decide for themselves if they keep total control over their own
 * sending of messages and thus keep their send method(s) private. A receiving instance however must
 * be visible to the world, but there is no guarantee that the data received will actually be
 * processed. Implementations may choose to ignore incoming messages, for instance if they represent
 * something like piano keys. Of course they are free to process them and -keeping with the example-
 * opt to act like a pianola.
 * <p>
 * Subclasses are expected to provide a {@code public static interface Ui} that extends the
 * {@code UI} interface in {@code MidiInOut}.
 *
 * @author leo
 */
public abstract class MidiInOut<U extends MidiInOut.Ui> implements Serializable, Lookup.Named<MidiInOut>, Marshallable {

    public static interface Event {

        public MidiInOut source();

    }

    /**
     * <p>
     * Interface to be implemented by user interface classes. This allows for decoupling of the user
     * interface: the user interface knows <code>MidiInOut</code>, but not the other way around.
     * <code>MidiInOut</code> only knows about the interface it should use to 'talk to'.
     */
    public static interface Ui {

        default void midiInOutConnected() {
        }

        default void midiInOutDisconnecting() {
        }

        default void received(MidiMessage message, long timestamp) {
        }

        default void sent(MidiMessage message, long timestamp) {
        }

        default void name(String name) {
        }
    }

    public class UpdateProperty implements Serializable {

        private static final long serialVersionUID = 0L;

        private transient Runnable afterSet;

        public final void setAfterSet(Runnable afterSet) {
            register(this);
            this.afterSet = afterSet;
        }

        public final void runAfterSet(boolean change) {
            if (change) {
                runAfterSet();
            }
        }

        public final void runAfterSet() {
            if (getUi() != null && afterSet != null) {
                afterSet.run();
            }
        }
    }

    public class IntUpdateProperty extends UpdateProperty {

        private static final long serialVersionUID = 0L;

        private AtomicInteger value = new AtomicInteger();

        public int get() {
            return value.get();
        }

        public void set(int newValue) {
            int oldValue = value.getAndSet(newValue);
            runAfterSet(oldValue != newValue);
        }
    }

    public class BooleanUpdateProperty extends UpdateProperty {

        private static final long serialVersionUID = 0L;

        private AtomicBoolean value = new AtomicBoolean();

        public BooleanUpdateProperty(boolean v) {
            value.set(v);
        }
        
        public boolean get() {
            return value.get();
        }

        public void set(boolean newValue) {
            boolean oldValue = value.getAndSet(newValue);
            runAfterSet(oldValue != newValue);
        }
    }

    public class ObjectUpdateProperty<T> extends UpdateProperty {

        private static final long serialVersionUID = 0L;

        private AtomicReference<T> value = new AtomicReference<>();

        public T get() {
            return value.get();
        }

        public void set(T newValue) {
            T oldValue = value.getAndSet(newValue);
            runAfterSet(!Objects.equals(value, newValue));
        }
    }

    private static final long serialVersionUID = 0L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected static final int NONE_FOR_7_BIT_INT = -1;

    public static final int CHANNEL_MIN_ZERO_BASED = 0;

    public static final int CHANNEL_MAX_ZERO_BASED = 15;

    public static final int CHANNEL_MIN_ONE_BASED = CHANNEL_MIN_ZERO_BASED + 1;

    public static final int CHANNEL_MAX_ONE_BASED = CHANNEL_MAX_ZERO_BASED + 1;

    public static final int CHANNEL_COUNT = CHANNEL_MAX_ZERO_BASED - CHANNEL_MIN_ZERO_BASED + 1;

    public static final int NOTE_MIN = 0;

    public static final int NOTE_MAX = 127;

    private static final MidiMessageFormat MIDI_FORMAT = new MidiMessageFormat();

    private transient Set sendingToReadonly;

    private transient Lookup<MidiInOut> lookup;

    private transient BlockingQueue<TimestampedMessage> asyncReceiveQueue;

    private transient AtomicBoolean processing;

    private transient ExecutorService executorService;

    private transient List<Consumer<U>> uiUpdates;

    private transient List<UpdateProperty> updateProperties;

    private transient U ui;

    // persisted data
    
    private String name;

    private Set<MidiInOut> sendingTo;

    private Set<MidiInOut> receivingFrom;

    private Map<Serializable, Serializable> info;


    public static record SaveData0(
            int id,
            String name,
            List<Marshalled> sendingTo,
            List<Marshalled> receivingFrom,
            Map<Serializable, Serializable> info) implements Marshalled<MidiInOut> {

        @Override
        public void unmarshalInternal(Context context, MidiInOut target) {
            target.name = name();
            for (Marshalled midiInOutOrRef : sendingTo()) {
                target.sendingTo.add((MidiInOut) midiInOutOrRef.unmarshal(context));
            }
            for (Marshalled midiInOutOrRef : receivingFrom()) {
                target.receivingFrom.add((MidiInOut) midiInOutOrRef.unmarshal(context));
            }
            target.info.putAll(info());
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                name,
                context.toSaveDataList(sendingTo),
                context.toSaveDataList(receivingFrom),
                info
        );
    }

    protected MidiInOut() {
        sendingTo = ConcurrentHashMap.newKeySet();
        receivingFrom = ConcurrentHashMap.newKeySet();
        info = new HashMap<>();
        asyncReceiveQueue = new LinkedBlockingQueue();
        processing = new AtomicBoolean(false);
        uiUpdates = new ArrayList<>();
        updateProperties = new ArrayList<>();
        sendingToReadonly = Collections.unmodifiableSet(sendingTo);
    }

    public final void activate(Project project) {
        if (this.lookup != null) {
            throw new UnsupportedOperationException("can activate only once");
        }
        if (project == null) {
            throw new IllegalArgumentException("project cannot be null");
        }
        if (project.getMidiInOutLookup() == null) {
            throw new IllegalArgumentException("lookup cannot be null");
        }
        if (project.getMidiInOutExecutorService() == null) {
            throw new IllegalArgumentException("executorService cannot be null");
        }
        this.lookup = project.getMidiInOutLookup();
        lookup.add(this);
        this.executorService = project.getMidiInOutExecutorService();
        startListening();
    }

    public U getUi() {
        return ui;
    }

    public void setUi(U ui) {
        if (this.ui != null) {
            this.ui.midiInOutDisconnecting();
        }
        this.ui = ui;
        if (ui != null) {
            this.ui.midiInOutConnected();
            uiUpdates.forEach(action -> action.accept(ui));
            updateProperties.forEach(updateProperty -> updateProperty.runAfterSet());
        }
    }

    public void close() {
        stopListening();
        lookup.remove(this);
        sendingTo.clear();
    }

    public Set<MidiInOut> sendingTo() {
        return sendingToReadonly;
    }

    private void ensureNotRecursiveSendingTo(MidiInOut toBeAdded) {
        if (this == toBeAdded) {
            throw new MidiInOut.SendReceiveLoopDetectedException();
        }
        for (MidiInOut receiver : sendingTo()) {
            receiver.ensureNotRecursiveSendingTo(toBeAdded);
        }
    }

    public void startSendingTo(MidiInOut receiver) {
        receiver.ensureNotRecursiveSendingTo(this);
        LOGGER.debug("<{}> starting sending to <{}>", this, receiver);
        if (sendingTo.add(receiver)) {
            receiver.receivingFrom.add(this);
        } else {
            //         throw new IllegalArgumentException("<" + this + "> already sending to <" + receiver + ">");
        }
    }

    public void stopSendingTo(MidiInOut receiver) {
        if (sendingTo.remove(receiver)) {
            receiver.receivingFrom.remove(this);
        } else {
            //           throw new IllegalArgumentException("<" + this + "> was not sending to <" + receiver + ">");
        }
    }

    /**
     * Unlike the public {@link MidiInOut#receive(javax.sound.midi.MidiMessage, long)} method, this
     * method gives implementations a default way to send messages to all registered instances in {@link #sendingTo()].
     *
     * @param message
     * @param timeStamp
     */
    protected void send(MidiMessage message, long timestamp) {
        send(new TimestampedMessage(message, timestamp));
    }

    protected void send(TimestampedMessage message) {
        if (sendingTo.isEmpty()) {
            LOGGER.info("eating message, no delegates for <{}>", this);
        } else {
            LOGGER.debug("iterating over <{}>", sendingTo);
            for (MidiInOut receiver : sendingTo) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("sending message <{}> to <{}>", MIDI_FORMAT.format(message.midiMessage()), receiver);
                }
                receiver.asyncReceive(message);
            }
        }
        uiUpdate(ui -> ui.sent(message.midiMessage(), message.timestamp()));
    }

    protected void send(TimestampedMessage... messages) {
        if (sendingTo.isEmpty()) {
            LOGGER.info("eating message, no delegates for <{}>", this);
        } else {
            LOGGER.debug("iterating over <{}>", sendingTo);
            for (MidiInOut receiver : sendingTo) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("bulk sending <[]> messages to <{}>", messages.length, receiver);
                }
                receiver.asyncReceive(messages);
            }
        }
        for (TimestampedMessage message : messages) {
            getUi().sent(message.midiMessage(), message.timestamp());
        }
    }

    protected void send(MidiMessage message) {
        LOGGER.info("default send <{}>", MIDI_FORMAT.format(message));
        send(message, -1);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        boolean changing = Objects.equals(this.name, name);
        boolean verify = lookup != null && changing;
        if (verify) {
            lookup.verifyName(name);
        }
        this.name = name;
        if (verify) {
            lookup.renamed(this);
        }
        if (changing) {
            getUi().name(name);
        }
    }

    @Override
    public Lookup<MidiInOut> getLookup() {
        return lookup;
    }

    @Override
    public String toString() {
        return getName(); // + " - " + super.toString();
    }

    protected void processReceive(MidiMessage message, long timeStamp) {
        send(message, timeStamp);
    }

    protected void processReceive(MidiMessage message) {
        processReceive(message, -1);
    }

    /**
     * Makes this instance receive midi data, meaning that a sender must call this method to send
     * data to this instance.
     *
     * @param message
     * @param timeStamp
     */
    public void asyncReceive(TimestampedMessage... messages) {
        synchronized (asyncReceiveQueue) {
            for (TimestampedMessage message : messages) {
                if (asyncReceiveQueue.offer(message)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("<{}> on received queue <{}>", this, MIDI_FORMAT.format(message.midiMessage()));
                    }
                } else {
                    LOGGER.error("<{}> dropping message", this);
                }
            }
        }
    }

    public void asyncReceive(MidiMessage message) {
        asyncReceive(new TimestampedMessage(message, -1L));
    }

    public void asyncReceive(MidiMessage message, long timestamp) {
        asyncReceive(new TimestampedMessage(message, timestamp));
    }

    /**
     * False if no messages will ever be sent from this instance, true otherwise. The returned value
     * may change during the lifetime of an instance.
     */
    public boolean isActiveSender() {
        return false;
    }

    /**
     * False if incoming messages are completely ignored, true otherwise. The returned value may
     * change during the lifetime of an instance.
     */
    public boolean isActiveReceiver() {
        return false;
    }

    public Map<Serializable, Serializable> getInfo() {
        return info;
    }

    /**
     * User interfaces and / or other threads must use the async* queues.
     *
     * @return
     */
    protected BlockingQueue<TimestampedMessage> getAsyncReceiveQueue() {
        return asyncReceiveQueue;
    }

    protected void startListening() {
        if (processing.compareAndExchange(false, true)) {
            LOGGER.warn("already processing");
        } else {
            LOGGER.debug("starting listening for incoming signals");
            executorService.execute(receiveProcessTask());
        }
    }

    protected void stopListening() {
        if (processing.compareAndExchange(true, false)) {
            LOGGER.debug("trying to stop listening for incoming signals");
        } else {
            LOGGER.warn("could not stop (not started?)");
        }
    }

    private Runnable receiveProcessTask() {
        return () -> {
            try {
                while (processing.get()) {
                    TimestampedMessage message = getAsyncReceiveQueue().take();
                    processReceive(message.midiMessage(), message.timestamp());
                    uiUpdate(ui -> ui.received(message.midiMessage(), message.timestamp()));
                }
            } catch (InterruptedException e) {
                LOGGER.warn("interrupted", e);
                Thread.interrupted();
                processing.set(false);
            }
            LOGGER.debug("stopped listening for incoming signals");
        };
    }

    private static Set<Integer> COMMAND_NOTE_RELATED;

    static {
        COMMAND_NOTE_RELATED = new HashSet<>();
        COMMAND_NOTE_RELATED.add(ShortMessage.NOTE_OFF);
        COMMAND_NOTE_RELATED.add(ShortMessage.NOTE_ON);
        COMMAND_NOTE_RELATED.add(ShortMessage.POLY_PRESSURE);
    }

    public static boolean noteRelated(ShortMessage shortMessage) {
        return COMMAND_NOTE_RELATED.contains(shortMessage.getCommand());
    }

    public static boolean validNote(int note) {
        return 0 <= note && note <= 127;
    }

    public static String toDec(MidiMessage msg, int maxLength) {
        StringJoiner result = new StringJoiner(" ");
        result.add(Integer.toString(msg.getStatus()));
        for (int i = 1; i < msg.getMessage().length; i++) {
            if (i >= maxLength) {
                result.add("\u2026 (+" + (msg.getMessage().length - maxLength) + ")");
                break;
            }
            result.add(Integer.toString(msg.getMessage()[i]));
        }
        return result.toString();
    }

    public static final ShortMessage createMidiClock() {
        try {
            return new ShortMessage(ShortMessage.TIMING_CLOCK);
        } catch (InvalidMidiDataException e) {
            LOGGER.debug("createMidiTimeCodes", e);
            throw new IllegalStateException(e);
        }
    }

    protected void verifyChannelZeroBased(int channel) {
        if (channel < CHANNEL_MIN_ZERO_BASED || channel > CHANNEL_MAX_ZERO_BASED) {
            throw new IllegalArgumentException("channel must be in range [0,15]");
        }
    }

    protected void verify7Bit(int data) {
        if (data < 0 || data > 127) {
            throw new IllegalArgumentException("value must be range [0,127]");
        }
    }

    protected void verify7BitPlusNone(int data) {
        if (data < -1 || data > 127) {
            throw new IllegalArgumentException("value must be range [0,127]");
        }
    }

    public static void forAllChannels(IntConsumer intConsumer) {
        for (int channel = CHANNEL_MIN_ZERO_BASED; channel < CHANNEL_MAX_ZERO_BASED; channel++) {
            intConsumer.accept(channel);
        }
    }

    protected void forReceiversRecursive(Consumer<? super MidiInOut> action) {
        if (sendingTo != null) { // could be null during init; but then we do not have any senders yet, so...
            sendingTo.forEach(receiver -> {
                action.accept(receiver);
                receiver.forReceiversRecursive(action);
            });
        }
    }

    protected void forSendersRecursive(Consumer<? super MidiInOut> action) {
        if (receivingFrom != null) { // could be null during init; but then we do not have any senders yet, so...
            receivingFrom.forEach(sender -> {
                action.accept(sender);
                sender.forSendersRecursive(action);
            });
        }
    }

    protected void forSendersAndReceiversRecursive(Consumer<? super MidiInOut> action) {
        forReceiversRecursive(action);
        forSendersRecursive(action);
    }

    protected void toReceiversRecursive(Event event) {
        forReceiversRecursive(receiver -> receiver.fromSender(event));
    }

    protected void toSendersRecursive(Event event) {
        forSendersRecursive(receiver -> receiver.fromReceiver(event));
    }

    protected void toSendersAndReceiversRecursive(Event event) {
        forSendersAndReceiversRecursive(midiInOut -> midiInOut.fromReceiver(event));
    }

    public void fromReceiver(Event event) {
    }

    public void fromSender(Event event) {
    }

    public void projectFullyLoaded() {
    }

    protected void addUiUpdate(Consumer<U> action) {
        uiUpdates.add(uiUpdate);
    }

    protected void uiUpdate(Runnable uiUpdate) {
        if (getUi() != null) {
            uiUpdate.run();
        }
    }

    protected void uiUpdate(Consumer<U> action) {
        if (getUi() != null) {
            action.accept(getUi());
        }
    }

    protected void register(UpdateProperty updateProperty) {
        if (!updateProperties.contains(updateProperty)) {
            updateProperties.add(updateProperty);
        }
    }

    public static class SendReceiveLoopDetectedException extends RuntimeException {

    }

}
