package nl.nlcode.m.ui;


import java.lang.invoke.MethodHandles;
import javafx.scene.control.Spinner;
import nl.nlcode.javafxutil.FxmlController;
import nl.nlcode.m.engine.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public class I0through127Spinner extends Spinner implements FxmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public I0through127Spinner() {
        loadFxml(I18n.msg());
    }

}
