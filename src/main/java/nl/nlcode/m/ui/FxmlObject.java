package nl.nlcode.m.ui;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.LinkedList;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper interface to load and instantiate a user interface element from one or more fxml files.
 *
 * @author leo
 */
public interface FxmlObject {

    static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    default void loadFxmlObject() {
        LinkedList<URL> toBeLoaded = new LinkedList<>();
        LOGGER.debug("loading custom component {}", getClass());
        for (Class currentClass = getClass(); FxmlObject.class.isAssignableFrom(currentClass); currentClass = currentClass.getSuperclass()) {
            final URL url = classToFxmlUrl(currentClass);
            toBeLoaded.addFirst(url);
            LOGGER.debug("will load <{}> for <{}>", url, currentClass);
        }
        for (URL url : toBeLoaded) {
            LOGGER.debug("loading <{}>", url);
            FXMLLoader fxmlLoader = new FXMLLoader(url, App.MESSAGES);
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        fxmlObjectLoaded();
    }

    default <T> void replace(ObservableList<T> list, T oldValue, T newValue) {
        int index = list.indexOf(oldValue);
        LOGGER.debug("oldValue <{}>, newValue <{}>, index <{}>", oldValue, newValue, index);
        if (index >= 0) {
            list.remove(oldValue);
            list.add(index, newValue);
        }
    }

    default URL classToFxmlUrl(Class type) {
        return type.getResource(type.getSimpleName() + ".fxml");
    }

    default void fxmlObjectLoaded() {
    }
}
