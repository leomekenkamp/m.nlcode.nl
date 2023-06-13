package nl.nlcode.marshalling;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * I hate direct dependence on a library. I also want versioning in save files. Combine the two, and
 * you need something like this and {@code Marshalled_1}. The idea is that classes are themselves
 * responsible for writing their data to Java Records and (re)creating objects from that data. Since
 * Jackson relies on annotations for handling multiple instances of the same object as references
 * (or custom code), {@code Marshallable_1} will do that. That way the switch to another format than
 * Jackson should be trivial.
 *
 * @author leo
 */
public interface Marshallable {

    public static record Reference(int id, int refId) implements Marshalled {

        @Override
        public Marshallable unmarshal(Context context) {
            return context.alreadyUnmarshalled(this);
        }

        @Override
        public void unmarshalInto(Context context, Marshallable target) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Marshallable createMarshallable() {
            throw new UnsupportedOperationException(); 
        }

    }
    
//    public static record KV()

    /**
     * NOT thread safe.
     */
    public static class Context {

        private int id = -1;

        private IdentityHashMap<Marshallable, Integer> originalToId = new IdentityHashMap<>();

        /**
         * Use of {@code MarshalHelper.marshal(Marshallable.Context, Marshallable) is preferred.
         */
        public Marshalled marshal(Marshallable object) {
            id += 1;
            Marshalled result;
            if (originalToId.containsKey(object)) {
                result = new Reference(id, originalToId.get(object));
            } else {
                originalToId.put(object, id);
                result = object.marshalInternal(id, this);
            }
            return result;
        }

        public List<Marshalled> toSaveDataList(Collection<? extends Marshallable> source) {
            return source.stream()
                    .map(toRecord -> marshal(toRecord))
                    .collect(Collectors.toList());
        }

    }

    /**
     * Should only be called internally by {@code Context}. DO NOT CALL THIS METHOD YOURSELF!
     * <p>
     * Use {@code MarshalHelper.marshal(Marshallable.Context, Marshallable) to make it easy on yourself.
     * One could also call {@code Marshallable.MarshalHelper.marshal(context, Marshallable)}, but the helper is preferred.
     *
     * @param id - the id to use for the return object
     * @param context
     * @return marshalled i.e. original object
     */
    Marshalled marshalInternal(int id, Context context);

}
