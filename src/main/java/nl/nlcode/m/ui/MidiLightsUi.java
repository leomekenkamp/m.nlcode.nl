package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.MidiLights;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiLightsUi extends MidiInOutUi<MidiLights> implements FxmlController, MidiLights.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Color TRANSPARENT = Color.rgb(0, 0, 0, 0);

    @FXML
    private Canvas keysCanvas;
    
    private GraphicsContext ctx;

    public MidiLightsUi(ProjectUi projectUi, MidiLights midiLights, MenuItem menuItem) {
        super(projectUi, midiLights, menuItem);
        loadFxml(MidiLightsUi.class, App.MESSAGES);
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        ctx = keysCanvas.getGraphicsContext2D();
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, keysCanvas.getWidth(), keysCanvas.getHeight());
//        getMidiInOut().setOnMidiMessageProcessed((midiMessage, time) -> {
//            Platform.runLater(() -> {
//                if (midiMessage instanceof ShortMessage shortMessage) {
//                    Color color = null;
//                    if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
//                        color = Color.WHITE;
//                    } else if (shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
//                        color = TRANSPARENT;
//                    }
//                    if (color != null) {
//                        setColor(ctx, 127 - shortMessage.getData1(), shortMessage.getChannel(), color);
//                    }
//                }
//            });
//        });
    }

    @Override
    public void received(MidiMessage message, long timestamp) {
        if (message instanceof final ShortMessage shortMessage) {
            final Color color;
            if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
                color = Color.WHITE;
            } else if (shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
                color = TRANSPARENT;
            } else {
                color = null;
            }
            if (color != null) {
                Platform.runLater(() -> {
                    setColor(127 - shortMessage.getData1(), shortMessage.getChannel(), color);
                });
            }
        }
    }

    private void setColor(int x, int y, Color color) {
        ctx.getPixelWriter().setColor(256 - (2 * x), 2 * y, color);
        ctx.getPixelWriter().setColor(256 - (2 * x + 1), 2 * y, color);
        ctx.getPixelWriter().setColor(256 - (2 * x), 2 * y + 1, color);
        ctx.getPixelWriter().setColor(256 - (2 * x + 1), 2 * y + 1, color);
    }

}
