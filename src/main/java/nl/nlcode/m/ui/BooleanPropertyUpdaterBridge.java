package nl.nlcode.m.ui;

import javafx.application.Platform;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.Property;
import nl.nlcode.m.linkui.BooleanUpdater;
import nl.nlcode.m.linkui.IntUpdater;

/**
 *
 * @author leo
 */
public class BooleanPropertyUpdaterBridge extends BooleanPropertyBase implements IntUpdater.Listener<Boolean> {

    private BooleanUpdater backendUpdater;
    private Property<Boolean> fxThreadProperty;

    protected BooleanPropertyUpdaterBridge(boolean value) {
        super.set(value);
    }

    public static BooleanPropertyUpdaterBridge create(BooleanUpdater backendUpdater) {
        BooleanPropertyUpdaterBridge result = new BooleanPropertyUpdaterBridge(backendUpdater.get());
        backendUpdater.addListener(result);
        result.backendUpdater = backendUpdater;
        return result;
    }

    public static BooleanPropertyUpdaterBridge create(BooleanUpdater backendUpdater, Property<Boolean> fxThreadProperty) {
        BooleanPropertyUpdaterBridge result = create(backendUpdater);
        result.fxThreadProperty = fxThreadProperty;
        fxThreadProperty.setValue(backendUpdater.get());
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
        backendUpdater.set(i, this);
    }

    @Override
    public void setValue(Boolean b) {
        set(b.booleanValue());
    }

    /**
     * Called by IntUpdater, on actual value change.
     *
     * @param oldValue
     * @param newValue
     */
    @Override
    public void updaterValueChanged(Boolean oldValue, Boolean newValue) {
        Platform.runLater(() -> setValue(newValue));
    }
}
