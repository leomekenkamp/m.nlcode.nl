package nl.nlcode.m.engine;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import nl.nlcode.m.linkui.Updater;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.marshalling.MarshalHelper;
import nl.nlcode.marshalling.Marshallable;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instead of using the Java MIDI API to create software instruments, we do it
 * the other way round. We define our own: this one. The Java MIDI API device is
 * accessed through {@link MidiDeviceLink}. The reason to define an alternative
 * way of defining instruments, is the simple reason that two {@code MidiInOut}
 * devices need to know of each other. The user needs to know which device
 * transmits to which other devices, but also which device receives from which
 * other devices. It was too cumbersome to 'enhance' the Java MIDI API to do
 * this; bridging via {@code MidiDeviceLink} gives far simpler code.
 * <p>
 * Note while there is a public {@link asyncReceive(MidiMessage, int)}, there is
 * no public 'send' method. Implementations need to decide for themselves if
 * they keep total control over their own sending of messages and thus keep
 * their send method(s) to themselves. A receiving instance however must be
 * visible to the world, but there is no guarantee that the data received will
 * actually be processed. Implementations may choose to ignore incoming
 * messages, for instance if they represent something like piano keys. Of course
 * they are free to process them and -keeping with the example- opt to act like
 * a pianola.
 * <p>
 * Subclasses are expected to provide a {@code public static interface Ui} that
 * extends the {@code UI} interface in {@code MidiInOut}.
 *
 * @author leo
 */
public abstract class MidiInOut<U extends MidiInOut.Ui> implements Lookup.Named<MidiInOut>, Marshallable, Updater.Holder<U> {

    public static void verify7Bit(int data) {
        if (data < 0 || data > 127) {
            throw new IllegalArgumentException("value must be range [0,127]");
        }
    }

    public static void verify7BitPlusNone(int data) {
        if (data < -1 || data > 127) {
            throw new IllegalArgumentException("value must be range [-1,127]");
        }
    }

    public static interface Event {

        public MidiInOut source();

    }

    /**
     * <p>
     * Interface to be implemented by user interface classes. This allows for
     * decoupling of the user interface: the user interface knows
     * <code>MidiInOut</code>, but not the other way around.
     * <code>MidiInOut</code> only knows about the interface it should use to
     * 'talk to'.
     */
    public interface Ui {

        default void midiInOutConnected() {
        }

        default void midiInOutDisconnecting() {
        }

        default void received(MidiMessage message, long timestamp) {
        }

        default void sent(MidiMessage message, long timestamp) {
        }

        default void nameChanged(String previousName, String currentName) {
        }

        default void sendingTo(MidiInOut receiver) {
        }

        default void receivingFrom(MidiInOut sender) {
        }

        default void notSendingTo(MidiInOut receiver) {
        }

        default void notReceivingFrom(MidiInOut sender) {
        }
    }

    public static final DateTimeFormatter LOCAL_TIME = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendFraction(NANO_OF_SECOND, 3, 3, true)
            .toFormatter();

    public static final DateTimeFormatter LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .append(LOCAL_TIME)
            .toFormatter();

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected static final int NONE_FOR_7_BIT_INT = -1;

    public static final int CHANNEL_MIN = 0;

    public static final int CHANNEL_MAX = 15;

    public static final int CHANNEL_COUNT = CHANNEL_MAX - CHANNEL_MIN + 1;

    public static final int MIDI_DATA_NONE = -1;

    public static final int MIDI_DATA_MIN = 0;

    public static final int MIDI_DATA_MAX = 127;

    public static final int NOTE_MIN = MIDI_DATA_MIN;

    public static final int NOTE_MAX = MIDI_DATA_MAX;

    public static final int NOTE_COUNT = MIDI_DATA_MAX - MIDI_DATA_MIN + 1;

    public static final int MIDI_VELOCITY_MIN = MIDI_DATA_MIN;

    public static final int MIDI_VELOCITY_MAX = MIDI_DATA_MAX;

    protected static final MidiMessageFormat MIDI_FORMAT = new MidiMessageFormat();

    private transient Set<MidiInOut> sendingToReadonly;

    private transient Set<MidiInOut> receivingFromReadonly;

    private transient Lookup<MidiInOut> lookup;

    private transient BlockingQueue<TimestampedMidiMessage> asyncReceiveQueue;

    private transient AtomicBoolean processing;

    private transient ExecutorService executorService;

    private transient Set<Updater<?, U, ? extends Updater.Holder<U>>> updaters;

    private transient Map<String, Updater<?, U, ? extends Updater.Holder<U>>> nameToUpdater;

    private transient U ui;

    private transient Project project;

    private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private transient Future receiveProcessTask;

    public static final MidiInOut[] EMPTY_ARRAY = new MidiInOut[]{};

    // persisted data
    private String name;

    private Set<MidiInOut> sendingTo;

    private Set<MidiInOut> receivingFrom;

    private Map<Serializable, Serializable> info;

    public static record SaveData0(
            int id,
            String name,
            Marshalled<MidiInOut>[] sendingTo,
            Marshalled<MidiInOut>[] receivingFrom,
            Map<Serializable, Serializable> info) implements Marshalled<MidiInOut> {

        @Override
        public void unmarshalInto(Context context, MidiInOut target) {
            target.name = name();
            MarshalHelper.unmarshalAddAll(context, sendingTo(), target.sendingTo);
            MarshalHelper.unmarshalAddAll(context, receivingFrom(), target.receivingFrom);
            target.info.putAll(info());
        }

        @Override
        public MidiInOut createMarshallable() {
            throw new UnsupportedOperationException("class of <" + name + "> is abstract");
        }
    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                name,
                MarshalHelper.marshallToArray(context, sendingTo),
                MarshalHelper.marshallToArray(context, receivingFrom),
                info
        );
    }

    protected MidiInOut() {
        sendingTo = ConcurrentHashMap.newKeySet();
        receivingFrom = ConcurrentHashMap.newKeySet();
        info = new HashMap<>();
        asyncReceiveQueue = new LinkedBlockingQueue();
        processing = new AtomicBoolean(false);
        updaters = new HashSet<>();
        nameToUpdater = new HashMap<>();
        sendingToReadonly = Collections.unmodifiableSet(sendingTo);
        receivingFromReadonly = Collections.unmodifiableSet(receivingFrom);
    }

    public final void openWith(Project project) {
        this.project = project;
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

    protected Project getProject() {
        return project;
    }

    private U getUi() {
        return ui;
    }

    public void setUi(U ui) {
        if (this.ui != null) {
            this.ui.midiInOutDisconnecting();
        }
        this.ui = ui;
        if (ui != null) {
            this.ui.midiInOutConnected();
            syncUi();
        }
    }

    protected void syncUi() {
        updaters.stream().forEach(updater -> updater.runAfterChange());
        // nameToUpdater.values().stream().forEach(updater -> updater.runAfterChange());
    }

    public void close() {
        stopListening();
        lookup.remove(this);
        while (!sendingTo.isEmpty()) {
            stopSendingTo(sendingTo.iterator().next());
        }
        while (!receivingFrom.isEmpty()) {
            receivingFrom.iterator().next().stopSendingTo(this);
        }
    }

    public Set<MidiInOut> sendingTo() {
        return sendingToReadonly;
    }

    public Set<MidiInOut> receivingFrom() {
        return receivingFromReadonly;
    }

    private void ensureNotRecursiveSendingTo(MidiInOut toBeAdded) {
        if (this == toBeAdded) {
            throw new MidiInOut.SendReceiveLoopDetectedException();
        }
        for (MidiInOut receiver : sendingTo()) {
            receiver.ensureNotRecursiveSendingTo(toBeAdded);
        }
    }

    public boolean startSendingTo(MidiInOut<? extends Ui> receiver) {
        receiver.ensureNotRecursiveSendingTo(this);
        LOGGER.debug("<{}> starting sending to <{}>", this, receiver);
        if (sendingTo.add(receiver)) {
            receiver.receivingFrom.add(this);
            uiUpdate(ui -> ui.sendingTo(receiver));
            receiver.uiUpdate(ui -> ui.receivingFrom(this));
            return true;
        } else {
            return false;
        }
    }

    public boolean stopSendingTo(MidiInOut<? extends Ui> receiver) {
        if (sendingTo.remove(receiver)) {
            receiver.receivingFrom.remove(this);
            uiUpdate(ui -> ui.notSendingTo(receiver));
            receiver.uiUpdate(ui -> ui.notReceivingFrom(this));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unlike the public
     * {@link MidiInOut#receive(javax.sound.midi.MidiMessage, long)} method,
     * this method gives implementations a default way to send messages to all
     * registered instances in {@link #sendingTo()].
     *
     * @param message
     * @param timeStamp
     */
    protected void send(MidiMessage message, long timestamp) {
        send(new TimestampedMidiMessage(message, timestamp));
    }

    protected void send(TimestampedMidiMessage message) {
        if (sendingTo.isEmpty()) {
            LOGGER.debug("eating message, no delegates for <{}>", this);
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

    protected void send(TimestampedMidiMessage... messages) {
        if (sendingTo.isEmpty()) {
            LOGGER.debug("eating message, no delegates for <{}>", this);
        } else {
            LOGGER.debug("iterating over <{}>", sendingTo);
            for (MidiInOut receiver : sendingTo) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("bulk sending <[]> messages to <{}>", messages.length, receiver);
                }
                receiver.asyncReceive(messages);
            }
        }
        for (TimestampedMidiMessage message : messages) {
            uiUpdate(ui -> ui.sent(message.midiMessage(), message.timestamp()));
        }
    }

    protected void send(MidiMessage message) {
        LOGGER.debug("default send <{}>", MIDI_FORMAT.format(message));
        send(message, -1);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldName = this.name;
        boolean changing = !Objects.equals(oldName, name);
        boolean verify = lookup != null && changing;
        if (verify) {
            lookup.verifyNameAndExecute(name, () -> {this.name = name;});
            lookup.renamed(this); // FIXME: this looks strange
        } else {
            this.name = name;
        }
        if (changing) {
            setDirty();
            uiUpdate(ui -> ui.nameChanged(oldName, name));
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

    protected final void processReceive(MidiMessage message) {
//        send(message, -1);
        processReceive(message, -1);
    }

    /**
     * Makes this instance receive midi data, meaning that a sender must call
     * this method to send data to this instance.
     *
     * @param message
     * @param timeStamp
     */
    public void asyncReceive(TimestampedMidiMessage... messages) {
        synchronized (asyncReceiveQueue) {
            for (TimestampedMidiMessage message : messages) {
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
        asyncReceive(new TimestampedMidiMessage(message, -1L));
    }

    public void asyncReceive(MidiMessage message, long timestamp) {
        asyncReceive(new TimestampedMidiMessage(message, timestamp));
    }

    /**
     * False if no messages will ever be sent from this instance, true
     * otherwise. The returned value may change during the lifetime of an
     * instance.
     */
    public boolean isActiveSender() {
        return false;
    }

    /**
     * False if incoming messages are completely ignored, true otherwise. The
     * returned value may change during the lifetime of an instance.
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
    protected BlockingQueue<TimestampedMidiMessage> getAsyncReceiveQueue() {
        return asyncReceiveQueue;
    }

    protected void startListening() {
        if (processing.getAndSet(true)) {
            LOGGER.warn("already processing");
        } else {
            LOGGER.debug("starting listening for incoming signals");
            receiveProcessTask = executorService.submit(createReceiveProcessTask());
        }
    }

    protected void stopListening() {
        if (processing.getAndSet(false)) {
            LOGGER.debug("trying to stop listening for incoming signals");
            receiveProcessTask.cancel(true);
            receiveProcessTask = null;
        } else {
            LOGGER.warn("could not stop (not started?)");
        }
    }

    private Runnable createReceiveProcessTask() {
        return () -> {
            while (processing.get()) {
                try {
                    TimestampedMidiMessage message = getAsyncReceiveQueue().take();
                    processReceive(message.midiMessage(), message.timestamp());
                    uiUpdate(ui -> ui.received(message.midiMessage(), message.timestamp()));
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    if (!processing.get()) {
                        LOGGER.warn("weird: interrupted, but processing is still true");
                    }
                }
            }
            LOGGER.debug("stopped listening for incoming signals");
        };
    }

    public void setDirty() {
        if (project != null) {
            project.setDirty();
        }
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
            result.add(String.format("%03d", msg.getMessage()[i]));
        }
        return result.toString();
    }

    public static String toDec(byte[] rawMsg, int maxLength) {
        StringJoiner result = new StringJoiner(" ");
        for (int i = 0; i < rawMsg.length; i++) {
            if (i >= maxLength) {
                result.add("\u2026 (+" + (rawMsg.length - maxLength) + ")");
                break;
            }
            result.add(String.format("%03d", Byte.toUnsignedInt(rawMsg[i])));
        }
        return result.toString();
    }

    public static String toHex(MidiMessage msg, int maxLength) {
        StringJoiner result = new StringJoiner(" ");
        result.add(Integer.toHexString(msg.getStatus()));
        for (int i = 1; i < msg.getMessage().length; i++) {
            if (i >= maxLength) {
                result.add("\u2026 (+" + (msg.getMessage().length - maxLength) + ")");
                break;
            }
            result.add(String.format("%02x", Byte.toUnsignedInt(msg.getMessage()[i])));
        }
        return result.toString();
    }

    public static String toHex(byte[] rawMsg, int maxLength) {
        StringJoiner result = new StringJoiner(" ");
        for (int i = 0; i < rawMsg.length; i++) {
            if (i >= maxLength) {
                result.add("\u2026 (+" + (rawMsg.length - maxLength) + ")");
                break;
            }
            result.add(String.format("%02x", rawMsg[i]));
        }
        return result.toString();
    }

    public static final ShortMessage createTimingClock() {
        try {
            return new ShortMessage(ShortMessage.TIMING_CLOCK);
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final boolean isTimingClock(MidiMessage message) {
        return message.getLength() == 1 && message.getStatus() == ShortMessage.TIMING_CLOCK;
    }

    public static final ShortMessage createShortMessage(int command, int channel, int note, int velocity) {
        try {
            return new ShortMessage(command, channel, note, velocity);
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static void verifyChannel(int channel) {
        if (channel < CHANNEL_MIN || channel > CHANNEL_MAX) {
            throw new IllegalArgumentException("channel {" + channel + "} must be in range [0,15]");
        }
    }

    protected static void verifyNote(int data) {
        verify7Bit(data);
    }

    protected static void verifyVelocity(int data) {
        verify7Bit(data);
    }

    public static void forAllChannels(IntConsumer intConsumer) {
        for (int channel = CHANNEL_MIN; channel <= CHANNEL_MAX; channel++) {
            intConsumer.accept(channel);
        }
    }

    public static void forAllNotes(IntConsumer intConsumer) {
        for (int note = NOTE_MIN; note <= NOTE_MAX; note++) {
            intConsumer.accept(note);
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

    public void uiUpdate(Consumer<U> action) {
        if (getUi() != null) {
            action.accept(getUi());
        }
    }

    @Override
    public void unregister(Updater<?, U, ? extends Updater.Holder<U>> updater) {
        updaters.remove(updater);
        nameToUpdater.remove(updater.getName(), updater);
    }

    @Override
    public void register(Updater<?, U, ? extends Updater.Holder<U>> updater) {
        updaters.add(updater);
        if (!Updater.TODO_NAME.equals(updater.getName())) {
            nameToUpdater.put(updater.getName(), updater);
        }
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        if (true) {
            throw new RuntimeException("is this code even used?");
        }
        return propertyChangeSupport;
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        if (true) {
            throw new RuntimeException("is this code even used?");
        }
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }

    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        if (true) {
            throw new RuntimeException("is this code even used?");
        }
        propertyChangeSupport.removePropertyChangeListener(name, listener);
    }

    public static class SendReceiveLoopDetectedException extends RuntimeException {
    }

    public static String toString(ShortMessage msg) {
        return msg == null ? "<null>"
                : "[cmd: " + msg.getCommand() + "; ch: " + msg.getChannel() + "; d1: " + msg.getData1() + "; d2: " + msg.getData2();
    }

    /**
     *
     * @return the updater 'properties' that this instance holds
     */
    public Map<String, Updater<?, U, ? extends Updater.Holder<U>>> getNameToUpdater() {
        return nameToUpdater;
    }
;
}
