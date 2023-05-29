package nl.nlcode.javafxutil;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.util.Callback;
import static nl.nlcode.javafxutil.FxmlController.LOGGER;

/**
 *
 * @author leo
 */
public class FxmlHelper {
    
    private FxmlHelper() {    
    }
    
    public static <T> T loadFxml(Class<?> controllerClass) {
        return null;
    }
    public static <T> T loadFxml(Class<T> baseClass, Callback<Class<?>, Object> factory, ResourceBundle bundle) {
        return loadFxml(baseClass.getResource(baseClass.getSimpleName().concat(".fxml")), factory, bundle);
    }

    public static <T> T loadFxml(String fxmlFileName, Callback<Class<?>, Object> factory, ResourceBundle bundle) {
        return loadFxml(FxmlHelper.class.getResource(fxmlFileName), factory, bundle);
    }

    public static <T> T loadFxml(URL from, Callback<Class<?>, Object> factory, ResourceBundle bundle) {
        LOGGER.debug("loading {}", from);
        FXMLLoader fxmlLoader = new FXMLLoader(from, bundle);

        if (factory == null) {
            LOGGER.debug("no controller factory");
        } else {
            LOGGER.debug("controller factory {}", factory);
            fxmlLoader.setControllerFactory(factory);
        }
        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
