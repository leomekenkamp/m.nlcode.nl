package nl.nlcode.marshalling;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author leo
 */
public interface Marshalled<T extends Marshallable> {

    public static final Marshalled[] EMPTY_ARRAY = new Marshalled[]{};

    int id();

    /**
     * NOT thread safe. 
     */
    public static class Context {

        private Map<Integer, Marshallable> idToMarchallable = new HashMap<>();

        /**
         * Should only be called internally by {@code Marshalled}. DO NOT CALL THIS METHOD YOURSELF!
         * <p>
         * Use {@code MarshalHelper.unmarshal(Marshalled.Context, Marshalled) to make it easy on yourself.
         * One could also call {@code Marshallable.marshal(context, Marshallable)}, but the helper is preferred.
         *
         * @param marshalled
         * @param target
         */
        public void unmarshal(Marshalled marshalled, Marshallable target) {
            idToMarchallable.put(marshalled.id(), target);
            marshalled.unmarshalInto(this, target);
        }

        Marshallable alreadyUnmarshalled(Marshallable.Reference reference) {
            return idToMarchallable.get(reference.refId());
        }

    }

    /**
     * Use of {@code MarshalHelper.unmarshal(Marshalled.Context, Marshalled) is preferred.
     */
    default T unmarshal(Context context) {
        T result = createMarshallable();
        context.unmarshal(this, result);
        return result;
    }

    /**
     * Should ONLY be called from within sub classes to unmarshal their super class marshalled data
     * into{@code target}
     * 
     * @param context
     * @param target target for writing this instance marshalled data into
     */
    void unmarshalInto(Context context, T target);

    /**
     * Factory method for when this instance is asked to unmarshal itself into a new {@code T} instance.
     * 
     * @return newly created object as target for unmarshalling data.
     */
//    default T createMarshallable() {
//        throw new IllegalStateException("override createMarshallable() (in record class " + this.getClass().getName());
//    }
    T createMarshallable();

}
