package nl.nlcode.m.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sound.midi.MidiMessage;
import nl.nlcode.m.linkui.BooleanUpdater;
import nl.nlcode.m.linkui.IntUpdater;
import nl.nlcode.m.linkui.ObjectUpdater;
import nl.nlcode.marshalling.Marshalled;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class MidiMessageDump<U extends MidiMessageDump.Ui> extends MidiInOut<U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String FILE_EXTENTION = ".csv";

    public static record EnhancedMessage(
            byte[] rawSource,
            long timestamp,
            LocalDateTime receivedAt,
            String description) {

        private static EnhancedMessage[] EMPTY_ARRAY = new EnhancedMessage[]{};
    }

    public static interface Ui extends MidiInOut.Ui {

        void received(EnhancedMessage message);

    }

    private static String[] CSV_HEADERS = {
        "dateTime",
        "timestamp",
        "bytesHex",
        "bytesDec",
        "description"
    };

    private static CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader(CSV_HEADERS)
            .build();

    private final List<EnhancedMessage> enhancedMessageList = Collections.synchronizedList(new ArrayList<>());

    private final ObjectUpdater<ShowTicks, U, MidiMessageDump<U>> showTicks;

    private final BooleanUpdater<U, MidiMessageDump<U>> writeToFile;

    private final IntUpdater<U, MidiMessageDump<U>> maxMessagesInFile;

    private transient int tickCount = 0;

    private transient int messagesInCurrentFile;

    public static record SaveData0(
            int id,
            ShowTicks showTicks,
            EnhancedMessage[] enhancedMessageList,
            boolean writeToFile,
            int maxMessagesInFile,
            Marshalled<MidiInOut> s) implements Marshalled<MidiMessageDump> {

        @Override
        public void unmarshalInto(Marshalled.Context context, MidiMessageDump target) {
            target.setShowTicks(showTicks());
            target.enhancedMessageList.addAll(Arrays.asList(enhancedMessageList()));
            target.writeToFile.set(writeToFile());
            target.maxMessagesInFile.set(maxMessagesInFile());
            s.unmarshalInto(context, target);
        }

        @Override
        public MidiMessageDump createMarshallable() {
            return new MidiMessageDump();
        }

    }

    private transient LocalDateTime dumpStartTime;
    private transient int fileCounter;
    private transient CSVPrinter csvPrinter;

    @Override
    public Marshalled marshalInternal(int id, Context context) {
        return new MidiMessageDump.SaveData0(
                id,
                showTicks.get(),
                enhancedMessageList.toArray(EnhancedMessage.EMPTY_ARRAY),
                writeToFile.get(),
                maxMessagesInFile.get(),
                super.marshalInternal(-1, context)
        );
    }

    public MidiMessageDump() {
        showTicks = new ObjectUpdater(this, ShowTicks.ALL);
        maxMessagesInFile = new IntUpdater<>(this, 1000, 1, Integer.MAX_VALUE);
        writeToFile = new BooleanUpdater<>(this);
        writeToFile.addListener((oldValue, newValue) -> {
            stopWriting();
        });
    }

    private synchronized void stopWriting() {
        if (csvPrinter != null) {
            try {
                csvPrinter.close(true);
            } catch (IOException e) {
                LOGGER.error("could not close file <" + getDumpPath(fileCounter).toFile() + ">", e);
            }
            csvPrinter = null;
            dumpStartTime = null;
        }
    }

    @Override
    protected void processReceive(MidiMessage message, long timeStamp) {
        boolean addToList = false;
        if (isTimingClock(message)) {
            switch (getShowTicks()) {
                case NONE:
                    break;
                case FIRST:
                    if (tickCount == 0) {
                        tickCount++;
                        addToList = true;
                    }
                    break;
                case EVERY24:
                    if (++tickCount >= 24) {
                        tickCount = 0;
                        addToList = true;
                    }
                    break;
                case EVERY384:
                    if (++tickCount >= 384) {
                        tickCount = 0;
                        addToList = true;
                    }
                    break;
                case ALL:
                    addToList = true;
            }
        } else {
            addToList = true;
        }
        if (addToList) {
            addToList(message, timeStamp);
        }
    }

    private synchronized void addToList(MidiMessage source, long timeStamp) {
        EnhancedMessage message = new EnhancedMessage(source.getMessage(), timeStamp, LocalDateTime.now(), MIDI_FORMAT.format(source));
        enhancedMessageList.add(0, message);
        uiUpdate(ui -> ui.received(message));
        final int MAX_SIZE = 100;
        while (enhancedMessageList.size() > MAX_SIZE) {
            enhancedMessageList.remove(enhancedMessageList.size() - 1);
        }
        writeToFileIfNeeded(source, message);
    }

    private void writeToFileIfNeeded(MidiMessage source, EnhancedMessage enhancedMessage) {
        if (writeToFile.get()) {
            if (dumpStartTime == null) {
                dumpStartTime = enhancedMessage.receivedAt();
            }
            try {
                if (csvPrinter == null) {
                    fileCounter = 0;
                } else if (messagesInCurrentFile >= maxMessagesInFile.get()) {
                    csvPrinter.close(true);
                    csvPrinter = null;
                    fileCounter++;
                    if (fileCounter >= 10_000) {
                        dumpStartTime = enhancedMessage.receivedAt();
                        fileCounter = 0;
                    }
                }
                if (csvPrinter == null) {
                    messagesInCurrentFile = 0;
                    File dumpFile = getDumpPath(fileCounter).toFile();
                    PrintWriter writer = new PrintWriter(new FileWriter(dumpFile, Charset.forName("UTF-8")));
                    csvPrinter = new CSVPrinter(writer, CSV_FORMAT);
                }
                csvPrinter.printRecord(
                        enhancedMessage.receivedAt,
                        enhancedMessage.timestamp,
                        toHex(enhancedMessage.rawSource, 3),
                        toDec(enhancedMessage.rawSource, 3),
                        enhancedMessage.description
                );
                messagesInCurrentFile++;
            } catch (IOException e) {
                csvPrinter = null;
                writeToFile.set(false);
                File dumpFile = getDumpPath(fileCounter).toFile();
                throw new FunctionalException("cannot write to <" + dumpFile + ">, output stopped", e);
            }
        }
    }

    private Path getDumpPath(int sequence) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        DecimalFormat sequenceFormat = new DecimalFormat("0000");

        String extName = "_" + getName() + "_" + dateFormat.format(dumpStartTime) + "_" + sequenceFormat.format(sequence) + FILE_EXTENTION;
        return getProject().getProjectExtPath(extName);
    }

    public List<EnhancedMessage> getEnhancedMessageList() {
        return enhancedMessageList;
    }

    @Override
    public boolean isActiveReceiver() {
        return true;
    }

    public ShowTicks getShowTicks() {
        return showTicks.get();
    }

    public void setShowTicks(ShowTicks showTicks) {
        tickCount = 0;
        this.showTicks.set(showTicks);
    }

    public IntUpdater<U, MidiMessageDump<U>> maxMessagesInFileUpdater() {
        return maxMessagesInFile;
    }

    public BooleanUpdater<U, MidiMessageDump<U>> writeToFileUpdater() {
        return writeToFile;
    }

    @Override
    public void close() {
        stopWriting();
        super.close();
    }

}
