package nl.nlcode.m.engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.ClockSource.EXTERNAL;
import static nl.nlcode.m.engine.ClockSource.INTERNAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class MidiClock extends MidiInOut {

    private static final long serialVersionUID = 0L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiClock.class);

    private volatile ClockSource clockSource;

    private AtomicInteger bar;

    private AtomicInteger beat;

    private AtomicInteger tick;

    private AtomicInteger beatsPerBar;

    private AtomicInteger ticksPerBeat;

    private AtomicInteger beats100PerMinute;

    private transient AtomicLong timerStartedWithTickTimeMicroseconds;

    private transient ScheduledExecutorService tickScheduler;

    private transient ScheduledFuture tickFuture;

    private transient AtomicReference<Consumer<Integer>> onTickChangeRef;
    private transient AtomicReference<Consumer<Integer>> onBeatChangeRef;
    private transient AtomicReference<Consumer<Integer>> onBarChangeRef;
    
    private transient AtomicBoolean tickTimerRunning;

    public MidiClock(Project project) {
        super(project);
        clockSource = ClockSource.INTERNAL;
        bar = new AtomicInteger();
        beat = new AtomicInteger();
        tick = new AtomicInteger();
        beatsPerBar = new AtomicInteger(4);
        ticksPerBeat = new AtomicInteger(24);
        beats100PerMinute = new AtomicInteger(12000);
        initSerialization();
        resetPosition();
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        initSerialization();
    }

    private void initSerialization() {
        tickScheduler = Executors.newSingleThreadScheduledExecutor();
        onTickChangeRef = new AtomicReference<>();
        onBeatChangeRef = new AtomicReference<>();
        onBarChangeRef = new AtomicReference<>();
        timerStartedWithTickTimeMicroseconds = new AtomicLong();
        tickTimerRunning = new AtomicBoolean(false);
    }

    public void setOnTickChange(Consumer<Integer> onTickChange) {
        onTickChangeRef.set(onTickChange);
    }

    public void setOnBeatChange(Consumer<Integer> onBeatChange) {
        onBeatChangeRef.set(onBeatChange);
    }

    public void setOnBarChange(Consumer<Integer> onBarChange) {
        onBarChangeRef.set(onBarChange);
    }

    private void changed(AtomicReference<Consumer<Integer>> onChangeRef, int value) {
        Consumer<Integer> onChange = onChangeRef.get();
        if (onChange != null) {
            onChange.accept(value);
        }
    }

    private void barChanged() {
        changed(onBarChangeRef, bar.get());
    }

    private void beatChanged() {
        changed(onBeatChangeRef, beat.get());
    }

    private void tickChanged() {
        changed(onTickChangeRef, tick.get());
    }

    public void resetPosition() {
        tick.set(0);
        tickChanged();
        beat.set(1);
        beatChanged();
        bar.set(1);
        barChanged();
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

    public void setBeatsPerBar(int beatsperbar) {
        beatsPerBar.set(beatsperbar);
    }

    public int getBeatsPerBar() {
        return beatsPerBar.get();
    }

    public float getBeatsPerMinute() {
        return beats100PerMinute.get() / 100;
    }

    public void setBeatsPerMinute(float beatsperminute) {
        setBeats100PerMinute(Math.round(beatsperminute * 100));
    }

    public int getBeats100PerMinute() {
        return beats100PerMinute.get();
    }

    public void setBeats100PerMinute(int b100pm) {
        LOGGER.info("setting bmp to {} / 100", b100pm);
        beats100PerMinute.set(b100pm);

    }

    protected void clockTick() {
        if (tick.incrementAndGet() > ticksPerBeat.get()) {
            tick.set(1);
            if (beat.incrementAndGet() > beatsPerBar.get()) {
                beat.set(1);
                bar.incrementAndGet();
                barChanged();
            }
            beatChanged();
        }
        tickChanged();
        send(createMidiClock());
    }

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
            timerStartedWithTickTimeMicroseconds.set(scheduleInterval);
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
        return (1_000_000L * 60L * 100L) / (beats100PerMinute.get() * ticksPerBeat.get());
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
                send(message);
        }
    }

}
