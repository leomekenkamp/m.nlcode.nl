package nl.nlcode.marshalling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 *
 * @author leo
 */
public interface Marshalled<T extends Marshallable> {

    int id();

    /**
     * NOT thread safe.
     */
    public static class Context {

        private Map<Integer, Marshallable> idToMarchallable = new HashMap<>();

        /**
         * Do NOT call this method yourself. Use {@code Marshalled#unmarshal(Context)} instead.
         * @param marshalled
         * @param target 
         */
        public void unmarshal(Marshalled marshalled, Marshallable target) {
            idToMarchallable.put(marshalled.id(), target);
            marshalled.unmarshalInternal(this, target);
        }

        Marshallable alreadyUnmarshalled(Marshallable.Reference reference) {
            return idToMarchallable.get(reference.refId());
        }

    }

    default T unmarshal(Context context) {
        T result = createMarshallable();
        context.unmarshal(this, result);
        return result;
    }
    
    void unmarshalInternal(Context context, T target);
    
    default T createMarshallable() {
        throw new IllegalStateException("override createMarshallable() (in the record class that implements Marshalled) to return a new instance");
    }

}
