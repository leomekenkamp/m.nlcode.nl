package nl.nlcode.m.ui;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.JvmStuff;
import nl.nlcode.m.engine.A42;
import nl.nlcode.m.engine.Arpeggiator;
import nl.nlcode.m.engine.MidiInOut;
import static nl.nlcode.m.engine.Control.FILE_EXTENTION_FILTER;
import nl.nlcode.m.engine.KeyboardKeyboard;
import nl.nlcode.m.engine.ChannelMatrix;
import nl.nlcode.m.engine.Echo;
import nl.nlcode.m.engine.MidiClock;
import nl.nlcode.m.engine.MidiDeviceLink;
import nl.nlcode.m.engine.LayerAndSplit;
import nl.nlcode.m.engine.Lights;
import nl.nlcode.m.engine.MessageTypeFilter;
import static nl.nlcode.m.engine.MidiInOut.CHANNEL_COUNT;
import static nl.nlcode.m.engine.MidiInOut.forAllChannels;
import nl.nlcode.m.engine.MidiMessageDump;
import nl.nlcode.m.engine.Sequencer;
import nl.nlcode.m.engine.NoteGateVelocity;
import nl.nlcode.m.engine.NoteHolder;
import nl.nlcode.m.engine.NoteChannelSpreader;
import nl.nlcode.m.engine.ProgramChanger;
import nl.nlcode.m.engine.Project;
import static nl.nlcode.m.ui.ControlUi.ALL_FILTER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public final class ProjectUi extends BorderPane implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final FileChooser.ExtensionFilter M_FILTER
            = new FileChooser.ExtensionFilter(App.MESSAGES.getString("m.nlcode.nl.files"), FILE_EXTENTION_FILTER);

    private static final String FOCUS = "focus";

    private static final String CONNECTED_SENDER = "connectedSender";
    private static final String CONNECTED_RECEIVER = "connectedReceiver";

    private static final String UI_CLASS_PREFIX = ProjectUi.class.getPackage().getName() + ".";

    @FXML
    private Menu windowMenu;

    private ObservableList<MidiInOutUi<?>> midiInOutUiList = FXCollections.observableArrayList(MidiInOutUi.NAME_EXTRACTOR);

    private ObservableList<MidiInOutUi<?>> midiInOutUiListReadonly = midiInOutUiList.sorted(MidiInOutUi.BY_NAME);

    private ObservableList<MidiInOutUi<?>> activeSenders = FXCollections.observableArrayList(MidiInOutUi.NAME_EXTRACTOR);

    private ObservableList<MidiInOutUi<?>> activeSendersReadonly = activeSenders.sorted(MidiInOutUi.BY_NAME);

    private ObservableList<MidiInOutUi<?>> activeReceivers = FXCollections.observableArrayList(MidiInOutUi.NAME_EXTRACTOR);

    private ObservableList<MidiInOutUi<?>> activeReceiversReadonly = activeReceivers.sorted(MidiInOutUi.BY_NAME);

    private Project project;

    private ControlUi controlUi;

    private ObjectProperty<Path> pathProperty = new SimpleObjectProperty<>();

    private BooleanProperty dirtyProperty = new SimpleBooleanProperty();

    private StringProperty logoProperty = new SimpleStringProperty();

    private ReadOnlyStringWrapper namePropertyWrapper = new ReadOnlyStringWrapper();

    private MenuItem menuItem;

    private StringProperty[] channelTextProperty = new StringProperty[CHANNEL_COUNT];

    private ChangeListener<Integer> midiChannelStringRepresentationChanged = (ov, oldValue, newValue) -> {
        updateChannelTextProperties();
    };

    private ChangeListener<Boolean> midiInOutUiSenderChange = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
            Platform.runLater(() -> {
                ReadOnlyBooleanProperty senderProperty = (ReadOnlyBooleanProperty) ov;
                MidiInOutUi midiInOutUi = (MidiInOutUi) senderProperty.getBean();
                if (oldValue.booleanValue()) {
                    activeSenders.remove(midiInOutUi);
                }
                if (newValue.booleanValue()) {
                    activeSenders.add(midiInOutUi);
                }
            });
        }
    };

    private ChangeListener<Boolean> midiInOutUiReceiverChange = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
            Platform.runLater(() -> {
                ReadOnlyBooleanProperty receiverProperty = (ReadOnlyBooleanProperty) ov;
                MidiInOutUi midiInOutUi = (MidiInOutUi) receiverProperty.getBean();
                if (oldValue.booleanValue()) {
                    activeReceivers.remove(midiInOutUi);
                }
                if (newValue.booleanValue()) {
                    activeReceivers.add(midiInOutUi);
                }
            });
        }
    };

    public ProjectUi(ControlUi controlUi, Project project, MenuItem menuItem) {
        loadFxml(App.MESSAGES);
        this.project = project;
        this.controlUi = controlUi;
        this.menuItem = menuItem;

        pathProperty.set(project.getPath());

        menuItem.textProperty().bindBidirectional(pathProperty(), new StringConverter<Path>() {
            @Override
            public String toString(Path path) {
                return path.toString();
            }

            @Override
            public Path fromString(String string) {
                return Path.of(string);
            }
        });

        windowMenu.setDisable(true);
        windowMenu.getItems().addListener(new ListChangeListener() {
            public void onChanged(ListChangeListener.Change c) {
                windowMenu.setDisable(c.getList().size() <= 3);
            }
        });

        midiInOutUiList.addListener(midiInOutListChange);

        logoProperty.bind(Bindings.createStringBinding(
                () -> pathProperty.get() + " " + (dirtyProperty.get() ? '\u0394' : '\u2261'), dirtyProperty, pathProperty)
        );

        namePropertyWrapper.bind(Bindings.createStringBinding(
                () -> pathProperty().get().getFileName().toString(), pathProperty())
        );

        forAllChannels(channel -> channelTextProperty[channel] = new SimpleStringProperty());
        updateChannelTextProperties();
        getControlUi().getMidiChannelStringConverter().offsetProperty().addListener(new WeakChangeListener(midiChannelStringRepresentationChanged));
    }

    public StringProperty[] channelTextProperty() {
        return channelTextProperty;
    }

    private void updateChannelTextProperties() {
        MessageFormat messageFormat = new MessageFormat(App.MESSAGES.getString("channel%"));
        int offset = getControlUi().getMidiChannelStringConverter().offsetProperty().get();
        forAllChannels(channel -> channelTextProperty[channel].set(messageFormat.format(new Object[]{channel + offset})));
    }

    @FXML
    public void createMessageTypeFilter(ActionEvent event) {
        activateAndCreateStage(new MessageTypeFilter());
    }

    @FXML
    public void createMidiDeviceLink(ActionEvent event) {
        activateAndCreateStage(new MidiDeviceLink());
    }

    @FXML
    public void createProgramChanger(ActionEvent event) {
        activateAndCreateStage(new ProgramChanger());
    }

    @FXML
    public void createA42(ActionEvent event) {
        activateAndCreateStage(new A42());
    }

    @FXML
    public void createArpeggiator(ActionEvent event) {
        activateAndCreateStage(new Arpeggiator());
        setDirty();
    }

    @FXML
    public void createKeyboardKeyboard(ActionEvent event) {
        activateAndCreateStage(new KeyboardKeyboard());
        setDirty();
    }

    @FXML
    public void createMidiMessageDump(ActionEvent event) {
        Stage stage = activateAndCreateStage(new MidiMessageDump());
        stage.setResizable(true);
    }

    @FXML
    public void createChannelMatrix(ActionEvent event) {
        activateAndCreateStage(new ChannelMatrix());
    }

    @FXML
    public void createLayerAndSplit(ActionEvent event) {
        Stage stage = activateAndCreateStage(LayerAndSplit.createWithDefaultSettings());
        stage.setResizable(true);
    }

    @FXML
    public void createLights(ActionEvent event) {
        activateAndCreateStage(new Lights());
    }

    @FXML
    public void createSequencer(ActionEvent event) {
        activateAndCreateStage(new Sequencer());
    }

    @FXML
    public void createMidiClock(ActionEvent event) {
        activateAndCreateStage(new MidiClock());
    }

    @FXML
    public void createNoteGate(ActionEvent event) {
        activateAndCreateStage(new NoteGateVelocity());
    }

    @FXML
    public void createNoteHolder(ActionEvent event) {
        activateAndCreateStage(new NoteHolder());
    }

    @FXML
    public void createEcho(ActionEvent event) {
        activateAndCreateStage(new Echo());
    }

    @FXML
    public void createNoteChannelSpreader(ActionEvent event) {
        activateAndCreateStage(new NoteChannelSpreader());
    }

    private ListChangeListenerHelper<MidiInOutUi> midiInOutListChangeHelper = new ListChangeListenerHelper<>() {
        @Override
        public void removed(MidiInOutUi removed) {
            LOGGER.debug("removed from general list: <{}>", removed);
            removed.activeReceiverProperty().removeListener(midiInOutUiReceiverChange);
            removed.activeSenderProperty().removeListener(midiInOutUiSenderChange);
            activeReceivers.remove(removed); // could call contains() first
            activeSenders.remove(removed); // could call contains() first
        }

        @Override
        public void added(MidiInOutUi removed) {
            LOGGER.debug("added to general list: <{}>", removed);
            removed.activeReceiverProperty().addListener(midiInOutUiReceiverChange);
            removed.activeSenderProperty().addListener(midiInOutUiSenderChange);
            if (removed.getMidiInOut().isActiveReceiver()) {
                activeReceivers.add(removed);
            }
            if (removed.getMidiInOut().isActiveSender()) {
                activeSenders.add(removed);
            }
        }
    };

    private ListChangeListener<MidiInOutUi> midiInOutListChange = (change) -> midiInOutListChangeHelper.process(change);

    public void createMidiInOutUisFromProjectMidiInOuts() {
        for (MidiInOut midiInOut : getProject().getMidiInOutList()) {
            createStage(midiInOut);
        }
        Map<MidiInOut, MidiInOutUi> inOutToUi = new HashMap<>();
        for (MidiInOutUi midiInOutUi : midiInOutUiList) {
            inOutToUi.put(midiInOutUi.getMidiInOut(), midiInOutUi);
        }
        LOGGER.debug("iterating over <{}> midiInOut instances", midiInOutUiList.size());
        for (MidiInOut midiInOut : getProject().getMidiInOutList()) {
            LOGGER.debug("processing instance <{}>", midiInOut);
            MidiInOutUi midiInOutUi = inOutToUi.get(midiInOut);
            LOGGER.debug("iterating over <{}> receivers", midiInOut.sendingTo().size());
            for (MidiInOut receiver : (Set<MidiInOut>) midiInOut.sendingTo()) {
                MidiInOutUi receiverUi = inOutToUi.get(receiver);
                LOGGER.debug("adding <{}> to input list of <{}>", midiInOutUi, receiverUi);
                LOGGER.debug("selectable inputs: <{}>", receiverUi.getInputListView().getItems());
                LOGGER.debug("input pre: <{}>", receiverUi.getInputListView().getChecked());
                receiverUi.getInputListView().setChecked(midiInOutUi, true);
            }
        }
    }

    public void setDirty() {
        project.setDirty();
        dirtyProperty.set(true);
    }

    private void dirtyReset() {
        dirtyProperty.set(false);
    }

    public boolean isDirty() {
        return dirtyProperty.get();
    }

    public ObservableList<MidiInOutUi<?>> getMidiInOutUiList() {
        return midiInOutUiListReadonly;
    }

    public void move(Path path) {
        ((Stage) getScene().getWindow()).setTitle(path.toString());
        pathProperty.set(path);
        setDirty();
        getScene().getWindow().sizeToScene();
    }

    private Stage activateAndCreateStage(MidiInOut midiInOut) {
        midiInOut.openWith(getProject());
        Stage result = createStage(midiInOut);
        setDirty();
        return result;

    }

    private ListChangeListener<MidiInOutUi> inputListChangeWhileInFocus = (change) -> {
        while (change.next()) {
            if (change.wasPermutated()) {
            } else if (change.wasUpdated()) {
            } else if (change.wasReplaced()) {
            } else {
                for (MidiInOutUi removed : change.getRemoved()) {
                    removed.getStyleClass().remove(CONNECTED_SENDER);
                }
                for (MidiInOutUi added : change.getAddedSubList()) {
                    added.getStyleClass().add(CONNECTED_SENDER);
                }
            }
        }
    };

    private ListChangeListener<MidiInOutUi> outputListChangeWhileInFocus = (change) -> {
        while (change.next()) {
            if (change.wasPermutated()) {
            } else if (change.wasUpdated()) {
            } else if (change.wasReplaced()) {
            } else {
                for (MidiInOutUi removed : change.getRemoved()) {
                    removed.getStyleClass().remove(CONNECTED_RECEIVER);
                }
                for (MidiInOutUi added : change.getAddedSubList()) {
                    added.getStyleClass().add(CONNECTED_RECEIVER);
                }
            }
        }
    };

    private Stage createStage(MidiInOut midiInOut) {
        LOGGER.debug("creating for <{}>", midiInOut);
        try {
            Class<MidiInOutUi> midiInOutUiClass = (Class<MidiInOutUi>) Class.forName(UI_CLASS_PREFIX + midiInOut.getClass().getSimpleName() + "Ui");
            Constructor<MidiInOutUi> ctor = midiInOutUiClass.getConstructor(getClass(), midiInOut.getClass(), MenuItem.class);
            MenuItem menuItem = new MenuItem();
            windowMenu.getItems().add(menuItem);
            MidiInOutUi midiInOutUi = ctor.newInstance(this, midiInOut, menuItem);
            midiInOut.setUi(midiInOutUi); // TODO: MOVE TO FACTORY METHOD

            Stage result = App.createStage(midiInOutUi);
            result.titleProperty().bind(midiInOutUi.nameProperty());
            midiInOutUiList.add(midiInOutUi);
            midiInOutUi.restoreWindowPositionAndSetAutosave();
            result.focusedProperty().addListener((ov, oldValue, newValue) -> {
                if (newValue) {
                    midiInOutUi.getStyleClass().add(FOCUS);
                    addStyleClass(midiInOutUi.getInputListView(), CONNECTED_SENDER);
                    addStyleClass(midiInOutUi.getOutputListView(), CONNECTED_RECEIVER);
                    midiInOutUi.getInputListView().getChecked().addListener(inputListChangeWhileInFocus);
                    midiInOutUi.getOutputListView().getChecked().addListener(outputListChangeWhileInFocus);
                } else {
                    midiInOutUi.getStyleClass().remove(FOCUS);
                    removeStyleClass(midiInOutUi.getInputListView(), CONNECTED_SENDER);
                    removeStyleClass(midiInOutUi.getOutputListView(), CONNECTED_RECEIVER);
                    midiInOutUi.getInputListView().getChecked().removeListener(inputListChangeWhileInFocus);
                    midiInOutUi.getOutputListView().getChecked().removeListener(outputListChangeWhileInFocus);
                }
                midiInOutUi.sizeToScene();
            });
            result.show();

            menuItem.setOnAction(action -> {
                result.show();
                result.toFront();
            });
            menuItem.textProperty().bind(midiInOutUi.nameProperty());
            menuItem.textProperty().addListener((ov, oldValue, newValue) -> {
                LOGGER.debug("from {} to {}", oldValue, newValue);
                sortMenuItems();
            });
            sortMenuItems();

            result.setOnCloseRequest(onCloseRequest(midiInOutUi));

            return result;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private void sortMenuItems() {
        LOGGER.debug("sorting menu");
        SortedSet<MenuItem> sortables = new TreeSet<>(new Comparator<MenuItem>() {
            @Override
            public int compare(MenuItem o1, MenuItem o2) {
                return o1.getText().compareToIgnoreCase(o2.getText());
            }
        });
        Iterator<MenuItem> i = windowMenu.getItems().iterator();
        while (!i.next().getClass().equals(SeparatorMenuItem.class)) {
            // find separator; below are the MidiInOutUi name menu items
        }
        while (i.hasNext()) {
            sortables.add(i.next());
            i.remove();
        }
        for (MenuItem menuItem : sortables) {
            // bug in javafx: addAll() does not set the parentMenu for the menuItem, so we use add()
            windowMenu.getItems().add(menuItem);
        }
    }

    private void addStyleClass(MidiInOutUiListView listView, String styleClass) {
        for (MidiInOutUi other : listView.getChecked()) {
            if (other != null) { // don't ask me how, but it happens
                other.getStyleClass().add(styleClass);
                other.sizeToScene();
            }
        }
    }

    private void removeStyleClass(MidiInOutUiListView listView, String styleClass) {
        for (MidiInOutUi other : listView.getChecked()) {
            if (other != null) { // don't ask me how, but it happens
                other.getStyleClass().remove(styleClass);
                other.sizeToScene();
            }
        }
    }

    private EventHandler<WindowEvent> onCloseRequest(MidiInOutUi midiInOutUi) {
        return event -> {
            midiInOutUi.closeWindow();
            event.consume();
        };
    }

    public void remove(MidiInOutUi midiInOutUi) {
        midiInOutUiList.remove(midiInOutUi);
        setDirty();
    }

    public ControlUi getControlUi() {
        return controlUi;
    }

    public Project getProject() {
        return project;
    }

    @FXML
    private void save() throws IOException {
        if (getPath() == null) {
            saveAs();
        } else {
            beforeSave();
            getProject().save();
            dirtyReset();
        }
    }

    @FXML
    private void saveAs() throws IOException {
        FileChooser fileChooser = projectChooser("openProject");
        fileChooser.getExtensionFilters().addAll(M_FILTER, ALL_FILTER);
        fileChooser.setSelectedExtensionFilter(M_FILTER);
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            Path path = file.toPath();
            beforeSave();
            getProject().saveAs(path);
            pathProperty.set(path);
            dirtyReset();
        }
    }

    static FileChooser projectChooser(String titleKey, File initialDirectory) {
        FileChooser result = new FileChooser();
        result.setTitle(App.MESSAGES.getString(titleKey));
        result.setInitialDirectory(initialDirectory);
        result.getExtensionFilters().clear();
        return result;
    }

    FileChooser projectChooser(String titleKey) {
        FileChooser result = projectChooser(titleKey, getProject().getPath().getParent().toFile());
        result.setInitialFileName(getProject().getPath().getFileName().toString());
        return result;
    }

    public ObjectProperty<Path> pathProperty() {
        return pathProperty;
    }

    public Path getPath() {
        return pathProperty.get();
    }

    public void setPath(Path path) {
        pathProperty.set(path);
    }

    public String getName() {
        return namePropertyWrapper.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return namePropertyWrapper.getReadOnlyProperty();
    }

    public ObservableList<MidiInOutUi<?>> getActiveSendersReadonly() {
        return activeSendersReadonly;
    }

    public ObservableList<MidiInOutUi<?>> getActiveReceiversReadonly() {
        return activeReceiversReadonly;
    }

    public void restoreWindowPosition() {
        restoreWindowPosition((Stage) getScene().getWindow(), getProject().getInfo());
    }

    public static void restoreWindowPosition(Stage stage, Map<Serializable, Serializable> info) {
        Double x = (Double) info.getOrDefault(App.PREF_X, null);
        if (x != null) {
            stage.setX(x);
            LOGGER.info("init X to {}", x);
        }
        Double y = (Double) info.getOrDefault(App.PREF_Y, null);
        if (y != null) {
            stage.setY(y);
            LOGGER.info("init Y to {}", y);
        }
    }

    protected void beforeSave() {
        double x = getScene().getWindow().getX();
        double y = getScene().getWindow().getY();
        getProject().getInfo().put(App.PREF_X, x);
        getProject().getInfo().put(App.PREF_Y, y);
        LOGGER.info("moved X,Y to {} {}", x, y);
        for (MidiInOutUi midiInOutUi : midiInOutUiList) {
            midiInOutUi.beforeSave();
        }
    }

    protected boolean canCloseWindow() {
        AtomicBoolean result = new AtomicBoolean(false);
        if (isDirty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(App.MESSAGES.getString("closeProject"));
            alert.setContentText(String.format(App.MESSAGES.getString("whatToDo"), getPath()));
            ButtonType saveAndClose = new ButtonType(App.MESSAGES.getString("saveAndClose"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelClose = new ButtonType(App.MESSAGES.getString("neverMind"), ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType deleteAndClose = new ButtonType(App.MESSAGES.getString("deleteAndClose"));
            alert.getButtonTypes().setAll(saveAndClose, cancelClose, deleteAndClose);
            alert.showAndWait().ifPresent(chosen -> {
                if (chosen == saveAndClose) {
                    try {
                        getProject().save();
                        result.set(true);
                    } catch (IOException e) {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle(e.getClass().getName());
                        error.setContentText(e.getMessage());
                    }
                } else if (chosen == deleteAndClose) {
                    result.set(true);
                }
            });
        } else {
            result.set(true);
        }
        return result.get();
    }

    protected void cleanupBeforeCloseWindow() {
        LOGGER.debug("closing project");
        project.close();
        LOGGER.debug("remove project from control ui");
        getControlUi().remove(this);
        LOGGER.debug("remove project menu item");
        menuItem.getParentMenu().getItems().remove(menuItem);
        LOGGER.debug("iterating over open midiInOutUi items");
        while (!midiInOutUiList.isEmpty()) {
            // A closing MidiInOutUi indirectly removes itself from a.a. the midiInOutUiList, so we cannot use an iterator.
            LOGGER.debug("closing window for <{}>", getMidiInOutUiList().get(0));
            midiInOutUiList.get(0).forceCloseWindow();
        }
    }

    @FXML
    public void closeWindow() {
        if (canCloseWindow()) {
            cleanupBeforeCloseWindow();
            LOGGER.debug("closing my window: <{}>", getName());
            ((Stage) getScene().getWindow()).close();
        }
        LOGGER.debug("done");
    }

    public String getLogo() {
        return logoProperty.get();
    }

    public StringProperty logoProperty() {
        return logoProperty;
    }

    @FXML
    private void allMidiInOutToFront() {
        for (MidiInOutUi midiInOutUi : midiInOutUiList) {
            ((Stage) midiInOutUi.getScene().getWindow()).toFront();
        }
    }

    @FXML
    private void minimizeAllMidiInOut() {
        for (MidiInOutUi midiInOutUi : midiInOutUiList) {
            ((Stage) midiInOutUi.getScene().getWindow()).setIconified(true);
        }
    }

    

}
