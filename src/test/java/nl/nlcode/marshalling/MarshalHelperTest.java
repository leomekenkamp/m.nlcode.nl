package nl.nlcode.marshalling;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author leo
 */
public class MarshalHelperTest {

    public static class MyTypeIdResolver extends TypeIdResolverBase {

        private JavaType superType;

        public MyTypeIdResolver(JavaType baseType, TypeFactory typeFactory) {
            super(baseType, typeFactory);
            superType = baseType;
        }

        @Override
        public void init(JavaType baseType) {
            super.init(baseType);
            superType = baseType;
        }

        @Override
        public Id getMechanism() {
            return Id.NAME;
        }

        @Override
        public String idFromValue(Object obj) {
            return idFromValueAndType(obj, obj.getClass());
        }

        @Override
        public String idFromValueAndType(Object obj, Class<?> subType) {
            String result = subType.getName();
            boolean objectArray = subType.isArray() && result.startsWith("[L");
            if (objectArray) {
                result = result.substring("[L".length(), result.length() - 1);
            }
            if (result.equals("java.util.HashMap")) {
                result = "!kv";
            }
            if (result.equals("java.util.ArrayList")) {
                result = "!list";
            }
            if (result.equals("java.time.LocalDateTime")) {
                result = "@";
            }
            if (result.startsWith("nl.nlcode.marshalling.")) {
                result = "!" + result.substring("nl.nlcode.marshalling.".length());
            } else if (result.startsWith("nl.nlcode.")) {
                result = "_" + result.substring("nl.nlcode.".length());
            }
            while (result.contains("$SaveData")) {
                result = result.replace("$SaveData", "@");
            }
            if (result.equals("!Marshallable$Reference")) {
                result = "!ref";
            }
            if (objectArray) {
                result += "[";
            }
            if (subType.isArray()) {
                result = result + "]";
            }
            return result;
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) {
            try {
                boolean objectArray = id.endsWith("[]");

                if (!objectArray && id.endsWith("]")) {
                    id = id.substring(0, id.length() - 1);
                }
                if (id.equals("!ref")) {
                    id = "!Marshallable$Reference";
                }
                if (id.equals("!list")) {
                    id = "java.util.ArrayList";
                }
                if (id.equals("!kv")) {
                    id = "java.util.HashMap";
                }
                if (id.equals("@")) {
                    id = "java.time.LocalDateTime";
                }
                if (id.startsWith("_")) {
                    id = "nl.nlcode." + id.substring(1);
                } else if (id.startsWith("!")) {
                    id = "nl.nlcode.marshalling." + id.substring(1);
                }
                while (id.contains("@")) {
                    id = id.replace("@", "$SaveData");
                }
                if (objectArray) {
                    id = "[L" + id.substring(0, id.length() - "[]".length()) + ";";
                }
                Class<?> subType = Class.forName(id);
                return context.constructSpecializedType(superType, subType);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    ObjectMapper objectMapper;

    public MarshalHelperTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("nl.nlcode.marshalling.")
                .allowIfSubType("java.util.ArrayList")
                .allowIfSubType("java.time.LocalDateTime")
                .allowIfSubTypeIsArray()
                .build();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.WRAPPER_OBJECT); //, JsonTypeInfo.As.WRAPPER_OBJECT

        TypeResolverBuilder<?> typeResolver = new StdTypeResolverBuilder();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        JavaType baseType = typeFactory.constructFromCanonical(Object.class.getName());
        typeResolver.init(JsonTypeInfo.Id.CLASS, new MyTypeIdResolver(baseType, typeFactory));
        typeResolver.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
        //typeResolver.typeProperty("@CLASS");
        objectMapper.setDefaultTyping(typeResolver);
    }

    @AfterEach
    public void tearDown() {
    }

    static class Trivial implements Marshallable {

        public static record SaveData0(int id) implements Marshalled<Trivial> {

            @Override
            public void unmarshalInto(Marshalled.Context context, Trivial target) {
            }

            @Override
            public Trivial createMarshallable() {
                return new Trivial();
            }
        }

        @Override
        public Marshalled marshalInternal(int id, Marshallable.Context context) {
            return new SaveData0(id);
        }

    };

    @Test
    public void trivial_test() throws JsonProcessingException {
        Trivial original = new Trivial();
        Marshallable.Context marshallableContext = new Marshallable.Context();
        Marshalled originalData = MarshalHelper.marshal(marshallableContext, original);

        String json = objectMapper.writeValueAsString(originalData);
        System.out.println(json);
        Trivial.SaveData0 copyData = objectMapper.readValue(json, Trivial.SaveData0.class);

        Marshalled.Context marshalledContext = new Marshalled.Context();
        Trivial copy = MarshalHelper.unmarshal(marshalledContext, copyData);
        MatcherAssert.assertThat(copy, CoreMatchers.not(CoreMatchers.is(original)));
    }

    static class SimpleTypes implements Marshallable {

        String s;
        int i;
        LocalDateTime dt;
        int[] ai;

        public static record SaveData0(
                int id,
                String version,
                String s,
                int i,
                LocalDateTime dt,
                int[] ai) implements Marshalled<SimpleTypes> {

            @Override
            public void unmarshalInto(Marshalled.Context context, SimpleTypes target) {
                target.s = s();
                if (!"0".equals(version())) {
                    throw new IllegalStateException();
                }
                target.i = i();
                target.dt = dt();
                target.ai = ai();
            }

            @Override
            public SimpleTypes createMarshallable() {
                return new SimpleTypes();
            }
        }

        @Override
        public Marshalled marshalInternal(int id, Marshallable.Context context) {
            return new SaveData0(id, "0", s, i, dt, ai);
        }

    };

    @Test
    public void simple_types_test() throws JsonProcessingException {
        SimpleTypes original = new SimpleTypes();
        original.s = "Hello, World!";
        original.i = 42;
        original.dt = LocalDateTime.of(1971, Month.APRIL, 6, 16, 0);
        original.ai = new int[]{3, 2, 1};
        Marshallable.Context marshallableContext = new Marshallable.Context();
        Marshalled originalData = MarshalHelper.marshal(marshallableContext, original);

        String json = objectMapper.writeValueAsString(originalData);
        System.out.println(json);
        SimpleTypes.SaveData0 copyData = objectMapper.readValue(json, SimpleTypes.SaveData0.class);

        Marshalled.Context marshalledContext = new Marshalled.Context();
        SimpleTypes copy = MarshalHelper.unmarshal(marshalledContext, copyData);
        MatcherAssert.assertThat(copy, CoreMatchers.not(CoreMatchers.is(original)));
        MatcherAssert.assertThat(copy.i, CoreMatchers.is(original.i));
        MatcherAssert.assertThat(copy.s, CoreMatchers.is(original.s));
        MatcherAssert.assertThat(copy.dt, CoreMatchers.is(original.dt));
        MatcherAssert.assertThat(copy.ai, CoreMatchers.is(original.ai));
    }

    static class SuperClass implements Marshallable {

        String superString;

        public static record SaveData0(int id, String s) implements Marshalled<SuperClass> {

            @Override
            public void unmarshalInto(Marshalled.Context context, SuperClass target) {
                target.superString = s();
            }

            @Override
            public SuperClass createMarshallable() {
                return new SuperClass();
            }
        }

        @Override
        public Marshalled marshalInternal(int id, Marshallable.Context context) {
            return new SaveData0(id, superString);
        }

    };

    static class SubClass extends SuperClass implements Marshallable {

        String subString;

        public static record SaveData0(int id, String s, Marshalled<SuperClass> superData) implements Marshalled<SubClass> {

            @Override
            public void unmarshalInto(Marshalled.Context context, SubClass target) {
                target.subString = s();
                superData.unmarshalInto(context, target);
            }

            @Override
            public SubClass createMarshallable() {
                return new SubClass();
            }
        }

        @Override
        public Marshalled marshalInternal(int id, Marshallable.Context context) {
            return new SaveData0(id, subString, super.marshalInternal(-1, context));
        }

    };

    @Test
    public void inheritance_composition_test_ArrayList() throws JsonProcessingException, IOException {
        SubClass original0 = new SubClass();
        original0.superString = "super string";
        original0.subString = "sub string";
        SuperClass original1 = new SuperClass();
        original1.superString = "super only string";

        Marshallable.Context marshallableContext = new Marshallable.Context();
        Marshalled originalData0 = MarshalHelper.marshal(marshallableContext, original0);
        Marshalled originalData1 = MarshalHelper.marshal(marshallableContext, original1);

        ArrayList wrapper = new ArrayList();
        wrapper.add(originalData0);
        wrapper.add(originalData1);
        String json = objectMapper.writeValueAsString(wrapper);
        System.out.println(json);
        ArrayList wrapperCopy = objectMapper.readValue(json, ArrayList.class);
        SubClass.SaveData0 copyData0 = (SubClass.SaveData0) wrapper.get(0);
        SuperClass.SaveData0 copyData1 = (SuperClass.SaveData0) wrapper.get(1);

        Marshalled.Context marshalledContext = new Marshalled.Context();
        SubClass copy0 = copyData0.unmarshal(marshalledContext);
        MatcherAssert.assertThat(copy0, CoreMatchers.instanceOf(original0.getClass()));
        MatcherAssert.assertThat(copy0, CoreMatchers.not(CoreMatchers.is(original0)));
        MatcherAssert.assertThat(copy0.superString, CoreMatchers.is(original0.superString));
        MatcherAssert.assertThat(copy0.subString, CoreMatchers.is(original0.subString));

        SuperClass copy1 = copyData1.unmarshal(marshalledContext);
        MatcherAssert.assertThat(copy1, CoreMatchers.instanceOf(original1.getClass()));
        MatcherAssert.assertThat(copy1, CoreMatchers.not(CoreMatchers.is(original1)));
        MatcherAssert.assertThat(copy1.superString, CoreMatchers.is(original1.superString));
    }

    @Test
    public void inheritance_composition_test_array() throws JsonProcessingException, IOException {
        SubClass original0 = new SubClass();
        original0.superString = "super string";
        original0.subString = "sub string";
        SuperClass original1 = new SuperClass();
        original1.superString = "super only string";

        Marshallable.Context marshallableContext = new Marshallable.Context();
        Marshalled originalData0 = MarshalHelper.marshal(marshallableContext, original0);
        Marshalled originalData1 = MarshalHelper.marshal(marshallableContext, original1);

        ArrayList wrapper = new ArrayList();
        Object[] array = new Object[]{originalData0, originalData1};
        wrapper.add(array);
        String json = objectMapper.writeValueAsString(wrapper);
        System.out.println(json);
        ArrayList wrapperCopy = objectMapper.readValue(json, ArrayList.class);
        Object[] arrayCopy = (Object[]) wrapperCopy.get(0);
        SubClass.SaveData0 copyData0 = (SubClass.SaveData0) arrayCopy[0];
        SuperClass.SaveData0 copyData1 = (SuperClass.SaveData0) arrayCopy[1];

        Marshalled.Context marshalledContext = new Marshalled.Context();
        SubClass copy0 = copyData0.unmarshal(marshalledContext);
        assertThat(copy0, instanceOf(original0.getClass()));
        assertThat(copy0, not(is(original0)));
        assertThat(copy0.superString, is(original0.superString));
        assertThat(copy0.subString, is(original0.subString));

        SuperClass copy1 = copyData1.unmarshal(marshalledContext);
        assertThat(copy1, instanceOf(original1.getClass()));
        assertThat(copy1, not(is(original1)));
        assertThat(copy1.superString, is(original1.superString));
    }

    @Test
    public void reference_test() throws JsonProcessingException, IOException {
        SubClass original0 = new SubClass();
        original0.superString = "super string";
        original0.subString = "sub string";
        SuperClass original1 = new SuperClass();
        original1.superString = "super only string";

        Marshallable.Context marshallableContext = new Marshallable.Context();
        Marshalled originalData0 = MarshalHelper.marshal(marshallableContext, original0);
        Marshalled originalData1 = MarshalHelper.marshal(marshallableContext, original1);
        Marshalled originalData0Ref = MarshalHelper.marshal(marshallableContext, original0);
        Marshalled originalData1Ref = MarshalHelper.marshal(marshallableContext, original1);

        ArrayList<Marshalled> wrapper = new ArrayList<>();
        wrapper.add(originalData0);
        wrapper.add(originalData1);
        wrapper.add(originalData0Ref);
        wrapper.add(originalData1Ref);
        String json = objectMapper.writeValueAsString(wrapper);
        System.out.println(json);
        ArrayList wrapperCopy = objectMapper.readValue(json, ArrayList.class);
        SubClass.SaveData0 copyData0 = (SubClass.SaveData0) wrapper.get(0);
        SuperClass.SaveData0 copyData1 = (SuperClass.SaveData0) wrapper.get(1);
        Marshalled copyData0Ref = wrapper.get(2);
        Marshalled copyData1Ref = wrapper.get(3);

        Marshalled.Context marshalledContext = new Marshalled.Context();

        SubClass copy0 = MarshalHelper.unmarshal(marshalledContext, copyData0);
        SuperClass copy1 = MarshalHelper.unmarshal(marshalledContext, copyData1);
        SubClass copy0ref = (SubClass) MarshalHelper.unmarshal(marshalledContext, copyData0Ref);
        SuperClass copy1ref = (SuperClass) MarshalHelper.unmarshal(marshalledContext, copyData1Ref);

        assertThat(copy0, sameInstance(copy0ref));
        assertThat(copy1, sameInstance(copy1ref));
    }

    public static class SelfReference implements Marshallable {

        SelfReference other;

        public static record SaveData0(int id, Marshalled<SelfReference> other) implements Marshalled<SelfReference> {

            @Override
            public void unmarshalInto(Context context, SelfReference target) {
                target.other = other().unmarshal(context);
            }

            @Override
            public SelfReference createMarshallable() {
                return new SelfReference();
            }

        }

        @Override
        public Marshalled marshalInternal(int id, Context context) {
            return new SaveData0(id, MarshalHelper.marshal(context, other));
        }

    }

    @Test
    public void self_reference() throws JsonProcessingException {
        SelfReference ref = new SelfReference();
        ref.other = ref;

        Marshallable.Context marshallableContext = new Marshallable.Context();
        Marshalled originalData = MarshalHelper.marshal(marshallableContext, ref);

        String json = objectMapper.writeValueAsString(originalData);
        System.out.println(json);
        SelfReference.SaveData0 copyData = objectMapper.readValue(json, SelfReference.SaveData0.class);

        Marshalled.Context marshalledContext = new Marshalled.Context();

        SelfReference copyRef = MarshalHelper.unmarshal(marshalledContext, copyData);
        assertThat(copyRef.other, sameInstance(copyRef));

    }
}
