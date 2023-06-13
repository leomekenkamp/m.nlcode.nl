package nl.nlcode.m.ui;

import javafx.application.Platform;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import nl.nlcode.m.linkui.DoubleUpdateProperty;
import nl.nlcode.m.linkui.IntUpdateProperty;

/**
 *
 * @author leo
 */
public class DoubleUpdatePropertyBridge extends DoublePropertyBase implements IntUpdateProperty.Listener<Double> {

    private DoubleUpdateProperty backendUpdateProperty;
    private Property<Double> fxThreadProperty;
    private ObjectProperty<Double> objectProperty;


    protected DoubleUpdatePropertyBridge(double value) {
        super.set(value);
    }

    public static DoubleUpdatePropertyBridge create(DoubleUpdateProperty backendUpdateProperty) {
        DoubleUpdatePropertyBridge result = new DoubleUpdatePropertyBridge(backendUpdateProperty.get());
        backendUpdateProperty.addListener(result);
        result.backendUpdateProperty = backendUpdateProperty;
        return result;
    }

    public static DoubleUpdatePropertyBridge create(DoubleUpdateProperty backendUpdateProperty, Property<Double> fxThreadProperty) {
        DoubleUpdatePropertyBridge result = create(backendUpdateProperty);
        result.fxThreadProperty = fxThreadProperty;
        result.objectProperty = result.asObject();
        fxThreadProperty.setValue(backendUpdateProperty.get());
        fxThreadProperty.bindBidirectional(result.objectProperty);
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
    public void set(double i) {
        super.set(i);
        backendUpdateProperty.set(i, this);
    }

    @Override
    public void setValue(Number number) {
        set(number.doubleValue());
    }

    /**
     * Called by IntUpdateProperty, on actual value change.
     *
     * @param oldValue
     * @param newValue
     */
    @Override
    public void updatePropertyValueChanged(Double oldValue, Double newValue) {
        Platform.runLater(()->setValue(newValue));
    }
}
