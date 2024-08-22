package nl.nlcode.m.ui;

import java.awt.Toolkit;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import javax.sound.midi.MidiDevice;
import nl.nlcode.m.engine.Control;
import nl.nlcode.m.engine.FunctionalException;
import nl.nlcode.m.engine.MidiDeviceMgr;
import nl.nlcode.m.engine.MidiInOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starting point. Apart from {@link nl.nlcode.m.Launcher} to simplify package
 * and module management
 */
public class FxApp extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String PREF_X = "x";

    public static final String PREF_Y = "y";

    public static final String DEFAULT_CSS_FILENAME = "dark.css";

    public static final String DEFAULT_STYLE_SHEET = ControlUi.class.getResource(DEFAULT_CSS_FILENAME).toExternalForm();

    private static final String STYLE_SHEET_PREF = "css";

    private static String styleSheet;

    public static void main(String[] args) {
        launch(args);
    }

    private static volatile FxApp instance;
    
    public static synchronized FxApp getInstance() {
        return instance;
    }
    
   public static synchronized void start() {
       //Thread t = new Thread(() -> {
           launch();
       //});
   }

    @Override
    public  void init() throws Exception {
        LOGGER.info("initialize");
        // Not prefetching at the moment, since CoreMidi4J hangs if we do.
        //preLoadMidiDeviceInfo();
    }

    @Override
    public synchronized void start(Stage stage) throws IOException {
        LOGGER.info("starting");
        if (instance != null) {
            throw new IllegalStateException("cannot start FxApp twice");
        }
        instance = this;
        try {
            Preferences preferences = Preferences.userNodeForPackage(Control.class);
            setStyleSheet(preferences.get(STYLE_SHEET_PREF, DEFAULT_STYLE_SHEET));
            Thread.setDefaultUncaughtExceptionHandler(FxApp::showError);
            ControlUi controlUi = ControlUi.createInstance(Control.getInstance());
            Scene scene = new Scene(controlUi);
            addStyleSheet(scene);
            stage.setScene(scene);
            stage.setOnCloseRequest(onCloseRequest(controlUi));
            controlUi.restoreWindowPositionAndSetAutosave();
            stage.setResizable(false);
            stage.show();
        } catch (RuntimeException e) {
            LOGGER.error("cannot start", e);
            e.printStackTrace(System.out);
            throw e;
        }
    }

    @Override
    public synchronized void stop() {
        LOGGER.info("stopping");
        ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.SECONDS);
        instance = null;
        //System.exit(0); // needed because the midi system will prevent proper shutdown
        LOGGER.info("this line should not be executed anymore");
    }

    private EventHandler<WindowEvent> onCloseRequest(ControlUi controlUi) {
        return event -> {
            if (!controlUi.closeWindow()) {
                event.consume();
            }
        };
    }

    public static void setStyleSheet(String styleSheet) {
        FxApp.styleSheet = styleSheet;
        Preferences.userNodeForPackage(Control.class).put(STYLE_SHEET_PREF, styleSheet);
    }

    public static void replaceStyleSheetOnAllWindows() {
        for (Window window : Stage.getWindows()) {
            ObservableList<String> styleSheets = window.getScene().getStylesheets();
            styleSheets.clear();
            styleSheets.add(STYLESHEET_MODENA);
            styleSheets.add(styleSheet);
            window.sizeToScene();
        }
    }

    public static String getStyleSheet() {
        return styleSheet;
    }

    protected static void addStyleSheet(Scene scene) {
        LOGGER.debug("using style sheet {}", styleSheet);
        scene.getStylesheets().add(styleSheet);
    }

    public static Stage createStage(Parent root) {
        Scene scene = new Scene(root);
        addStyleSheet(scene);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setResizable(false);
        return stage;
    }

    private static void preLoadMidiDeviceInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("pre-fetching MIDI device info for faster initial reaction of user interface");
                MidiDeviceMgr.getInstance().getMidiDeviceInfo();
                LOGGER.debug("done");
            }
        }).start();
    }

    private static void showError(Thread t, Throwable e) {
        if (e instanceof FunctionalException) {
            Toolkit.getDefaultToolkit().beep();
            LOGGER.info("probably user error", e);
        } else if (!Platform.isFxApplicationThread()) {
            showErrorDialog(e);
            LOGGER.info("error in background", e);
        } else {
            showErrorDialog(e);
            e.printStackTrace(System.out);
            LOGGER.warn("An unexpected error occurred in {}", t);
        }
    }

    private static void showErrorDialog(Throwable e) {
//        ErrorUi errorUi = new ErrorUi(I18n.msg());
//        Stage stage = createStage(errorUi);
//        stage.setWidth(1000);
//        stage.setHeight(750);
//        stage.initModality(Modality.APPLICATION_MODAL);
//        stage.setResizable(true);
//
//        String errorMsg;
//        if (e instanceof FunctionalException) {
//            errorMsg = e.getMessage();
//            stage.setTitle("Computer says 'no' ...");
//        } else {
//            StringWriter errorBuffer = new StringWriter();
//            e.printStackTrace(new PrintWriter(errorBuffer));
//            errorMsg = errorBuffer.toString();
//        }
//        errorUi.getErrorMessage().setText(errorMsg);
//        stage.showAndWait();
        LOGGER.debug("error dialog shown for <{}>", e.getMessage());
    }

    private static class MidiInOutStringConverter extends ListItemStringConverter<MidiInOut> {

        public MidiInOutStringConverter(List<MidiInOut> list) {
            super(list);
        }

        @Override
        public String toString(MidiInOut midiInOut) {
            return midiInOut == null ? "" : midiInOut.getName();
        }

    }

    private static StringConverter<MidiInOut> createMidiInOutConverter(List<MidiInOut> list) {
        return new MidiInOutStringConverter(list);
    }

    private static class MidiDeviceConverter extends ListItemStringConverter<MidiDevice> {

        public MidiDeviceConverter(List<MidiDevice> list) {
            super(list);
        }

        @Override
        public String toString(MidiDevice midiDevice) {
            return midiDevice == null ? "" : MidiDeviceMgr.getDisplayName(midiDevice);
        }

    }

    public static StringConverter<MidiDevice> createMidiDeviceConverter(List<MidiDevice> list) {
        return new MidiDeviceConverter(list);
    }

}
