package nl.nlcode.m.ui;

import javafx.application.Platform;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import nl.nlcode.m.linkui.IntUpdater;

/**
 *
 * @author leo
 */
public class IntPropertyUpdaterBridge extends IntegerPropertyBase implements IntUpdater.Listener<Integer>, ObservableValue<Number> {

    private IntUpdater backendUpdater;
    private Property<Integer> fxThreadProperty;
    private ObjectProperty<Integer> objectProperty;

    protected IntPropertyUpdaterBridge(int value) {
        super.set(value);
    }

    public static IntPropertyUpdaterBridge create(IntUpdater backendUpdater) {
        IntPropertyUpdaterBridge result = new IntPropertyUpdaterBridge(backendUpdater.get());
        backendUpdater.addListener(result);
        result.backendUpdater = backendUpdater;
        return result;
    }

    public static IntPropertyUpdaterBridge create(IntUpdater backendUpdater, Property<Integer> fxThreadProperty) {
        IntPropertyUpdaterBridge result = create(backendUpdater);
        result.fxThreadProperty = fxThreadProperty;
        result.objectProperty = result.asObject();
        fxThreadProperty.setValue(backendUpdater.get());
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
        backendUpdater.set(i, this);
    }

    @Override
    public void setValue(Number number) {
        set(number.intValue());
    }

    /**
     * Called by IntUpdater, on actual value change.
     *
     * @param oldValue
     * @param newValue
     */
    @Override
    public void updaterValueChanged(Integer oldValue, Integer newValue) {
        Platform.runLater(() -> setValue(newValue));
    }
}
