package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
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
public class ProgramChanger extends MidiInOut<ProgramChanger.Ui> {

    private static final long serialVersionUID = 0L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final int PROGRAM_NONE = NONE_FOR_7_BIT_INT;

    public static interface Ui extends MidiInOut.Ui {

        void updateProgram(int channel, int program);

        void updateDropIncomingChanges(int channel, boolean drop);

        void updateAutoSendOnConnect(boolean newValue);

        void updateAutoSendOnMidiDeviceChange(boolean newValue);

    }

    private volatile int[] program = new int[CHANNEL_COUNT];
    private transient Consumer<U>[] updateProgram;

    private volatile boolean[] dropIncomingChanges = new boolean[CHANNEL_COUNT];
    private transient Consumer<U>[] updateDropIncomingChanges;

    private volatile boolean autoSendOnMidiDeviceChange = true;
    private transient Consumer<U> updateAutoSendOnMidiDeviceChange;

    private BooleanUpdateProperty autoSendOnConnect = new BooleanUpdateProperty();

        public static record SaveData0(
            int id,
            boolean autoSendOnConnect,
            int[] program,
            boolean[] dropIncomingChanges,
            Marshalled<MidiInOut> s) implements Marshalled<ProgramChanger> {

        @Override
        public void unmarshalInternal(Context context, ProgramChanger target) {
            target.autoSendOnConnect.set(autoSendOnConnect());
            target.program = program();
            target.dropIncomingChanges = dropIncomingChanges();
            s.unmarshalInternal(context, target);
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
                autoSendOnConnect.get(),
                program,
                dropIncomingChanges,
                super.marshalInternal(-1, context)
        );
    }


    public ProgramChanger() {
        forAllChannels(channel -> program[channel] = PROGRAM_NONE);
        updateProgram = new Consumer[CHANNEL_COUNT];
        forAllChannels(channel -> {
            updateProgram[channel] = ui -> ui.updateProgram(channel, getProgram(channel));
            addUiUpdate(updateProgram[channel]);

        });

        updateDropIncomingChanges = new Consumer[CHANNEL_COUNT];
        forAllChannels(channel -> {
            updateDropIncomingChanges[channel] = () -> getUi().updateDropIncomingChanges(channel, getDropIncomingChanges(channel));
            addUiUpdate(updateDropIncomingChanges[channel]);
        });

        updateAutoSendOnMidiDeviceChange = () -> getUi().updateAutoSendOnMidiDeviceChange(getAutoSendOnMidiDeviceChange());
        addUiUpdate(updateAutoSendOnMidiDeviceChange);
        
        autoSendOnConnect.setAfterSet(()
                -> getUi().updateAutoSendOnConnect(autoSendOnConnect.get())
        );
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
                    if (dropIncomingChanges[channel]) {
                        LOGGER.debug("dropping incoming change <{}> for channel <{}>", program, channel);
                    } else {
                        this.program[channel] = program;
                        send(message, timestamp);
                        uiUpdate(updateProgram[channel]);
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
        verifyChannelZeroBased(channel);
        if (program != null) {
            int program = this.program[channel];
            if (program != PROGRAM_NONE) {
                programChange(channel, program, true);
                uiUpdate(ui -> ui.updateProgram(channel, program));
            }
        }
    }

    @Override
    public void startSendingTo(MidiInOut receiver) {
        super.startSendingTo(receiver);
        if (autoSendOnConnect().get()) {
            forAllChannels(channel -> resend(channel));
        }
    }

    @Override
    public void fromReceiver(Event event) {
        if (getAutoSendOnMidiDeviceChange() && event instanceof MidiDeviceLink.MidiDeviceChanged midiDeviceChanged) {
            if (midiDeviceChanged != null) {
                resend();
            }
        }
    }


    public int getProgram(int channel) {
        verifyChannelZeroBased(channel);
        return program[channel];
    }

    public void setProgram(int channel, int prog) {
        programChange(channel, prog, false);
    }

    private void programChange(int channel, int prog, boolean forceResend) {
        verifyChannelZeroBased(channel);
        verify7BitPlusNone(prog);
        boolean newValue = this.program[channel] != prog;
        if (forceResend || newValue) {
            try {
                this.program[channel] = prog;
                if (prog != PROGRAM_NONE) {
                    ShortMessage programChange = new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, prog, 0);
                    send(programChange);
                }
                if (newValue) {
                    uiUpdate(updateProgram[channel]);
                }
            } catch (InvalidMidiDataException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public boolean getDropIncomingChanges(int channel) {
        return dropIncomingChanges[channel];
    }

    public void setDropIncomingChanges(int channel, boolean drop) {
        boolean update = dropIncomingChanges[channel] != drop;
        dropIncomingChanges[channel] = drop;
        if (update) {
            uiUpdate(updateDropIncomingChanges[channel]);
        }
    }

    public BooleanUpdateProperty autoSendOnConnect() {
        return autoSendOnConnect;
    }

    public void setAutoSendOnMidiDeviceChange(boolean autoSendOnMidiDeviceChange) {
        boolean update = this.autoSendOnMidiDeviceChange != autoSendOnMidiDeviceChange;
        this.autoSendOnMidiDeviceChange = autoSendOnMidiDeviceChange;
        if (update) {
            uiUpdate(updateAutoSendOnMidiDeviceChange);
        }
    }

    public boolean getAutoSendOnMidiDeviceChange() {
        return autoSendOnMidiDeviceChange;
    }

}
