package nl.nlcode.m.engine;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map-type lookup.
 *
 * @author leo
 */
public class Lookup<T extends Lookup.Named> implements Iterable<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Comparator<Lookup.Named> NAME_COMPARATOR = new Comparator<>() {
        @Override
        public int compare(Lookup.Named o1, Lookup.Named o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private static final Set<String> RESERVED_NAMES = Set.of(
            "device",
            "exit",
            "help",
            "?",
            "!",
            ".",
            "/",
            "list",
            "new",
            "project",
            "print",
            "verbosity",
            "system"
    );

    /**
     * Items that can be looked up from a {@code Lookup}. Implementations must
     * ensure that an instance can only be linked to one {@code Lookup} at a
     * time.
     * <p>
     * The name of an instance can only change during its lifetime, when the
     * implementation calls {@code beforeRenameTo(String)} successfully just
     * before the actual name change.
     */
    public interface Named<T extends Lookup.Named> {

        /**
         * The name that is used (will be used) as lookup key.
         *
         * @return unique name withing the containing lookup
         */
        String getName();

        void setName(String name);

        /**
         * Implementations MUST call this method just before they change the
         * actual name returned by getName(), or call {@code verifyName(String) on the value returned by {@code getLookup}.
         *
         * @param unique new name withing the containing lookup
         */
        default void beforeRenameTo(String newName) {
            Lookup lookup = getLookup();
            if (lookup != null) {
                lookup.verifyName(newName);
            }
        }

        /**
         *
         * @return
         */
        Lookup<T> getLookup();

        default String getHumanTypeName() {
            String key = getClass().getName();
            if (I18n.msg().containsKey(key)) {
                return I18n.msg().getString(key);
            } else {
                return getSystemTypeName();
            }
        }

        default String getSystemTypeName() {
            return getClass().getSimpleName();
        }
    }

    private List<T> synchronizedBackingList;

    public static <T extends Lookup.Named> Lookup<T> createWithSynchronizedBacking(List<T> synchronizedBackingList) {
        Lookup<T> result = new Lookup<>();
        result.synchronizedBackingList = synchronizedBackingList;
        return result;
    }

    public static <T extends Lookup.Named> Lookup<T> create() {
        return createWithSynchronizedBacking(Collections.synchronizedList(new ArrayList<T>()));
    }

    private Lookup() {
    }

    static void verifyFormat(String name) {
        LOGGER.debug("verifying name {}", name);
        if (name.isBlank()) {
            throw new FunctionalException("name must contain at least one non-whitespace character");
        }
        if (name.length() > 64) {
            throw new FunctionalException("name size cannot exceed 64 characters");
        }
    }

    static void verifyReservedName(String name) {
        if (RESERVED_NAMES.contains(name)) {
            throw new FunctionalException("name is reserved");
        }
    }

    public String suggestName(String base) {
        HashSet<String> names = new HashSet<>();
        synchronized (synchronizedBackingList) {
            for (T t : synchronizedBackingList) {
                names.add(t.getName());
            }
        }
        for (int i = 0; i < 1000; i++) {
            String proposal = base + "_" + i;
            if (!names.contains(proposal)) {
                return proposal;
            }
        }
        throw new FunctionalException("there are too many names starting with <" + base + ">");
    }

    public void verifyName(String name) {
        verifyName(name, () -> {
        });
    }

    public void verifyName(String name, Runnable runWhenAllowed) {
        verifyFormat(name);
        verifyReservedName(name);
        synchronized (synchronizedBackingList) {
            for (T t : synchronizedBackingList) {
                if (t.getName().equals(name)) {
                    throw new FunctionalException("name already exists: <" + name + ">");
                }
            }
            runWhenAllowed.run();
        }
    }

    public void renamed(T item) {
        if (synchronizedBackingList.contains(item)) {
            // The backingList may just very well have some sort of observer mechanism. That mechanism
            // will probably be limited to changes in the list itself; changes in the contained objects
            // will probably not trigger any listeners. Since we assume that the {@code name} if the item
            // will be used in certain ui-lists, we forcibly change the list when a name changes.
            // We do not know of the sort down below forces that change, so we force it here
            synchronizedBackingList.sort(NAME_COMPARATOR);
            synchronizedBackingList.add(synchronizedBackingList.remove(0));
        }
    }

    public void add(T item) {
        synchronized (synchronizedBackingList) {
            if (item.getName() == null) {
                item.setName(suggestName(item.getHumanTypeName()));
            } else {
                verifyName(item.getName());
            }
            synchronizedBackingList.add(item);
            synchronizedBackingList.sort(NAME_COMPARATOR);
        }
    }

    /**
     * Makes item unavailable for lookup.
     *
     * @param item
     */
    public boolean remove(T item) {
        return synchronizedBackingList.remove(item);
    }

    public T get(String name) {
        synchronized (synchronizedBackingList) {
            for (T item : synchronizedBackingList) {
                if (item.getName().equals(name)) {
                    return item;
                }
            }
        }
        return null;
    }

    public Iterator<T> iterator() {
        return synchronizedBackingList.iterator();
    }

    public int size() {
        return synchronizedBackingList.size();
    }
}
