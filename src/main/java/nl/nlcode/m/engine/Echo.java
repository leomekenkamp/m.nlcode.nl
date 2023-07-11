package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.m.linkui.IntUpdateProperty;
import nl.nlcode.m.linkui.ObjectUpdateProperty;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class Echo<U extends Echo.Ui> extends MidiInOut<U> {

    public static interface Ui extends MidiInOut.Ui {

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private class Bucket {

        public long targetTimeIndex;
        public final Set<ShortMessage> messages;

        public Bucket() {
            targetTimeIndex = currentTimeIndex + echoLength.get();
            messages = new HashSet();
        }
    }

    private volatile long currentTimeIndex;

    private IntUpdateProperty echoLength = new IntUpdateProperty(20, 1, 480);

    private IntUpdateProperty absoluteVelocityDecrease = new IntUpdateProperty(0, 0, 127);
    private IntUpdateProperty relativeVelocityDecrease = new IntUpdateProperty(15, 0, 99);

    private volatile Bucket futureBucket;

    private transient LinkedList<Bucket> buckets = new LinkedList<>();

    private transient volatile AtomicInteger notePlayCount[][];
    private transient volatile AtomicInteger notePlannedCount[][];

    private transient ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> timerFuture;

    private ObjectUpdateProperty<TickSource, U, Echo<U>> tickSource = new ObjectUpdateProperty<>(TickSource.TIME);

    public static record SaveData0(
            int id,
            TickSource tickSource,
            int echoLength,
            int absoluteVelocityDecrease,
            int relativeVelocityDecrease,
            Marshalled<MidiInOut> s) implements Marshalled<Echo> {

        @Override
        public void unmarshalInto(Context context, Echo target) {
            target.tickSource.set(tickSource());
            target.echoLength.set(echoLength());
            target.absoluteVelocityDecrease.set(absoluteVelocityDecrease());
            target.relativeVelocityDecrease.set(relativeVelocityDecrease());
            s.unmarshalInto(context, target);
        }

        @Override
        public Echo createMarshallable() {
            return new Echo();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                tickSource.get(),
                echoLength.get(),
                absoluteVelocityDecrease.get(),
                relativeVelocityDecrease.get(),
                super.marshalInternal(-1, context)
        );
    }

    public Echo() {
        notePlayCount = new AtomicInteger[CHANNEL_COUNT][NOTE_COUNT];
        forAllChannels(channel -> forAllNotes(note -> notePlayCount[channel][note] = new AtomicInteger()));
        notePlannedCount = new AtomicInteger[CHANNEL_COUNT][NOTE_COUNT];
        forAllChannels(channel -> forAllNotes(note -> notePlannedCount[channel][note] = new AtomicInteger()));
        tickSource.register(this);
        tickSource.addListener((oldValue, newValue) -> {
            timer(newValue == TickSource.TIME);
        });
        timer(tickSource.get() == TickSource.TIME);
    }

    private void timer(boolean run) {
        synchronized (buckets) {
            if (run) {
                if (timerFuture == null || timerFuture.isDone()) {
                    timerFuture = scheduledExecutorService.scheduleAtFixedRate(() -> timeTick(), 0, 20, TimeUnit.MILLISECONDS);
                }
            } else {
                if (timerFuture != null) {
                    timerFuture.cancel(false);
                    timerFuture = null;
                }
            }
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

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage incoming) {
            synchronized (buckets) {
                if (incoming.getStatus() == ShortMessage.TIMING_CLOCK) {
                    send(message, timeStamp);
                    if (tickSource.get() == TickSource.MIDI) {
                        tick();
                    }
                } else if (incoming.getCommand() == ShortMessage.NOTE_ON || incoming.getCommand() == ShortMessage.NOTE_OFF) {
                    sendAndEcho(incoming, timeStamp);
                } else {
                    send(message, timeStamp);
                }
            }
        } else {
            send(message, timeStamp);
        }
    }

    private void timeTick() {
        synchronized (buckets) {
            tick();
        }
    }

    private void tick() {
        futureBucket = null;
        currentTimeIndex += 1;
        while (!buckets.isEmpty() && buckets.getFirst().targetTimeIndex <= currentTimeIndex) {
            Bucket bucket = buckets.removeFirst();
            for (ShortMessage message : bucket.messages) {
                sendAndEcho(message, -1);
            }
        }
    }

    private Bucket getFutureBucket() {
        if (futureBucket == null) {
            futureBucket = new Bucket();
            buckets.addLast(futureBucket);
        }
        return futureBucket;
    }

    private void sendAndEcho(ShortMessage message, long timeStamp) {
        if (message.getCommand() == ShortMessage.NOTE_ON) {
            send(message, timeStamp);
            notePlayCount[message.getChannel()][message.getData1()].incrementAndGet();
        } else if (message.getCommand() == ShortMessage.NOTE_OFF) {
            // should prevent a note off to cancel other note on events
            if (notePlayCount[message.getChannel()][message.getData1()].decrementAndGet() == 0) {
                send(message, timeStamp);
            }
        }
        int oldVelocity = message.getData2();
        int newVelocity = oldVelocity;
        newVelocity = (newVelocity * (100 - relativeVelocityDecrease.get())) / 100;
        newVelocity -= absoluteVelocityDecrease.get();
        if (newVelocity == oldVelocity) {
            newVelocity -= 1;
        }
        if (message.getCommand() == ShortMessage.NOTE_OFF && newVelocity <= 0) {
            newVelocity = 1;
        }
        if (newVelocity > 0) {
            ShortMessage echo = createShortMessage(message.getCommand(), message.getChannel(), message.getData1(), newVelocity);
            if (message.getCommand() == ShortMessage.NOTE_ON) {
                notePlannedCount[message.getChannel()][message.getData1()].incrementAndGet();
                getFutureBucket().messages.add(echo);
            } else if (message.getCommand() == ShortMessage.NOTE_OFF) {
                if (notePlannedCount[message.getChannel()][message.getData1()]
                        .getAndAccumulate(-1, (current, add) -> {
                            if (current > 0) {
                                return current + add;
                            } else {
                                return 0;
                            }
                        }) > 0) {
                    getFutureBucket().messages.add(echo);
                }
            }
        }
    }

    public ObjectUpdateProperty<TickSource, U, Echo<U>> tickSource() {
        return tickSource;
    }

    public TickSource getTickSource() {
        return tickSource.get();
    }

    public void setTickSource(TickSource tickSource) {
        this.tickSource.set(tickSource);
    }

    public int getAbsoluteVelocityDecrease() {
        return absoluteVelocityDecrease.get();
    }

    public void setAbsoluteVelocityDecrease(int absoluteVelocityDecrease) {
        this.absoluteVelocityDecrease.set(absoluteVelocityDecrease);
    }

    public IntUpdateProperty absoluteVelocityDecrease() {
        return absoluteVelocityDecrease;
    }

    public int getRelativeVelocityDecrease() {
        return relativeVelocityDecrease.get();
    }

    public void setRelativeVelocityDecrease(int relativeVelocityDecrease) {
        this.relativeVelocityDecrease.set(relativeVelocityDecrease);
    }

    public IntUpdateProperty relativeVelocityDecrease() {
        return relativeVelocityDecrease;
    }

    public int getEchoLength() {
        return echoLength.get();
    }

    public void setEchoLength(int echoLength) {
        this.echoLength.set(echoLength);
    }

    public IntUpdateProperty echoLength() {
        return echoLength;
    }
}
