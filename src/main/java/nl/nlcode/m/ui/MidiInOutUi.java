package nl.nlcode.m.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;
import nl.nlcode.javafxutil.CtorParamControllerFactory;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.FunctionalException;
import nl.nlcode.m.engine.MidiInOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class MidiInOutUi<T extends MidiInOut> extends TabPane implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiInOutUi.class);

    private static final String MIDI_IN_OUT = "midiInOut";

    private static final String ACTIVE_SENDER = "activeSender";

    private static final String ACTIVE_RECEIVER = "activeReceiver";

    @FXML
    private TextField name;

    @FXML
    private MidiInOutUiListView outputListView;

    @FXML
    private MidiInOutUiListView inputListView;

    @FXML
    private Tab outputsTab;

    @FXML
    private Tab inputsTab;

    @FXML
    private Tab instrumentTab;

    private T midiInOut;

    private ProjectUi projectUi;

    private FxmlController.RunOnce runOnce;

    private ReadOnlyBooleanWrapper activeSenderWrapper;

    private ReadOnlyBooleanWrapper activeReceiverWrapper;

    private boolean propagateInputOutputChangesToOthers = true;

    private MenuItem menuItem;

    static Callback<MidiInOutUi<?>, Observable[]> getNameExtractor() {
        return NAME_EXTRACTOR;
    }

    private static final Callback<MidiInOutUi<?>, Observable[]> NAME_EXTRACTOR = new Callback<MidiInOutUi<?>, Observable[]>() {
        @Override
        public Observable[] call(MidiInOutUi midiInOutUi) {
            LOGGER.debug("extractor initialized");
            return new Observable[]{
                midiInOutUi.nameProperty()
            };
        }
    };

    public MidiInOutUi(ProjectUi projectUi, T midiInOut, MenuItem menuItem) {
        this.projectUi = projectUi;
        this.midiInOut = midiInOut;
        this.menuItem = menuItem;
        activeSenderWrapper = new ReadOnlyBooleanWrapper(this, "activeSenderWrapper");
        activeReceiverWrapper = new ReadOnlyBooleanWrapper(this, "activeReceiverWrapper");
        runOnce = new FxmlController.RunOnce(distance(getClass(), MidiInOutUi.class));
        loadFxml(MidiInOutUi.class, new CtorParamControllerFactory(projectUi), App.MESSAGES);
    }

    public final void initialize() {
        runOnce.runAtGivenInvocation(() -> {
            doInit();
        });
    }

    protected void doInit() {
        syncAll();
        inputListView.setAvailableMidiInOutUiList(projectUi.getActiveSendersReadonly());
        outputListView.setAvailableMidiInOutUiList(projectUi.getActiveReceiversReadonly());

        setName(getMidiInOut().getName());
        LOGGER.debug("name from non-gui counterpart: {}", name.getText());
        nameProperty().addListener((ov, oldValue, newValue) -> {
            // FIXME: see if a TextFormatter works better here
            T midiInOut = getMidiInOut();
            if (midiInOut != null && !midiInOut.getName().equals(newValue)) {
                try {
                    getMidiInOut().setName(newValue);
                    LOGGER.debug("name changed to {}", name.getText());
                    getProjectUi().setDirty();
                } catch (FunctionalException e) {
                    name.textProperty().set(oldValue);
                }
            }
        });

        inputsTab.disableProperty().bind(activeReceiverProperty().not());
        outputsTab.disableProperty().bind(activeSenderProperty().not());

        inputListView.setOwner(this);
        outputListView.setOwner(this);
        LOGGER.debug("adding change listeners to input and output selection lists");
        inputListView.getSelectionModel().getSelectedItems().addListener(inputSelectionChangeListener());
        outputListView.getSelectionModel().getSelectedItems().addListener(outputSelectionChangeListener());

        activeSenderWrapper.addListener((ov, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
                getStyleClass().add(ACTIVE_SENDER);
            } else {
                getStyleClass().remove(ACTIVE_SENDER);
            }
        });

        activeReceiverWrapper.addListener((ov, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
                getStyleClass().add(ACTIVE_RECEIVER);
            } else {
                getStyleClass().remove(ACTIVE_RECEIVER);
            }
        });

        getStyleClass().add(MIDI_IN_OUT);
    }

    private ListChangeListener<MidiInOutUi> inputSelectionChangeListener() {
        return (ListChangeListener.Change<? extends MidiInOutUi> change) -> {
            LOGGER.debug("change in input selection of {}", getMidiInOut());
            if (isPropagateInputOutputChangesToOthers()) {
                LOGGER.debug("handling changes");
                getProjectUi().setDirty();
                while (change.next()) {
                    for (MidiInOutUi removed : change.getRemoved()) {
                        removed.getMidiInOut().stopSendingTo(getMidiInOut());
                        removed.setPropagateInputOutputChangesToOthers(false);
                        int index = removed.getOutputListView().getItems().indexOf(MidiInOutUi.this);
                        removed.getOutputListView().getSelectionModel().clearSelection(index);
                        removed.setPropagateInputOutputChangesToOthers(true);
                    }
                    for (MidiInOutUi added : change.getAddedSubList()) {
                        added.getMidiInOut().startSendingTo(getMidiInOut());
                        added.setPropagateInputOutputChangesToOthers(false);
                        added.getOutputListView().getSelectionModel().select(MidiInOutUi.this);
                        added.setPropagateInputOutputChangesToOthers(true);
                    }
                }
            } else {
                LOGGER.debug("not handling change, because !propagateInputOutputChangesToOthers");
            }
        };
    }

    private ListChangeListener<MidiInOutUi> outputSelectionChangeListener() {
        return (ListChangeListener.Change<? extends MidiInOutUi> change) -> {
            LOGGER.debug("change in output selection of {}", getMidiInOut());
            if (isPropagateInputOutputChangesToOthers()) {
                LOGGER.debug("handling changes");
                getProjectUi().setDirty();
                while (change.next()) {
                    for (MidiInOutUi removed : change.getRemoved()) {
                        getMidiInOut().stopSendingTo(removed.getMidiInOut());
                        removed.setPropagateInputOutputChangesToOthers(false);
                        int index = removed.getInputListView().getItems().indexOf(MidiInOutUi.this);
                        removed.getInputListView().getSelectionModel().clearSelection(index);
                        removed.setPropagateInputOutputChangesToOthers(true);
                    }
                    for (MidiInOutUi added : change.getAddedSubList()) {
                        getMidiInOut().startSendingTo(added.getMidiInOut());
                        added.setPropagateInputOutputChangesToOthers(false);
                        added.getInputListView().getSelectionModel().select(MidiInOutUi.this);
                        added.setPropagateInputOutputChangesToOthers(true);
                    }
                }
            } else {
                LOGGER.debug("changes already made from input side");
            }
        };
    }

    protected void setDirty() {
        getProjectUi().setDirty();
    }

    public T getMidiInOut() {
        return midiInOut;
    }

    public Tab getInstrumentTab() {
        return instrumentTab;
    }

    public void setInstrumentTab(Tab instrumentTab) {
        replace(getTabs(), this.instrumentTab, instrumentTab);
        this.instrumentTab = instrumentTab;
        if (instrumentTab != null) {
            getSelectionModel().select(instrumentTab);
        }
    }

    public StringProperty nameProperty() {
        return name.textProperty();
    }

    public String getName() {
        return name.textProperty().get();
    }

    public void setName(String nm) {
        name.textProperty().set(nm);
    }

    public ProjectUi getProjectUi() {
        return projectUi;
    }

    public ReadOnlyBooleanProperty activeSenderProperty() {
        return activeSenderWrapper.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty activeReceiverProperty() {
        return activeReceiverWrapper.getReadOnlyProperty();
    }

    protected void syncAll() {
        syncActiveSenderReceiver();
    }

    protected void syncActiveSenderReceiver() {
        T midiInOut = getMidiInOut();
        if (midiInOut != null) {
            activeReceiverWrapper.set(midiInOut.isActiveReceiver());
            activeSenderWrapper.set(midiInOut.isActiveSender());
        }
    }

    protected MidiInOutUiListView getOutputListView() {
        return outputListView;
    }

    protected MidiInOutUiListView getInputListView() {
        return inputListView;
    }

    /**
     * @return the propagateSelectionChangesToOthers
     */
    public boolean isPropagateInputOutputChangesToOthers() {
        return propagateInputOutputChangesToOthers;
    }

    /**
     * @param propagateInputOutputChangesToOthers the propagateSelectionChangesToOthers to set
     */
    public void setPropagateInputOutputChangesToOthers(boolean propagateInputOutputChangesToOthers) {
        this.propagateInputOutputChangesToOthers = propagateInputOutputChangesToOthers;
    }

    public void restoreWindowPositionAndSetAutosave() {
        ProjectUi.restoreWindowPosition((Stage) getScene().getWindow(), getMidiInOut().getInfo());
    }

    public void beforeSave() {
        double x = getScene().getWindow().getX();
        double y = getScene().getWindow().getY();
        getMidiInOut().getInfo().put(App.PREF_X, x);
        getMidiInOut().getInfo().put(App.PREF_Y, y);
        LOGGER.info("moved X,Y to {} {}", x, y);
    }

    public boolean isConnected() {
        return !getInputListView().getSelectionModel().isEmpty() || !getOutputListView().getSelectionModel().isEmpty();
    }

    public String connectedDebugInfo() {
        StringBuilder result = new StringBuilder();
        for (MidiInOutUi input : getInputListView().getSelectionModel().getSelectedItems()) {
            result.append(input.getName()).append(" -> ").append("\n");
        }
        result.append(getName()).append("\n");
        for (MidiInOutUi output : getOutputListView().getSelectionModel().getSelectedItems()) {
            result.append(" -> ").append(output.getName()).append("\n");
        }
        return result.toString();
    }

    public void sizeToScene() {
        //((Stage) getScene().getWindow()).sizeToScene();
    }

    protected ObservableList<MidiInOutUi> getSendingTo() {
        return getOutputListView().getSelectionModel().getSelectedItems();
    }

    protected ObservableList<MidiInOutUi> getReceivingFrom() {
        return getInputListView().getSelectionModel().getSelectedItems();
    }

    private void forAllConnected(Consumer<MidiInOutUi> consumer) {
        for (MidiInOutUi receiver : getSendingTo()) {
            consumer.accept(receiver);
        }
        for (MidiInOutUi sender : getReceivingFrom()) {
            consumer.accept(sender);
        }
    }

    private void forAllUnconnected(Consumer<MidiInOutUi> consumer) {
        Set<MidiInOutUi> unconnecteds = new HashSet<>(getProjectUi().getMidiInOutUiList());
        unconnecteds.removeAll(getSendingTo());
        unconnecteds.removeAll(getReceivingFrom());
        unconnecteds.remove(this);
        for (MidiInOutUi unconnected : unconnecteds) {
            consumer.accept(unconnected);
        }
    }

    @FXML
    private void connectedToFront() {
        forAllConnected(midiInOutUi -> ((Stage) midiInOutUi.getScene().getWindow()).toFront());
    }

    @FXML
    private void connectedMinimize() {
        forAllConnected(midiInOutUi -> ((Stage) midiInOutUi.getScene().getWindow()).setIconified(true));
    }

    @FXML
    private void unconnectedToFront() {
        forAllUnconnected(midiInOutUi -> ((Stage) midiInOutUi.getScene().getWindow()).toFront());
    }

    @FXML
    private void unconnectedMinimize() {
        forAllUnconnected(midiInOutUi -> ((Stage) midiInOutUi.getScene().getWindow()).setIconified(true));
    }

    @FXML
    private void projectToFront() {
        ((Stage) getProjectUi().getScene().getWindow()).toFront();
    }

    public void forceCloseWindow() {
        getInputListView().getSelectionModel().clearSelection();
        getOutputListView().getSelectionModel().clearSelection();
        closeWindow();
    }

    @FXML
    public boolean closeWindow() {
        boolean result = false;
        if (canCloseWindow()) {
            menuItem.getParentMenu().getItems().remove(menuItem);
            getProjectUi().remove(this);
            getMidiInOut().close();
            ((Stage) getScene().getWindow()).close();
            result = true;
        }
        return result;
    }

    protected boolean canCloseWindow() {
        boolean result = false;
        if (isConnected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(App.MESSAGES.getString("cannotClose"));
            alert.setContentText(App.MESSAGES.getString("disconnectMidiInOutFirst")
                    + "\n\n" + connectedDebugInfo()
            );
            ButtonType ok = new ButtonType(App.MESSAGES.getString("ok"), ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(ok);
            alert.showAndWait();
        } else {
            result = true;
        }
        return result;
    }

    public String toString() {
        return getMidiInOut().getName() + ": " + super.toString();
    }
}
