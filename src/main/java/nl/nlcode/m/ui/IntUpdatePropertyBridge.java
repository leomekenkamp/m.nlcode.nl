package nl.nlcode.m.ui;

import javafx.application.Platform;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import nl.nlcode.m.linkui.IntUpdateProperty;

/**
 *
 * @author leo
 */
public class IntUpdatePropertyBridge extends IntegerPropertyBase implements IntUpdateProperty.Listener<Integer> {

    private IntUpdateProperty backendUpdateProperty;
    private Property<Integer> fxThreadProperty;
    private ObjectProperty<Integer> objectProperty;

    protected IntUpdatePropertyBridge(int value) {
        super.set(value);
    }

    public static IntUpdatePropertyBridge create(IntUpdateProperty backendUpdateProperty) {
        IntUpdatePropertyBridge result = new IntUpdatePropertyBridge(backendUpdateProperty.get());
        backendUpdateProperty.addListener(result);
        result.backendUpdateProperty = backendUpdateProperty;
        return result;
    }

    public static IntUpdatePropertyBridge create(IntUpdateProperty backendUpdateProperty, Property<Integer> fxThreadProperty) {
        IntUpdatePropertyBridge result = create(backendUpdateProperty);
        result.fxThreadProperty = fxThreadProperty;
        result.objectProperty = result.asObject();
        fxThreadProperty.setValue(backendUpdateProperty.get());
        fxThreadProperty.bindBidirectional(result.objectProperty);
        return result;
    }

    public void refresh() {
        fxThreadProperty.unbindBidirectional(objectProperty);
        fxThreadProperty.setValue(0);
        fxThreadProperty.setValue(1);
        fxThreadProperty.setValue(getValue());
        fxThreadProperty.bindBidirectional(objectProperty);
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
    public void set(int i) {
        super.set(i);
        backendUpdateProperty.set(i, this);
    }

    @Override
    public void setValue(Number number) {
        set(number.intValue());
    }

    /**
     * Called by IntUpdateProperty, on actual value change.
     *
     * @param oldValue
     * @param newValue
     */
    @Override
    public void updatePropertyValueChanged(Integer oldValue, Integer newValue) {
        Platform.runLater(() -> setValue(newValue));
    }
}
