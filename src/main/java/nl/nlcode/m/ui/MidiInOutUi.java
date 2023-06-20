package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanIntegerProperty;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
public class MidiInOutUi<T extends MidiInOut> extends TabPane implements FxmlController, MidiInOut.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    /**
     * Should only have one child (or none)
     */
    @FXML
    private HBox instrument;
    
    public ObservableList<Node> getInstrument() {
        return instrument.getChildren();
    }
    
    public String getInstrumentTypeName() {
        return App.MESSAGES.getString(getMidiInOut().getClass().getName());
    }

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

    protected MidiInOutUi() {
    }

    public MidiInOutUi(ProjectUi projectUi, T midiInOut, MenuItem menuItem) {
        this.projectUi = projectUi;
        this.midiInOut = midiInOut;
        this.menuItem = menuItem;
        activeSenderWrapper = new ReadOnlyBooleanWrapper(this, "activeSenderWrapper");
        activeReceiverWrapper = new ReadOnlyBooleanWrapper(this, "activeReceiverWrapper");
        runOnce = new FxmlController.RunOnce(distance(getClass(), MidiInOutUi.class));
        Object x = loadFxml(MidiInOutUi.class, new CtorParamControllerFactory(projectUi), App.MESSAGES);
    }

    @FXML
    public final void initialize() {
        // Every time the FXMLLoader loads an fxml file, it will call 'initialize'. This structure
        // prevents the 'handleInitialize' method from being called multiple times; it will only be
        // called once, at the load of the MidiInOutUi.fxml file.
        runOnce.runAtGivenInvocation(() -> {
            handleInitialize();
        });
    }

    public ObservableList<MidiInOutUi<?>> getActiveSenders() {
        return projectUi.getActiveSendersReadonly();
    }

    /**
     * Will get called right after FXML file loading.
     * Subclasses *must* call their superclass <code>handleInitialize()</code> method, probably as
     * the first instruction of their overriding implementation of this method.
     */
    protected void handleInitialize() {
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
                getStyleClass().add(ACTIVE_RECEIVER);
            } else {
                getStyleClass().remove(ACTIVE_RECEIVER);
            }
        });

        getStyleClass().add(MIDI_IN_OUT);
        
    }

    private ListChangeListener<MidiInOutUi> inputSelectionChangeListener = new ListChangeListener<>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends MidiInOutUi> change) {
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

    private void handleForInputSelection(ListChangeListener.Change<? extends MidiInOutUi> change) {
        if (change.wasRemoved()) {
            int localIndex = change.getFrom();
            for (MidiInOutUi removed : change.getRemoved()) {
                if (removed == null) {
                    // change.getRemoved() is buggy, use indices instead
                    removed = getInputListView().getItems().get(localIndex);
                }
                LOGGER.debug("removed <{}> from input of <{}>", removed.getMidiInOut(), this);
                removed.getMidiInOut().stopSendingTo(getMidiInOut());
                removed.setPropagateInputOutputChangesToOthers(false);
                int remoteIndex = removed.getOutputListView().getItems().indexOf(MidiInOutUi.this);
                removed.getOutputListView().getSelectionModel().clearSelection(remoteIndex);
                removed.setPropagateInputOutputChangesToOthers(true);
                localIndex += 1;
            }
        }
        if (change.wasAdded()) {
            for (MidiInOutUi added : change.getAddedSubList()) {
                LOGGER.debug("added <{}> to input of <{}>", added.getMidiInOut(), this);
                added.setPropagateInputOutputChangesToOthers(false);
                added.getOutputListView().getSelectionModel().select(MidiInOutUi.this);
                added.setPropagateInputOutputChangesToOthers(true);
                added.getMidiInOut().startSendingTo(getMidiInOut());
            }
        }
    }

    private ListChangeListener<MidiInOutUi> outputSelectionChangeListener = new ListChangeListener<>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends MidiInOutUi> change) {
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

    private void handleForOutputSelection(ListChangeListener.Change<? extends MidiInOutUi> change) {
        if (change.wasRemoved()) {
            int localIndex = change.getFrom();
            for (MidiInOutUi removed : change.getRemoved()) {
                if (removed == null) {
                    // change.getRemoved() is buggy, use indices instead
                    removed = getOutputListView().getItems().get(localIndex);
                }
                LOGGER.debug("removed <{}> from output of <{}>", removed.getMidiInOut(), this);
                getMidiInOut().stopSendingTo(removed.getMidiInOut());
                removed.setPropagateInputOutputChangesToOthers(false);
                int remoteIndex = removed.getInputListView().getItems().indexOf(MidiInOutUi.this);
                removed.getInputListView().getSelectionModel().clearSelection(remoteIndex);
                removed.setPropagateInputOutputChangesToOthers(true);
                localIndex += 1;
            }
        }
        if (change.wasAdded()) {
            for (MidiInOutUi added : change.getAddedSubList()) {
                LOGGER.debug("added <{}> to output of <{}>", added.getMidiInOut(), this);
                getMidiInOut().startSendingTo(added.getMidiInOut());
                added.setPropagateInputOutputChangesToOthers(false);
                added.getInputListView().getSelectionModel().select(MidiInOutUi.this);
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
            result.append(input).append(" -> ").append("\n");
        }
        result.append(getName()).append("\n");
        for (MidiInOutUi output : getOutputListView().getSelectionModel().getSelectedItems()) {
            result.append(" -> ").append(output).append("\n");
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
    public void connectedToFront() {
        forAllConnected(midiInOutUi -> ((Stage) midiInOutUi.getScene().getWindow()).toFront());
    }

    @FXML
    public void connectedMinimize() {
        forAllConnected(midiInOutUi -> ((Stage) midiInOutUi.getScene().getWindow()).setIconified(true));
    }

    @FXML
    public void unconnectedToFront() {
        forAllUnconnected(midiInOutUi -> ((Stage) midiInOutUi.getScene().getWindow()).toFront());
    }

    @FXML
    public void unconnectedMinimize() {
        forAllUnconnected(midiInOutUi -> ((Stage) midiInOutUi.getScene().getWindow()).setIconified(true));
    }

    public void projectToFront() {
        ((Stage) getProjectUi().getScene().getWindow()).toFront();
    }

    public void forceCloseWindow() {
        inputListView.getSelectionModel().clearSelection();
        outputListView.getSelectionModel().clearSelection();
        inputListView.getSelectionModel().getSelectedItems().removeListener(inputSelectionChangeListener);
        outputListView.getSelectionModel().getSelectedItems().removeListener(outputSelectionChangeListener);
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
    
    protected JavaBeanIntegerProperty createBeanIntegerProperty(String propertyName) {
        try {
            return JavaBeanIntegerPropertyBuilder.create().bean(getMidiInOut()).name(propertyName).build();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
