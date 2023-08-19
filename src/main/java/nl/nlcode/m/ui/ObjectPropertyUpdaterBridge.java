package nl.nlcode.m.ui;

import javafx.application.Platform;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import nl.nlcode.m.linkui.ObjectUpdater;

/**
 *
 * @author leo
 */
public class ObjectPropertyUpdaterBridge<T> extends ObjectPropertyBase<T> implements ObjectUpdater.Listener<T> {

    private ObjectUpdater backendUpdater;
    private Property<T> fxThreadProperty;

    protected ObjectPropertyUpdaterBridge() {
    }

    public static <T> ObjectPropertyUpdaterBridge create(ObjectUpdater<T, ?, ?> backendUpdater, Property<T> fxThreadProperty) {
        ObjectPropertyUpdaterBridge result = new ObjectPropertyUpdaterBridge();
        backendUpdater.addListener(result);
        result.backendUpdater = backendUpdater;
        result.fxThreadProperty = fxThreadProperty;
        fxThreadProperty.setValue(backendUpdater.get());
        result.bindBidirectional(fxThreadProperty);
        return result;
    }

    @Override
    public Object getBean() {
        return null; // TODO
    }

    @Override
    public String getName() {
        return ""; // TODO
    }

    @Override
    public void set(T value) {
        super.set(value);
        backendUpdater.set(value, this);
    }

    @Override
    public void setValue(T value) {
        set(value);
    }
    
    /**
     * Called by ObjectUpdater, on actual value change.
     *
     * @param oldValue
     * @param newValue
     */
    @Override
    public void updaterValueChanged(T oldValue, T newValue) {
        Platform.runLater(() -> setValue(newValue));
    }

}
