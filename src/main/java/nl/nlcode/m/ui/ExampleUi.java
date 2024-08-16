package nl.nlcode.m.ui;

import java.lang.invoke.MethodHandles;
import javafx.scene.control.MenuItem;
import nl.nlcode.m.engine.Example;
import nl.nlcode.m.engine.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lmekenkamp
 */
public class ExampleUi extends MidiInOutUi<Example> implements Example.Ui {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public ExampleUi(ProjectUi projectUi, Example example, MenuItem menuItem) {
        super(projectUi, example, menuItem);
        loadFxml(ExampleUi.class, I18n.msg());
    }

    @Override
    protected void handleInitialize() {
        super.handleInitialize();
        
    }

}
