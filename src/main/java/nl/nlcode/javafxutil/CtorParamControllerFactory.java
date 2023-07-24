package nl.nlcode.javafxutil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javafx.util.Callback;

/**
 *
 * @author leo
 */
public class CtorParamControllerFactory implements Callback<Class<?>, Object> {

    private Object[] ctorParams;
    private Class<?>[] ctorClasses;

    public CtorParamControllerFactory(Object ... ctorParams) {
        this.ctorParams = ctorParams;
        ctorClasses = new Class<?>[ctorParams.length];
        for (int i = 0; i < ctorParams.length; i++) {
            ctorClasses[i] = ctorParams[i].getClass();
        }
    }
    
    @Override
    public Object call(Class<?> type) {
        try {
            Constructor<?> ctor = type.getConstructor(ctorClasses);
            return ctor.newInstance(ctorParams);
        } catch (SecurityException 
                | InstantiationException 
                | IllegalAccessException
                | IllegalArgumentException 
                | InvocationTargetException
                | NoSuchMethodException e) {
            throw new IllegalStateException("troubles with <" + type + "> and <" + ctorClasses + ">", e);
        }
    }
    
    public String toString() {
        return "" + ctorParams;
    }
}
