package nl.nlcode.m.engine;

import nl.nlcode.m.linkui.IntUpdater;
import nl.nlcode.m.linkui.BooleanUpdater;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import static nl.nlcode.m.engine.MidiInOut.forAllChannels;
import nl.nlcode.marshalling.Marshalled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class ProgramChanger<U extends ProgramChanger.Ui> extends MidiInOut<U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final int PROGRAM_NONE = NONE_FOR_7_BIT_INT;

    public static interface Ui extends MidiInOut.Ui {

        void updateProgram(int channel, int program);

        void updateDropIncomingChanges(int channel, boolean drop);

        void updateResendOnConnect(boolean newValue);

        void updateResendOnMidiDeviceChange(boolean newValue);

    }

    private final IntUpdater<U, ProgramChanger<U>>[] program;

    private final BooleanUpdater<U, ProgramChanger<U>>[] dropIncomingChanges;

    private final BooleanUpdater<U, ProgramChanger<U>> resendOnMidiDeviceChange;

    private final BooleanUpdater<U, ProgramChanger<U>> resendOnConnect;

    public static record SaveData0(
            int id,
            boolean resendOnConnect,
            boolean resendOnMidiDeviceChange,
            int[] program,
            boolean[] dropIncomingChanges,
            Marshalled<MidiInOut> s) implements Marshalled<ProgramChanger> {

        @Override
        public void unmarshalInto(Context context, ProgramChanger target) {
            target.setResendOnConnect(resendOnConnect());
            target.setResendOnMidiDeviceChange(resendOnMidiDeviceChange());
            forAllChannels(channel -> target.setProgram(channel, program[channel]));
            forAllChannels(channel -> target.setDropIncomingChanges(channel, dropIncomingChanges()[channel]));
            s.unmarshalInto(context, target);
        }

        @Override
        public ProgramChanger createMarshallable() {
            return new ProgramChanger();
        }

    }

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new SaveData0(
                id,
                getResendOnConnect(),
                getResendOnMidiDeviceChange(),
                IntUpdater.toIntArray(program),
                BooleanUpdater.toBooleanArray(dropIncomingChanges),
                super.marshalInternal(-1, context)
        );
    }

    public ProgramChanger() {

        resendOnMidiDeviceChange = new BooleanUpdater(this, true);

        resendOnConnect = new BooleanUpdater(this, true);

        program = new IntUpdater[CHANNEL_COUNT];
        forAllChannels(channel -> {
            program[channel] = new IntUpdater(this, PROGRAM_NONE, MIDI_DATA_NONE, MIDI_DATA_MAX);
            program[channel].setAfterChange(this, ui -> {
                ui.updateProgram(channel, getProgram(channel));
            });
        });

        dropIncomingChanges = new BooleanUpdater[CHANNEL_COUNT];
        forAllChannels(channel -> {
            dropIncomingChanges[channel] = new BooleanUpdater(this, false);
            dropIncomingChanges[channel].setAfterChange(this, ui -> ui.updateDropIncomingChanges(channel, getDropIncomingChanges(channel)));
        });

        resendOnMidiDeviceChange.setAfterChange(this, ui -> ui.updateResendOnMidiDeviceChange(getResendOnMidiDeviceChange()));

        resendOnConnect.setAfterChange(this, ui -> ui.updateResendOnConnect(getResendOnConnect()));
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
    protected void processReceive(MidiMessage message, long timestamp) {
        if (message instanceof ShortMessage shortMessage) {
            switch (shortMessage.getCommand()) {
                case ShortMessage.PROGRAM_CHANGE -> {
                    int channel = shortMessage.getChannel();
                    int program = shortMessage.getData1();
                    if (dropIncomingChanges[channel].get()) {
                        LOGGER.debug("dropping incoming change <{}> for channel <{}>", program, channel);
                    } else {
                        send(message, timestamp);
                        setProgram(channel, program);
                    }
                }
                default -> {
                    LOGGER.debug("short msgs not interesting for me, relaying to super");
                    super.processReceive(message, timestamp);
                }
            }
        } else {
            LOGGER.debug("not interesting for me, relaying to super");
            super.processReceive(message, timestamp);
        }
    }

    public void resend() {
        forAllChannels(channel -> resend(channel));
    }

    public void resend(int channel) {
        verifyChannel(channel);
        if (program != null) {
            int program = getProgram(channel);
            if (program != PROGRAM_NONE) {
                programChange(channel, program);
                uiUpdate(ui -> ui.updateProgram(channel, program));
            }
        }
    }

    @Override
    public void startSendingTo(MidiInOut receiver) {
        super.startSendingTo(receiver);
        if (getResendOnConnect()) {
            resend();
        }
    }

    @Override
    public void fromReceiver(Event event) {
        if (getResendOnMidiDeviceChange() && event instanceof MidiDeviceLink.MidiDeviceChanged midiDeviceChanged) {
            if (midiDeviceChanged != null) {
                resend();
            }
        }
    }

    private void programChange(int channel, int prog) {
        try {
            if (prog != PROGRAM_NONE) {
                ShortMessage programChange = new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, prog, 0);
                send(programChange);
            }
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }

    public int getProgram(int channel) {
        verifyChannel(channel);
        return program[channel].get();
    }

    public void setProgram(int channel, int prog) {
        verifyChannel(channel);
        MidiInOut.verify7BitPlusNone(prog);
        if (getProgram(channel) != prog) {
            programChange(channel, prog);
        }
        program[channel].set(prog);
    }

    public boolean getDropIncomingChanges(int channel) {
        verifyChannel(channel);
        return dropIncomingChanges[channel].get();
    }

    public void setDropIncomingChanges(int channel, boolean drop) {
        verifyChannel(channel);
        dropIncomingChanges[channel].set(drop);
    }

    public boolean getResendOnMidiDeviceChange() {
        return resendOnMidiDeviceChange.get();
    }

    public void setResendOnMidiDeviceChange(boolean resend) {
        resendOnMidiDeviceChange.set(resend);
    }

    public boolean getResendOnConnect() {
        return resendOnConnect.get();
    }

    public void setResendOnConnect(boolean resend) {
        resendOnConnect.set(resend);
    }
}
