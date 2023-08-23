package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static javax.sound.midi.ShortMessage.NOTE_ON;
import static nl.nlcode.m.engine.MidiInOut.CHANNEL_COUNT;
import static nl.nlcode.m.engine.MidiInOut.forAllChannels;
import nl.nlcode.m.linkui.BooleanUpdater;
import nl.nlcode.m.linkui.IntUpdater;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class Transposer<U extends Transposer.Ui> extends MidiInOut<U> {

    public static interface Ui extends MidiInOut.Ui {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final BooleanUpdater<U, Transposer<U>>[] channelUpdater;

    private final IntUpdater<U, Transposer<U>> octavesUpdater;

    private final IntUpdater<U, Transposer<U>> semitonesUpdater;

    public static record SaveData0(
            int id,
            int octaves,
            int semitones,
            boolean[] channel,
            Marshalled<MidiInOut> s) implements Marshalled<Transposer> {

        @Override
        public void unmarshalInto(Context context, Transposer target) {
            s.unmarshalInto(context, target);
            target.octavesUpdater.set(octaves());
            target.semitonesUpdater.set(semitones());
            forAllChannels(channel -> target.setChannel(channel, channel()[channel]));
        }

        @Override
        public Transposer createMarshallable() {
            return new Transposer();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                octavesUpdater.get(),
                semitonesUpdater.get(),
                BooleanUpdater.toBooleanArray(channelUpdater),
                super.marshalInternal(-1, context)
        );
    }

    private transient Map<Integer, Integer> encNoteOnReceivedToTransposedSend;

    public Transposer() {
        octavesUpdater = new IntUpdater<>(this, 0, -11, 11);
        semitonesUpdater = new IntUpdater<>(this, 0, -12, 12);
        channelUpdater = new BooleanUpdater[CHANNEL_COUNT];
        forAllChannels(channel -> {
            channelUpdater[channel] = new BooleanUpdater(this, true);
        });
        encNoteOnReceivedToTransposedSend = new HashMap<>();
    }

    private static int encNote(ShortMessage msg) {
        if (msg.getCommand() != ShortMessage.NOTE_ON && msg.getCommand() != ShortMessage.NOTE_OFF) {
            throw new IllegalArgumentException("msg {" + msg + "} is not a note messsage");
        }
        return msg.getChannel() * NOTE_COUNT + msg.getData1();
    }

    private static int decNoteToNote(int encNote) {
        return encNote % NOTE_COUNT;
    }

    private static int decNoteToChannel(int encNote) {
        int result = encNote / NOTE_COUNT;
        verifyChannel(result);
        return result;
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
            if (incoming.getCommand() == NOTE_ON) {
                ShortMessage transposedOn = transpose(incoming);
                int encIncoming = encNote(incoming);
                if (transposedOn == null) {
                    encNoteOnReceivedToTransposedSend.put(encIncoming, null);
                } else {
                    send(transposedOn, timeStamp);
                    encNoteOnReceivedToTransposedSend.put(encIncoming, transposedOn.getData1());
                }
            } else if (incoming.getCommand() == NOTE_OFF) {
                int encNote = encNote(incoming);
                if (encNoteOnReceivedToTransposedSend.containsKey(encNote)) {
                    Integer prevNoteOn = encNoteOnReceivedToTransposedSend.remove(encNote);
                    ShortMessage noteOff;
                    if (prevNoteOn == null) {
                        noteOff = transpose(incoming);
                    } else {
                        noteOff = createShortMessage(incoming.getCommand(), incoming.getChannel(), prevNoteOn, incoming.getData2());
                    }
                    if (noteOff != null) {
                        send(noteOff, timeStamp);
                    }
                } else {
                    // unmatched note off: simply send transposed
                    ShortMessage noteOff = transpose(incoming);
                    if (noteOff != null) {
                        send(noteOff, timeStamp);
                    }
                }
            } else {
                super.processReceive(message, timeStamp);
            }
        }
    }

    private ShortMessage transpose(ShortMessage msg) {
        if (channelUpdater[msg.getChannel()].get()) {
            int note = msg.getData1();
            int transposedNote = note + 12 * octavesUpdater.get() + semitonesUpdater.get();
            if (validNote(transposedNote)) {
                return createShortMessage(msg.getCommand(), msg.getChannel(), transposedNote, msg.getData1());
            } else {
                return null;
            }
        } else {
            return msg;
        }
    }

    public void setChannel(int channelNummer, boolean transpose) {
        verifyChannel(channelNummer);
        channelUpdater[channelNummer].set(transpose);
    }

    public boolean getChannel(int channelNummer) {
        verifyChannel(channelNummer);
        return channelUpdater[channelNummer].get();
    }

    public BooleanUpdater channelUpdater(int channelNummer) {
        verifyChannel(channelNummer);
        return channelUpdater[channelNummer];
    }

    public void setSemitones(int semitones) {
        this.semitonesUpdater.set(semitones);
    }

    public int getSemitones() {
        return semitonesUpdater.get();
    }

    public IntUpdater semitonesUpdater() {
        return semitonesUpdater;
    }

    public void setOctaves(int octaves) {
        this.octavesUpdater.set(octaves);
    }

    public int getOctaves() {
        return octavesUpdater.get();
    }

    public IntUpdater octavesUpdater() {
        return octavesUpdater;
    }

}
