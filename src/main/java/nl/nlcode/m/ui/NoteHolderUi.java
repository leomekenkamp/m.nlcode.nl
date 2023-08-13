package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import static nl.nlcode.m.engine.MidiInOut.CHANNEL_COUNT;
import static nl.nlcode.m.engine.MidiInOut.forAllChannels;
import nl.nlcode.m.engine.NoteHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class NoteHolderUi extends MidiInOutUi<NoteHolder> implements NoteHolder.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private IntegerProperty[] notesHeldCountProperty;

    @FXML
    private Field channel0Field, channel1Field, channel2Field, channel3Field,
            channel4Field, channel5Field, channel6Field, channel7Field,
            channel8Field, channel9Field, channel10Field, channel11Field,
            channel12Field, channel13Field, channel14Field, channel15Field;

    @FXML
    private CheckBox newNoteOnBeforeOldNoteOff;
    private BooleanUpdatePropertyBridge newNoteOnBeforeOldNoteOffBackend;

    public NoteHolderUi(ProjectUi projectUi, NoteHolder noteHolder, MenuItem menuItem) {
        super(projectUi, noteHolder, menuItem);
        notesHeldCountProperty = new IntegerProperty[CHANNEL_COUNT];
        for (int i = 0; i < CHANNEL_COUNT; i++) {
            notesHeldCountProperty[i] = new SimpleIntegerProperty(0);
        }
        loadFxml(NoteHolderUi.class, App.MESSAGES);
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        Field[] channelFields = new Field[]{
            channel0Field, channel1Field, channel2Field, channel3Field,
            channel4Field, channel5Field, channel6Field, channel7Field,
            channel8Field, channel9Field, channel10Field, channel11Field,
            channel12Field, channel13Field, channel14Field, channel15Field
        };
        forAllChannels(channel -> channelFields[channel].labelTextProperty().bind(getProjectUi().channelTextProperty()[channel]));
        newNoteOnBeforeOldNoteOffBackend = BooleanUpdatePropertyBridge.create(getMidiInOut().newNoteOnBeforeOldNoteOffProperty(), newNoteOnBeforeOldNoteOff.selectedProperty());

    }

    @Override
    public void notesHeldChanged(int channel, int nrOfNotes) {
        LOGGER.info("ch: {}; notes: {}", channel, nrOfNotes);
        Platform.runLater(() -> {
            notesHeldCountProperty[channel].set(nrOfNotes);
        });
    }

    public IntegerProperty notesHeldCount1Property() {
        return notesHeldCountProperty[0];
    }

    public int getNotesHeldCount1() {
        return notesHeldCountProperty[0].get();
    }

    public IntegerProperty notesHeldCount2Property() {
        return notesHeldCountProperty[1];
    }

    public int getNotesHeldCount2() {
        return notesHeldCountProperty[1].get();
    }

    public IntegerProperty notesHeldCount3Property() {
        return notesHeldCountProperty[2];
    }

    public int getNotesHeldCount3() {
        return notesHeldCountProperty[2].get();
    }

    public IntegerProperty notesHeldCount4Property() {
        return notesHeldCountProperty[3];
    }

    public int getNotesHeldCount4() {
        return notesHeldCountProperty[3].get();
    }

    public IntegerProperty notesHeldCount5Property() {
        return notesHeldCountProperty[4];
    }

    public int getNotesHeldCount5() {
        return notesHeldCountProperty[4].get();
    }

    public IntegerProperty notesHeldCount6Property() {
        return notesHeldCountProperty[5];
    }

    public int getNotesHeldCount6() {
        return notesHeldCountProperty[5].get();
    }

    public IntegerProperty notesHeldCount7Property() {
        return notesHeldCountProperty[6];
    }

    public int getNotesHeldCount7() {
        return notesHeldCountProperty[6].get();
    }

    public IntegerProperty notesHeldCount8Property() {
        return notesHeldCountProperty[7];
    }

    public int getNotesHeldCount8() {
        return notesHeldCountProperty[7].get();
    }

    public IntegerProperty notesHeldCount9Property() {
        return notesHeldCountProperty[8];
    }

    public int getNotesHeldCount9() {
        return notesHeldCountProperty[8].get();
    }

    public IntegerProperty notesHeldCount10Property() {
        return notesHeldCountProperty[9];
    }

    public int getNotesHeldCount10() {
        return notesHeldCountProperty[9].get();
    }

    public IntegerProperty notesHeldCount11Property() {
        return notesHeldCountProperty[10];
    }

    public int getNotesHeldCount11() {
        return notesHeldCountProperty[10].get();
    }

    public IntegerProperty notesHeldCount12Property() {
        return notesHeldCountProperty[11];
    }

    public int getNotesHeldCount12() {
        return notesHeldCountProperty[11].get();
    }

    public IntegerProperty notesHeldCount13Property() {
        return notesHeldCountProperty[12];
    }

    public int getNotesHeldCount13() {
        return notesHeldCountProperty[12].get();
    }

    public IntegerProperty notesHeldCount14Property() {
        return notesHeldCountProperty[13];
    }

    public int getNotesHeldCount14() {
        return notesHeldCountProperty[13].get();
    }

    public IntegerProperty notesHeldCount15Property() {
        return notesHeldCountProperty[14];
    }

    public int getNotesHeldCount15() {
        return notesHeldCountProperty[14].get();
    }

    public IntegerProperty notesHeldCount16Property() {
        return notesHeldCountProperty[15];
    }

    public int getNotesHeldCount16() {
        return notesHeldCountProperty[15].get();
    }

}
