package nl.nlcode.javafxutil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Callback;

/**
 *
 * @author leo
 */
public class CtorParamControllerFactory implements Callback<Class<?>, Object> {

    private Map<Class<?>, Object> classToCtorParam = new HashMap<>();

    public CtorParamControllerFactory(Object ... ctorParams) {
        add(ctorParams);
    }
    
    public final void add(Object ... ctorParams) {
        for (Object ctorParam : ctorParams) {
            classToCtorParam.put(ctorParam.getClass(), ctorParam);
        }
    }

    @Override
    public Object call(Class<?> type) {
        try {
            Constructor<?>[] ctors = type.getConstructors();
            if (ctors.length != 1) {
                throw new IllegalStateException("multiple ctors for " + type);
            }
            Constructor<?> ctor = ctors[0];
            Class<?>[] paramTypes = ctor.getParameterTypes();
            Object[] initArgs = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                initArgs[i] = classToCtorParam.get(paramTypes[i]);
            }
            return ctor.newInstance(initArgs);
        } catch (SecurityException 
                | InstantiationException 
                | IllegalAccessException
                | IllegalArgumentException 
                | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public String toString() {
        return "" + classToCtorParam;
    }
}
