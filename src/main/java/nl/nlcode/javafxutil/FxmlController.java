package nl.nlcode.javafxutil;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper interface to load and instantiate a user interface element with inheritance from one or
 * more fxml files.
 *
 * @author leo
 */
public interface FxmlController<T> {

    public static class RunOnce {

        private int invocationsRemaining;

        public RunOnce(int runAtNthInvocation) {
            this.invocationsRemaining = runAtNthInvocation;
        }

        public boolean runAtGivenInvocation(Runnable runnable) {
            switch (invocationsRemaining) {
                case -1:
                    return false;
                case 0:
                    runnable.run();
                    invocationsRemaining--;
                    return true;
                default:
                    invocationsRemaining--;
                    return false;
            }
        }
    }

    static final Logger LOGGER = LoggerFactory.getLogger(FxmlController.class);

    default T loadFxml(URL from, CtorParamControllerFactory factory, ResourceBundle bundle) {
        LOGGER.debug("loading {}", from);
        FXMLLoader fxmlLoader = new FXMLLoader(from, bundle);
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        // next line to enable s Scene Builder to find the right classes?
        fxmlLoader.setClassLoader(getClass().getClassLoader());

        if (factory == null) {
            LOGGER.debug("no controller factory");
        } else {
            LOGGER.debug("controller factory {}", factory);
            fxmlLoader.setControllerFactory(factory);
        }
        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            LOGGER.warn("could not load", e);
            throw new IllegalStateException(e);
        }
    }

    default T loadFxml(ResourceBundle bundle) {
        return loadFxml(getClass(), null, bundle);
    }

    default T loadFxml(CtorParamControllerFactory factory, ResourceBundle bundle) {
        return loadFxml(getClass(), factory, bundle);
    }

    default T loadFxml(Class type, ResourceBundle bundle) {
        return loadFxml(type, null, bundle);
    }

    default T loadFxml(Class type, CtorParamControllerFactory factory, ResourceBundle bundle) {
        return loadFxml(type.getResource(type.getSimpleName() + ".fxml"), factory, bundle);
    }

    default T loadFxml(String fxmlFileName, CtorParamControllerFactory factory, ResourceBundle bundle) {
        return loadFxml(getClass().getResource(fxmlFileName), factory, bundle);
    }

    default <T> void replace(ObservableList<T> list, T oldValue, T newValue) {
        int index = list.indexOf(oldValue);
        LOGGER.debug("oldValue <{}>, newValue <{}>, index <{}>", oldValue, newValue, index);
        if (index >= 0) {
            list.remove(oldValue);
            list.add(index, newValue);
        }
    }

    default int distance(Class<?> class1, Class<?> class2) {
        Class<?> superClass;
        Class<?> subClass;
        if (class1.isAssignableFrom(class2)) {
            superClass = class1;
            subClass = class2;
        } else if (class2.isAssignableFrom(class1)) {
            superClass = class2;
            subClass = class1;
        } else {
            throw new IllegalStateException("no inheritance...");
        }
        int result = 0;
        while (subClass != superClass) {
            result++;
            subClass = subClass.getSuperclass();
        }
        LOGGER.debug("distance between <{}> and <{}>: <{}>", class1, class2, result);
        return result;
    }

}
