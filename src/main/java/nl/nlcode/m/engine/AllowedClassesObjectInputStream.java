package nl.nlcode.m.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author leo
 */
public final class AllowedClassesObjectInputStream extends ObjectInputStream {

    private static final boolean LEARN_MODE = false; // always put to FALSE for production

    private static final Logger LOGGER = LoggerFactory.getLogger(AllowedClassesObjectInputStream.class);
    private static Set<String> allowed = new TreeSet<>();

    public AllowedClassesObjectInputStream(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        if (LEARN_MODE) {
            learn(desc);
        }
        if (!allowed.contains(desc.getName())) {
            throw new ClassNotFoundException("You are a fool, you and your <" + desc.getName() + ">. Bugger off.");
        }
        return super.resolveClass(desc);
    }

    private void learn(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        LOGGER.info("new one: <{}>", desc.getName());
        Class<?> resolved = super.resolveClass(desc);
        allow(resolved.getName());
        for (String name : allowed) {
            LOGGER.warn("AllowedClassesObjectInputStream.allow(\"{}\");", name);
        }
    }

    public static void allow(String name) {
        allowed.add(name);
    }

}
