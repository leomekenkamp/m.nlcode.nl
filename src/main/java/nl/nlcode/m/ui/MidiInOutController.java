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
public class MidiInOutController<T extends MidiInOut>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiInOutController.class);

    private static final boolean SHORT_NAMES = true;
    
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

    static Callback<MidiInOutController<?>, Observable[]> getNameExtractor() {
        return NAME_EXTRACTOR;
    }

    private static final Callback<MidiInOutController<?>, Observable[]> NAME_EXTRACTOR = new Callback<MidiInOutController<?>, Observable[]>() {
        @Override
        public Observable[] call(MidiInOutController midiInOutUi) {
            LOGGER.debug("extractor initialized");
            return new Observable[]{
                midiInOutUi.nameProperty()
            };
        }
    };

    public MidiInOutController(ProjectUi projectUi, T midiInOut, MenuItem menuItem) {
        this.projectUi = projectUi;
        this.midiInOut = midiInOut;
        this.menuItem = menuItem;
        activeSenderWrapper = new ReadOnlyBooleanWrapper(this, "activeSenderWrapper");
        activeReceiverWrapper = new ReadOnlyBooleanWrapper(this, "activeReceiverWrapper");
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
        inputListView.getSelectionModel().getSelectedItems().addListener(inputSelectionChangeListener);
        outputListView.getSelectionModel().getSelectedItems().addListener(outputSelectionChangeListener);

        activeSenderWrapper.addListener((ov, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
                getStyleClass().add(ACTIVE_SENDER);
            } else {
                getStyleClass().remove(ACTIVE_SENDER);
            }
        });

        activeReceiverWrapper.addListener((ov, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
// todo                getStyleClass().add(ACTIVE_RECEIVER);
            } else {
// todo                getStyleClass().remove(ACTIVE_RECEIVER);
            }
        });

// todo        getStyleClass().add(MIDI_IN_OUT);
    }

    private ListChangeListener<MidiInOutController> inputSelectionChangeListener = new ListChangeListener<>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends MidiInOutController> change) {
            LOGGER.debug("change in input selection of <{}>", getMidiInOut());
            if (isPropagateInputOutputChangesToOthers()) {
                LOGGER.debug("handling changes {}", change);
                getProjectUi().setDirty();
                while (change.next()) {
                    handleForInputSelection(change);
                }
            } else {
                LOGGER.debug("changes already made on output side");
            }
        }
    };

    private void handleForInputSelection(ListChangeListener.Change<? extends MidiInOutController> change) {
        if (change.wasRemoved()) {
            int localIndex = change.getFrom();
            for (MidiInOutController removed : change.getRemoved()) {
                if (removed == null) {
                    // change.getRemoved() is buggy, use indices instead
                    removed = getInputListView().getItems().get(localIndex);
                }
                LOGGER.debug("removed <{}> from input of <{}>", removed.getMidiInOut(), this);
                removed.getMidiInOut().stopSendingTo(getMidiInOut());
                removed.setPropagateInputOutputChangesToOthers(false);
                int remoteIndex = removed.getOutputListView().getItems().indexOf(MidiInOutController.this);
                removed.getOutputListView().getSelectionModel().clearSelection(remoteIndex);
                removed.setPropagateInputOutputChangesToOthers(true);
                localIndex += 1;
            }
        }
        if (change.wasAdded()) {
            for (MidiInOutController added : change.getAddedSubList()) {
                LOGGER.debug("added <{}> to input of <{}>", added.getMidiInOut(), this);
                added.setPropagateInputOutputChangesToOthers(false);
                added.getOutputListView().getSelectionModel().select(MidiInOutController.this);
                added.setPropagateInputOutputChangesToOthers(true);
                added.getMidiInOut().startSendingTo(getMidiInOut());
            }
        }
    }

    private ListChangeListener<MidiInOutController> outputSelectionChangeListener = new ListChangeListener<>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends MidiInOutController> change) {
            LOGGER.debug("change in output selection of {}", getMidiInOut());
            if (isPropagateInputOutputChangesToOthers()) {
                LOGGER.debug("handling changes {}", change);
                getProjectUi().setDirty();
                while (change.next()) {
                    handleForOutputSelection(change);
                }
            } else {
                LOGGER.debug("changes already made on input side");
            }
        }
    };

    private void handleForOutputSelection(ListChangeListener.Change<? extends MidiInOutController> change) {
        if (change.wasRemoved()) {
            int localIndex = change.getFrom();
            for (MidiInOutController removed : change.getRemoved()) {
                if (removed == null) {
                    // change.getRemoved() is buggy, use indices instead
                    removed = getOutputListView().getItems().get(localIndex);
                }
                LOGGER.debug("removed <{}> from output of <{}>", removed.getMidiInOut(), this);
                getMidiInOut().stopSendingTo(removed.getMidiInOut());
                removed.setPropagateInputOutputChangesToOthers(false);
                int remoteIndex = removed.getInputListView().getItems().indexOf(MidiInOutController.this);
                removed.getInputListView().getSelectionModel().clearSelection(remoteIndex);
                removed.setPropagateInputOutputChangesToOthers(true);
                localIndex += 1;
            }
        }
        if (change.wasAdded()) {
            for (MidiInOutController added : change.getAddedSubList()) {
                LOGGER.debug("added <{}> to output of <{}>", added.getMidiInOut(), this);
                getMidiInOut().startSendingTo(added.getMidiInOut());
                added.setPropagateInputOutputChangesToOthers(false);
                added.getInputListView().getSelectionModel().select(MidiInOutController.this);
                added.setPropagateInputOutputChangesToOthers(true);
            }
        }
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
        for (MidiInOutController input : getInputListView().getSelectionModel().getSelectedItems()) {
            result.append(input).append(" -> ").append("\n");
        }
        result.append(getName()).append("\n");
        for (MidiInOutController output : getOutputListView().getSelectionModel().getSelectedItems()) {
            result.append(" -> ").append(output).append("\n");
        }
        return result.toString();
    }

    public void sizeToScene() {
        //((Stage) getScene().getWindow()).sizeToScene();
    }

    protected ObservableList<MidiInOutController> getSendingTo() {
        return getOutputListView().getSelectionModel().getSelectedItems();
    }

    protected ObservableList<MidiInOutController> getReceivingFrom() {
        return getInputListView().getSelectionModel().getSelectedItems();
    }

    private void forAllConnected(Consumer<MidiInOutController> consumer) {
        for (MidiInOutController receiver : getSendingTo()) {
            consumer.accept(receiver);
        }
        for (MidiInOutController sender : getReceivingFrom()) {
            consumer.accept(sender);
        }
    }

    private void forAllUnconnected(Consumer<MidiInOutController> consumer) {
        Set<MidiInOutController> unconnecteds = new HashSet<>(getProjectUi().getMidiInOutUiList());
        unconnecteds.removeAll(getSendingTo());
        unconnecteds.removeAll(getReceivingFrom());
        unconnecteds.remove(this);
        for (MidiInOutController unconnected : unconnecteds) {
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
//        inputListView.getSelectionModel().getSelectedItems().removeListener(inputSelectionChangeListener);
//        outputListView.getSelectionModel().getSelectedItems().removeListener(outputSelectionChangeListener);
//        inputListView.getSelectionModel().clearSelection();
//        outputListView.getSelectionModel().clearSelection();
        menuItem.getParentMenu().getItems().remove(menuItem);
        getProjectUi().remove(this);
        getMidiInOut().close();
        ((Stage) getScene().getWindow()).close();
    }

    @FXML
    public boolean closeWindow() {
        boolean result = false;
        if (canCloseWindow()) {
            forceCloseWindow();
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
        if (SHORT_NAMES) {
            return getMidiInOut().getName();
        } else {
            return getMidiInOut().getName() + ": " + super.toString();
        }
    }
}
