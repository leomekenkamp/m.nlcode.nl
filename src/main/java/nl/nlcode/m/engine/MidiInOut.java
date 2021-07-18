package nl.nlcode.m.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
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
 * Note while there is a public {@link receive(MidiMessage, int)}, there is no public 'send' method.
 * Implementations need to decide for themselves if they keep total control over their own sending
 * of messages and thus keep their send method(s) private. A receiving instance however must be
 * visible to the world, but there is no guarantee that the data received will actually be
 * processed. Implementations may choose to ignore incoming messages, for instance if they represent
 * something like piano keys. Of course they are free to process them and -keeping with the example-
 * opt to act like a pianola.
 *
 * @author leo
 */
public abstract class MidiInOut implements Serializable, Lookup.Named<MidiInOut> {

    private static final long serialVersionUID = 0L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiInOut.class);

    private static final MidiMessageFormat MIDI_FORMAT = new MidiMessageFormat();

    private Set<MidiInOut> sendingTo;

    private Map<Serializable, Serializable> info;

    private transient Set sendingToReadonly;

    private transient Lookup<MidiInOut> lookup;

    private transient BlockingQueue<MidiMessage> asyncReceiveQueue;

    private transient AtomicBoolean processing;

    private transient ExecutorService executorService;

    private String name;

    private transient AtomicReference<BiConsumer<MidiMessage, Long>> onMidiMessageReceiveRef;
    
    protected MidiInOut(Project project) {
        this(project.getMidiInOutLookup(), project.getMidiInOutExecutorService());
    }

    protected MidiInOut(Lookup<MidiInOut> lookup, ExecutorService executorService) {
        sendingTo = ConcurrentHashMap.newKeySet();
        info = new HashMap<>();
        deserializationInit();
        init(lookup, executorService);
    }

    private void deserializationInit() {
        sendingToReadonly = Collections.unmodifiableSet(sendingTo);
        asyncReceiveQueue = new LinkedBlockingQueue();
        processing = new AtomicBoolean(false);
        onMidiMessageReceiveRef = new AtomicReference<>();
    }

    public final void init(Lookup<MidiInOut> lookup, ExecutorService executorService) {
        if (this.lookup != null) {
            throw new UnsupportedOperationException("can init only once");
        }
        if (lookup == null) {
            throw new IllegalArgumentException("lookup cannot be null");
        }
        if (executorService == null) {
            throw new IllegalArgumentException("executorService cannot be null");
        }
        this.lookup = lookup;
        lookup.add(this);
        this.executorService = executorService;
        startListening();
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        deserializationInit();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    public void close() {
        stopListening();
        lookup.remove(this);
        sendingTo.clear();
    }

    public Set<MidiInOut> sendingTo() {
        return sendingToReadonly;
    }

    public void startSendingTo(MidiInOut receiver) {
        LOGGER.debug("{} starting sending to {}", this, receiver);
        if (sendingTo.add(receiver)) {
        } else {
            throw new IllegalArgumentException(this + " already sending to " + receiver);
        }
    }

    public void stopSendingTo(MidiInOut receiver) {
        if (sendingTo.remove(receiver)) {
        } else {
            throw new IllegalArgumentException(this + " was not sending to " + receiver);
        }
    }

    /**
     * Unlike the public {@link MidiInOut#receive(javax.sound.midi.MidiMessage, long)} method, this
     * method gives implementations a default way to send messages to all registered instances in {@link #sendingTo()].
     *
     * @param message
     * @param timeStamp
     */
    protected void send(MidiMessage message, long timeStamp) {
        if (sendingTo.isEmpty()) {
            LOGGER.info("eating message, no delegates for {}", this);
        } else {
            LOGGER.debug("iterating over {}", sendingTo);
            for (MidiInOut receiver : sendingTo) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("sending message {} to {}", MIDI_FORMAT.format(message), receiver);
                }
                receiver.asyncReceive(message, timeStamp);
            }
        }
    }

    protected void send(MidiMessage message) {
        LOGGER.info("default send {}", MIDI_FORMAT.format(message));
        send(message, -1);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        boolean changeing = lookup != null && !name.equals(this.name);
        if (changeing) {
            lookup.verifyName(name);
        }
        this.name = name;
        if (changeing) {
            lookup.renamed(this);
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
        // by default, do nothing
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
    public void asyncReceive(MidiMessage message, long timeStamp) {
        if (asyncReceiveQueue.offer(message)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} on received queue {}", this, MIDI_FORMAT.format(message));
            }
        } else {
            LOGGER.error("{} dropping message", this);
        }
    }

    public BiConsumer<MidiMessage, Long> setOnMidiMessageReceive() {
        return onMidiMessageReceiveRef.get();
    }

    public void setOnMidiMessageReceive(BiConsumer<MidiMessage, Long> onAsyncReceive) {
        onMidiMessageReceiveRef.set(onAsyncReceive);
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
    protected BlockingQueue<MidiMessage> getAsyncReceiveQueue() {
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
                    MidiMessage midiMessage = getAsyncReceiveQueue().poll(250, TimeUnit.MILLISECONDS);
                    if (midiMessage != null) {
                        processReceive(midiMessage);
                        BiConsumer<MidiMessage, Long> onMidiMessageReceive = onMidiMessageReceiveRef.get();
                        if (onMidiMessageReceive != null) {
                            onMidiMessageReceive.accept(midiMessage, -1L);
                        }
                    }
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

}
