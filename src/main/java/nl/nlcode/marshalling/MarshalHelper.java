package nl.nlcode.marshalling;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * Main API entry point for (un)marshallling. 
 * 
 * @author leo
 */
public final class MarshalHelper {

    private MarshalHelper() {
    }

    //
    // marshalling
    //
    
    public static <T extends Marshallable> Marshalled<T> marshal(Marshallable.Context context, T marshallable) {
        return context.marshal(marshallable);
    }

    public static <T extends Marshallable> Marshalled<T>[] marshallToArray(Marshallable.Context context, Collection<? extends T> source) {
        Marshalled<T>[] result = (Marshalled<T>[]) Array.newInstance(Marshalled.class, source.size());
        int i = 0;
        for (T marshallable : source) {
            result[i++] = MarshalHelper.marshal(context, marshallable);
        }
        return result;
    }

    public static <T extends Marshallable> List<Marshalled<T>> marshallToList(Marshallable.Context context, Collection<? extends T> source) {
        return source.stream().map(marshallable -> MarshalHelper.marshal(context, marshallable)).toList();
    }
    
    //
    // unmarshalling
    //
    
    public static <T extends Marshallable, R extends Marshalled<T>> T unmarshal(Marshalled.Context context, R marshalled) {
        return marshalled.unmarshal(context);
    }

    public static <T extends Marshallable, R extends Marshalled<T>> void unmarshalAddAll(Marshalled.Context context, R[] source, Collection<T> target) {
        for (int i = 0; i < source.length; i++) {
            target.add(unmarshal(context, source[i]));
        }
    }
}
