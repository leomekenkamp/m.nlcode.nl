package nl.nlcode.marshalling;

import com.fasterxml.jackson.core.JsonProcessingException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

/**
 *
 * @author leo
 */
public class MarshallingTest {

    ObjectMapper objectMapper;

    public MarshallingTest() {
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
                .allowIfSubType("nl.nlcode")
//                .allowIfSubType("java.util.ArrayList")
                .build();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);

    }

    @AfterEach
    public void tearDown() {
    }

    static class Trivial implements Marshallable {

        public static record SaveData0(int id) implements Marshalled<Trivial> {

            @Override
            public void unmarshalInternal(Marshalled.Context context, Trivial target) {
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
        Marshalled originalData = marshallableContext.marshal(original);

        String json = objectMapper.writeValueAsString(originalData);
        System.out.println(json);
        Trivial.SaveData0 copyData = objectMapper.readValue(json, Trivial.SaveData.class);

        Marshalled.Context marshalledContext = new Marshalled.Context();
        Trivial copy = copyData.unmarshal(marshalledContext);
        assertThat(copy, not(is(original)));
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
            public void unmarshalInternal(Marshalled.Context context, SimpleTypes target) {
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
        Marshalled originalData = marshallableContext.marshal(original);

        String json = objectMapper.writeValueAsString(originalData);
        System.out.println(json);
        SimpleTypes.SaveData0 copyData = objectMapper.readValue(json, SimpleTypes.SaveData.class);

        Marshalled.Context marshalledContext = new Marshalled.Context();
        SimpleTypes copy = copyData.unmarshal(marshalledContext);
        assertThat(copy, not(is(original)));
        assertThat(copy.i, is(original.i));
        assertThat(copy.s, is(original.s));
        assertThat(copy.dt, is(original.dt));
        assertThat(copy.ai, is(original.ai));
    }

    static class SuperClass implements Marshallable {

        String superString;

        public static record SaveData0(int id, String s) implements Marshalled<SuperClass> {

            @Override
            public void unmarshalInternal(Marshalled.Context context, SuperClass target) {
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
            public void unmarshalInternal(Marshalled.Context context, SubClass target) {
                target.subString = s();
                superData.unmarshalInternal(context, target);
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
    public void inheritance_composition_test() throws JsonProcessingException, IOException {
        SubClass original0 = new SubClass();
        original0.superString = "super string";
        original0.subString = "sub string";
        SuperClass original1 = new SuperClass();
        original1.superString = "super only string";

        Marshallable.Context marshallableContext = new Marshallable.Context();
        Marshalled originalData0 = marshallableContext.marshal(original0);
        Marshalled originalData1 = marshallableContext.marshal(original1);

        ArrayList wrapper = new ArrayList();
        wrapper.add(originalData0);
        wrapper.add(originalData1);
        String json = objectMapper.writeValueAsString(wrapper);
        System.out.println(json);
        ArrayList wrapperCopy = objectMapper.readValue(json, ArrayList.class);
        SubClass.SaveData0 copyData0 = (SubClass.SaveData) wrapper.get(0);
        SuperClass.SaveData0 copyData1 = (SuperClass.SaveData) wrapper.get(1);

        Marshalled.Context marshalledContext = new Marshalled.Context();
        SubClass copy0 = copyData0.unmarshal(marshalledContext);
        assertThat(copy0.getClass(), is(original0.getClass()));
        assertThat(copy0, not(is(original0)));
        assertThat(copy0.superString, is(original0.superString));
        assertThat(copy0.subString, is(original0.subString));

        SuperClass copy1 = copyData1.unmarshal(marshalledContext);
        assertThat(copy1.getClass(), is(original1.getClass()));
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
        Marshalled originalData0 = marshallableContext.marshal(original0);
        Marshalled originalData1 = marshallableContext.marshal(original1);
        Marshalled originalData0Ref = marshallableContext.marshal(original0);
        Marshalled originalData1Ref = marshallableContext.marshal(original1);

        ArrayList<Marshalled> wrapper = new ArrayList<>();
        wrapper.add(originalData0);
        wrapper.add(originalData1);
        wrapper.add(originalData0Ref);
        wrapper.add(originalData1Ref);
        String json = objectMapper.writeValueAsString(wrapper);
        System.out.println(json);
        ArrayList wrapperCopy = objectMapper.readValue(json, ArrayList.class);
        SubClass.SaveData0 copyData0 = (SubClass.SaveData) wrapper.get(0);
        SuperClass.SaveData0 copyData1 = (SuperClass.SaveData) wrapper.get(1);
        Marshalled copyData0Ref = wrapper.get(2);
        Marshalled copyData1Ref = wrapper.get(3);

        Marshalled.Context marshalledContext = new Marshalled.Context();

        SubClass copy0 = copyData0.unmarshal(marshalledContext);
        SuperClass copy1 = copyData1.unmarshal(marshalledContext);
        SubClass copy0ref = (SubClass) copyData0Ref.unmarshal(marshalledContext);
        SuperClass copy1ref = (SuperClass) copyData1Ref.unmarshal(marshalledContext);

        assertThat(copy0, sameInstance(copy0ref));
        assertThat(copy1, sameInstance(copy1ref));
    }

    public static class SelfReference implements Marshallable {

        SelfReference other;

        public static record SaveData0(int id, Marshalled<SelfReference> other) implements Marshalled<SelfReference> {

            @Override
            public void unmarshalInternal(Context context, SelfReference target) {
                target.other = other().unmarshal(context);
            }

            @Override
            public SelfReference createMarshallable() {
                return new SelfReference();
            }

        }

        @Override
        public Marshalled marshalInternal(int id, Context context) {
            return new SaveData0(id, context.marshal(other));
        }

        
    }

    @Test
    public void self_reference() throws JsonProcessingException {
        SelfReference ref = new SelfReference();
        ref.other = ref;

        Marshallable.Context marshallableContext = new Marshallable.Context();
        Marshalled originalData = marshallableContext.marshal(ref);

        String json = objectMapper.writeValueAsString(originalData);
        System.out.println(json);
        SelfReference.SaveData0 copyData = objectMapper.readValue(json, SelfReference.SaveData.class);

        Marshalled.Context marshalledContext = new Marshalled.Context();

        SelfReference copyRef = copyData.unmarshal(marshalledContext);
        assertThat(copyRef.other, sameInstance(copyRef));

    }
}
