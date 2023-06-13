package nl.nlcode.m.ui;

import javafx.application.Platform;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import nl.nlcode.m.linkui.ObjectUpdateProperty;

/**
 *
 * @author leo
 */
public class ObjectUpdatePropertyBridge<T> extends ObjectPropertyBase<T> implements ObjectUpdateProperty.Listener<T> {

    private ObjectUpdateProperty backendUpdateProperty;
    private Property<T> fxThreadProperty;

    protected ObjectUpdatePropertyBridge() {
    }

    public static <T> ObjectUpdatePropertyBridge create(ObjectUpdateProperty<T, ?, ?> backendUpdateProperty, Property<T> fxThreadProperty) {
        ObjectUpdatePropertyBridge result = new ObjectUpdatePropertyBridge();
        backendUpdateProperty.addListener(result);
        result.backendUpdateProperty = backendUpdateProperty;
        result.fxThreadProperty = fxThreadProperty;
        fxThreadProperty.setValue(backendUpdateProperty.get());
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
        backendUpdateProperty.set(value, this);
    }

    @Override
    public void setValue(T value) {
        set(value);
    }
    
    /**
     * Called by ObjectUpdateProperty, on actual value change.
     *
     * @param oldValue
     * @param newValue
     */
    @Override
    public void updatePropertyValueChanged(T oldValue, T newValue) {
        Platform.runLater(() -> setValue(newValue));
    }

}
