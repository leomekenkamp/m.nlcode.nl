package nl.nlcode.m.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javax.sound.midi.ShortMessage;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.MidiLights;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class MidiLightsUi extends MidiInOutUi<MidiLights> implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiLightsUi.class);

    private static final Color TRANSPARENT = Color.rgb(0, 0, 0, 0);
    
    @FXML
    private Canvas keysCanvas;

    public MidiLightsUi(ProjectUi projectUi, MidiLights midiLights, MenuItem menuItem) {
        super(projectUi, midiLights, menuItem);
        loadFxml(MidiLightsUi.class, App.MESSAGES);
    }

    @Override
    protected void doInit() {
        super.doInit();
        GraphicsContext ctx = keysCanvas.getGraphicsContext2D();
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, keysCanvas.getWidth(), keysCanvas.getHeight());
        getMidiInOut().setOnMidiMessageReceive((midiMessage, time) -> {
            Platform.runLater(() -> {
                if (midiMessage instanceof ShortMessage) {
                    Color color = null;
                    ShortMessage shortMessage = (ShortMessage) midiMessage;
                    if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
                        color = Color.WHITE;
                    } else if (shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
                        color = TRANSPARENT;
                    }
                    if (color != null) {
                        setColor(ctx, 127 - shortMessage.getData1(), shortMessage.getChannel(), color);
                    }
                }
            });
        });
    }
    
    private void setColor(GraphicsContext ctx, int x, int y, Color color) {
         ctx.getPixelWriter().setColor(256 - (2 * x), 2 * y, color);
         ctx.getPixelWriter().setColor(256 - (2 * x + 1), 2 * y, color);
         ctx.getPixelWriter().setColor(256 - (2 * x), 2 * y + 1, color);
         ctx.getPixelWriter().setColor(256 - (2 * x + 1), 2 * y + 1, color);
    }

}
