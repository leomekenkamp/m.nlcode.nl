package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import nl.nlcode.m.JvmStuff;
import nl.nlcode.m.linkui.ObjectUpdateProperty;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public abstract class TimeSensitiveMidiInOut<U extends TimeSensitiveMidiInOut.Ui> extends MidiInOut<U> {

    public static interface Ui extends MidiInOut.Ui {

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected transient final Object SYNCHRONIZATION_LOCK = new Object();

    private static ScheduledExecutorService scheduledExecutorService = JvmStuff.getInstance().scheduledExecutorService();

    private ScheduledFuture<?> timerFuture;

    protected final ObjectUpdateProperty<TickSource, U, TimeSensitiveMidiInOut<U>> tickSource = new ObjectUpdateProperty<>(TickSource.TIME);

    public static record SaveData0(
            int id,
            TickSource tickSource,
            Marshalled<MidiInOut> s) implements Marshalled<TimeSensitiveMidiInOut> {

        @Override
        public void unmarshalInto(Context context, TimeSensitiveMidiInOut target) {
            target.tickSource.set(tickSource());
            s.unmarshalInto(context, target);
        }

        @Override
        public TimeSensitiveMidiInOut createMarshallable() {
            throw new UnsupportedOperationException("class of <" + this.toString() + "> is abstract");
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                tickSource.get(),
                super.marshalInternal(-1, context)
        );
    }

    public TimeSensitiveMidiInOut() {
        tickSource.register(this);
        tickSource.addListener((oldValue, newValue) -> {
            timer(newValue == TickSource.TIME);
        });
        timer(tickSource.get() == TickSource.TIME);
    }

    private void timer(boolean run) {
        synchronized (SYNCHRONIZATION_LOCK) {
            if (run) {
                if (timerFuture == null || timerFuture.isDone()) {
                    // initialDelay because subcalsses will need to have finished their ctor. A bit shaky, this...
                    timerFuture = scheduledExecutorService.scheduleAtFixedRate(() -> unsynchronizedTick(), 1000, 20, TimeUnit.MILLISECONDS);
                }
            } else {
                if (timerFuture != null) {
                    timerFuture.cancel(false);
                    timerFuture = null;
                }
            }
        }
    }

    protected void unsynchronizedTick() {
        try {
            synchronized (SYNCHRONIZATION_LOCK) {
                synchronizedTick();
            }
        } catch (RuntimeException e) {
            LOGGER.error("crash async", e);
        }
     }

    

    protected abstract void synchronizedTick();

    public ObjectUpdateProperty<TickSource, U, ? extends TimeSensitiveMidiInOut<U>> tickSource() {
        return tickSource;
    }

    public TickSource getTickSource() {
        return tickSource.get();
    }

    public void setTickSource(TickSource tickSource) {
        this.tickSource.set(tickSource);
    }

    protected void processReceiveTimingClock(long timeStamp) {
        if (tickSource.get() == TickSource.MIDI) {
            synchronizedTick();
        }
    }
}
