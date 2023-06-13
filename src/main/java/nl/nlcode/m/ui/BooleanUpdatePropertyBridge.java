package nl.nlcode.m.ui;

import javafx.application.Platform;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.Property;
import nl.nlcode.m.linkui.BooleanUpdateProperty;
import nl.nlcode.m.linkui.IntUpdateProperty;

/**
 *
 * @author leo
 */
public class BooleanUpdatePropertyBridge extends BooleanPropertyBase implements IntUpdateProperty.Listener<Boolean> {

    private BooleanUpdateProperty backendUpdateProperty;
    private Property<Boolean> fxThreadProperty;

    protected BooleanUpdatePropertyBridge(boolean value) {
        super.set(value);
    }

    public static BooleanUpdatePropertyBridge create(BooleanUpdateProperty backendUpdateProperty) {
        BooleanUpdatePropertyBridge result = new BooleanUpdatePropertyBridge(backendUpdateProperty.get());
        backendUpdateProperty.addListener(result);
        result.backendUpdateProperty = backendUpdateProperty;
        return result;
    }

    public static BooleanUpdatePropertyBridge create(BooleanUpdateProperty backendUpdateProperty, Property<Boolean> fxThreadProperty) {
        BooleanUpdatePropertyBridge result = create(backendUpdateProperty);
        result.fxThreadProperty = fxThreadProperty;
        fxThreadProperty.setValue(backendUpdateProperty.get());
        fxThreadProperty.bindBidirectional(result);
        return result;
    }

//    public void refresh() {
//        fxThreadProperty.unbindBidirectional(this);
//        fxThreadProperty.setValue(false);
//        fxThreadProperty.setValue(true);
//        fxThreadProperty.setValue(getValue());
//        fxThreadProperty.bindBidirectional(this);
//    }

    @Override
    public Object getBean() {
        return null; // TODO
    }

    @Override
    public String getName() {
        return ""; // TODO
    }

    @Override
    public void set(boolean i) {
        super.set(i);
        backendUpdateProperty.set(i, this);
    }

    @Override
    public void setValue(Boolean b) {
        set(b.booleanValue());
    }

    /**
     * Called by IntUpdateProperty, on actual value change.
     *
     * @param oldValue
     * @param newValue
     */
    @Override
    public void updatePropertyValueChanged(Boolean oldValue, Boolean newValue) {
        Platform.runLater(() -> setValue(newValue));
    }
}
