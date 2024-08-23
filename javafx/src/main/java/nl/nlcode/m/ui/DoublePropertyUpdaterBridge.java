package nl.nlcode.m.ui;

import javafx.application.Platform;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import nl.nlcode.m.linkui.DoubleUpdater;
import nl.nlcode.m.linkui.IntUpdater;

/**
 *
 * @author leo
 */
public class DoublePropertyUpdaterBridge extends DoublePropertyBase implements IntUpdater.Listener<Double> {

    private DoubleUpdater backendUpdater;
    private Property<Double> fxThreadProperty;
    private ObjectProperty<Double> objectProperty;


    protected DoublePropertyUpdaterBridge(double value) {
        super.set(value);
    }

    public static DoublePropertyUpdaterBridge create(DoubleUpdater backendUpdater) {
        DoublePropertyUpdaterBridge result = new DoublePropertyUpdaterBridge(backendUpdater.get());
        backendUpdater.addListener(result);
        result.backendUpdater = backendUpdater;
        return result;
    }

    public static DoublePropertyUpdaterBridge create(DoubleUpdater backendUpdater, Property<Double> fxThreadProperty) {
        DoublePropertyUpdaterBridge result = create(backendUpdater);
        result.fxThreadProperty = fxThreadProperty;
        result.objectProperty = result.asObject();
        fxThreadProperty.setValue(backendUpdater.get());
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
        backendUpdater.set(i, this);
    }

    @Override
    public void setValue(Number number) {
        set(number.doubleValue());
    }

    /**
     * Called by IntUpdater, on actual value change.
     *
     * @param oldValue
     * @param newValue
     */
    @Override
    public void updaterValueChanged(Double oldValue, Double newValue) {
        Platform.runLater(()->setValue(newValue));
    }
}
