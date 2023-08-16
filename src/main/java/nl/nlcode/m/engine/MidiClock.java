package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.ClockSource.EXTERNAL;
import static nl.nlcode.m.engine.ClockSource.INTERNAL;
import nl.nlcode.m.linkui.DoubleUpdateProperty;
import nl.nlcode.m.linkui.IntUpdateProperty;
import nl.nlcode.m.linkui.LongUpdateProperty;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class MidiClock<U extends MidiClock.Ui> extends MidiInOut<U> {

    public static interface Ui extends MidiInOut.Ui {

        void timingClock();

        void beatChanged();

        void barChanged();

        void bpmChanged();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private volatile ClockSource clockSource;

    private final IntUpdateProperty<U, MidiClock<U>> bar;

    private final IntUpdateProperty<U, MidiClock<U>> beat;

    private final IntUpdateProperty<U, MidiClock<U>> tick;

    private final IntUpdateProperty<U, MidiClock<U>> beatsPerBar;

    private final IntUpdateProperty<U, MidiClock<U>> ticksPerBeat;

    private final DoubleUpdateProperty<U, MidiClock<U>> beatsPerMinute;

    public static record SaveData0(
            int id,
            ClockSource clockSource,
            int bar,
            int beat,
            int tick,
            int beatsPerBar,
            int ticksPerBeat,
            double beatsPerMinute,
            Marshalled<MidiInOut> s) implements Marshalled<MidiClock> {

        @Override
        public void unmarshalInto(Marshalled.Context context, MidiClock target) {
            target.clockSource = clockSource();
            target.bar.set(bar());
            target.beat.set(beat());
            target.tick.set(tick());
            target.beatsPerBar.set(beatsPerBar());
            target.ticksPerBeat.set(ticksPerBeat());
            target.beatsPerMinute.set(beatsPerMinute());
            s.unmarshalInto(context, target);
        }

        @Override
        public MidiClock createMarshallable() {
            return new MidiClock();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new MidiClock.SaveData0(
                id,
                clockSource,
                bar.get(),
                beat.get(),
                tick.get(),
                beatsPerBar.get(),
                ticksPerBeat.get(),
                beatsPerMinute.get(),
                super.marshalInternal(-1, context)
        );
    }

    private transient ScheduledExecutorService tickScheduler;

    private transient ScheduledFuture tickFuture;

    private transient AtomicBoolean tickTimerRunning;

    public MidiClock() {
        bar = new IntUpdateProperty<>(this, 0);
        beat = new IntUpdateProperty<>(this, 0);
        tick = new IntUpdateProperty<>(this, 0);
        beatsPerBar = new IntUpdateProperty<>(this, 4);
        ticksPerBeat = new IntUpdateProperty<>(this, 24);
        beatsPerMinute = new DoubleUpdateProperty<>(this, 120.00f);

        clockSource = ClockSource.INTERNAL;
        bar.setAfterChange(this, (ui) -> ui.barChanged());
        beat.setAfterChange(this, ui -> ui.beatChanged());
        tick.setAfterChange(this, ui -> ui.timingClock());
        ticksPerBeat.setAfterChange(this, ui -> {
        });
        tickScheduler = Executors.newSingleThreadScheduledExecutor();
        tickTimerRunning = new AtomicBoolean(false);

        resetPosition();
    }

    public void resetPosition() {
        tick.set(0);
        beat.set(0);
        bar.set(0);
    }

    public int getTick() {
        return tick.get();
    }

    public int getBeat() {
        return beat.get();
    }

    public int getBar() {
        return bar.get();
    }

    public void setBar(int bar) {
        throw new UnsupportedOperationException();
    }

    public void setBeatsPerBar(int beatsPerBar) {
        this.beatsPerBar.set(beatsPerBar);
    }

    public int getBeatsPerBar() {
        return beatsPerBar.get();
    }

    public IntUpdateProperty<U, MidiClock<U>> beatsPerBar() {
        return beatsPerBar;
    }

    public double getBeatsPerMinute() {
        return beatsPerMinute.get();
    }

    public void setBeatsPerMinute(double beatsPerMinute) {
        this.beatsPerMinute.set(beatsPerMinute);
    }

    public DoubleUpdateProperty<U, MidiClock<U>> beatsPerMinute() {
        return beatsPerMinute;
    }

    protected void clockTick() {
        int newTick = tick.withReference(ref -> {
            return ref.accumulateAndGet(1, (current, add) -> {
                if (current >= ticksPerBeat.get()) {
                    return 1;
                } else {
                    return current + add;
                }
            });
        });

        if (newTick == 1) {
            int newBeat = beat.withReference(ref -> {
                return ref.accumulateAndGet(1, (current, add) -> {
                    if (current >= beatsPerBar.get()) {
                        return 1;
                    } else {
                        return current + add;
                    }
                });
            });

            if (newBeat == 1 && bar.get() < 100000) {
                bar.withReference((AtomicInteger ref) -> ref.incrementAndGet());
            }
        }

        send(createTimingClock());
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

    @Override
    public boolean isActiveSender() {
        return true;
    }

    public ClockSource getClockSource() {
        return clockSource;
    }

    public void setClockSource(ClockSource clockSource) {
        if (clockSource == null) {
            throw new IllegalArgumentException("clockSource must not be null");
        }
        if (this.clockSource == INTERNAL && clockSource == EXTERNAL) {
            stopTimer();
        }
        this.clockSource = clockSource;
    }

    public void start() {
        stopTimer();
        resetPosition();
        if (clockSource == INTERNAL) {
            startTimer();
        }
        send(ShortMessage.START);
    }

    public void resume() {
        if (clockSource == INTERNAL) {
            startTimer();
        }
        send(ShortMessage.CONTINUE);
    }

    public void stop() {
        stopTimer();
        send(ShortMessage.STOP);
    }

    private void send(int status) {
        try {
            send(new ShortMessage(status));
        } catch (InvalidMidiDataException e) {
            LOGGER.error("huh?", e);
        }
    }

    private void startTimer() {
        startTimer(false);
    }

    private void startTimer(boolean forceRestart) {
        if ((!tickTimerRunning.compareAndExchange(false, true) && !forceRestart) || (forceRestart && tickFuture.isCancelled())) {
            long scheduleInterval = getTickTimeMicroseconds();
            tickFuture = tickScheduler.scheduleAtFixedRate(() -> {
                clockTick();
                if (scheduleInterval != getTickTimeMicroseconds()) {
                    tickFuture.cancel(false);
                    startTimer(true);
                }
            }, 0, scheduleInterval, TimeUnit.MICROSECONDS);
        }
    }

    private void stopTimer() {
        if (tickTimerRunning.compareAndExchange(true, false)) {
            tickFuture.cancel(false);
        }
    }

    public final long getTickTimeMicroseconds() {
        // answer needed: (microsecond / tick)
        // we have:
        //    - bmp -> b / min
        //    - 60 sec / min
        //    - 24 tick / b
        // so: (microsec / sec) * (sec / min) / ((b / min) * (tick / b)) = microsec / tic
        // -> (1_000_000 * 60) / ((b / min) * (tick / b)

        // (b / min) * (sec / tick)
        return (1_000_000L * 60L * 100L) / ((long) (beatsPerMinute.get() * 100L) * ticksPerBeat.get());
    }

    @Override
    public void close() {
        tickScheduler.shutdown();
        super.close();
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        switch (message.getStatus()) {
            case ShortMessage.MIDI_TIME_CODE:
                if (clockSource == EXTERNAL) {
                    clockTick();
                }
                break;
            case ShortMessage.START:
                start();
                break;
            case ShortMessage.STOP:
                stop();
                break;
            case ShortMessage.CONTINUE:
                resume();
                break;
            default:
                super.processReceive(message, timeStamp);
        }
    }

}
