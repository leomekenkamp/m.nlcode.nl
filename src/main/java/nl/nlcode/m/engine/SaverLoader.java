package nl.nlcode.m.engine;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import nl.nlcode.m.engine.Project.SaveData0;
import nl.nlcode.marshalling.MarshalHelper;
import nl.nlcode.marshalling.Marshallable;
import nl.nlcode.marshalling.Marshalled;

/**
 *
 * @author leo
 */
public class SaverLoader {

    private static final String PROGRAM_ID = "nl.nlcode.m";
    private static final String VERSION = "0";
    private static final String TYPE_JSON = "json";
    private static final String TYPE_XML = "xml";
    private static final String ENCODING_TXT = "txt";

    private static class MyTypeIdResolver extends TypeIdResolverBase {

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
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.NAME;
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
                result = "!@ldt";
            }
            if (result.startsWith("nl.nlcode.marshalling.")) {
                result = "!" + result.substring("nl.nlcode.marshalling.".length());
            } else if (result.startsWith("nl.nlcode.m.engine.")) {
                result = "_" + result.substring("nl.nlcode.m.engine.".length());
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
                if (id.equals("!@ldt")) {
                    id = "java.time.LocalDateTime";
                }
                if (id.startsWith("_")) {
                    id = "nl.nlcode.m.engine." + id.substring(1);
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

    private static ObjectMapper createObjectMapper() {
        ObjectMapper result = new ObjectMapper();
        result.registerModule(new JavaTimeModule());
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("nl.nlcode.m.engine.")
                .allowIfSubType("nl.nlcode.marshalling.")
                .allowIfSubType("java.util.ArrayList")
                .allowIfSubType("java.time.LocalDateTime")
                .allowIfSubType("java.util.HashMap")
                .allowIfSubTypeIsArray()
                .build();
        result.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, JsonTypeInfo.As.WRAPPER_OBJECT); //, JsonTypeInfo.As.WRAPPER_OBJECT
        TypeResolverBuilder<?> typeResolver = new StdTypeResolverBuilder();
        TypeFactory typeFactory = result.getTypeFactory();
        JavaType baseType = typeFactory.constructFromCanonical(Object.class.getName());
        typeResolver.init(JsonTypeInfo.Id.CLASS, new MyTypeIdResolver(baseType, typeFactory));
        typeResolver.inclusion(JsonTypeInfo.As.WRAPPER_OBJECT);
        result.setDefaultTyping(typeResolver);

        return result;
    }

    public Project load(Path path) throws FileNotFoundException, IOException {
        InputStream in = Files.newInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        if (!reader.readLine().equals(PROGRAM_ID)) {
            throw new IOException("incompatible file");
        }
        if (!reader.readLine().equals(VERSION)) {
            throw new IOException("incompatible file version");
        }
        String encoding = reader.readLine();
        if (!encoding.equals(ENCODING_TXT)) {
            throw new IOException("incompatible file encoding");
        }
        String type = reader.readLine();
        if (type.equals(TYPE_JSON)) {
            return loadJson(reader);
        } else if (type.equals(TYPE_XML)) {
            throw new IOException("not yet...");
        } else {
            throw new IOException("incompatible file type");
        }
    }

    private Project loadJson(Reader in) throws IOException {
        ObjectMapper objectMapper = createObjectMapper();
        SaveData0 saveData = objectMapper.readValue(in, Project.SaveData0.class);
        Marshalled.Context context = new Marshalled.Context();
        Project result = MarshalHelper.unmarshal(context, saveData);
        return result;
    }

    public void save(Project project) throws FileNotFoundException, IOException { //TODO backup file
        FileOutputStream out = new FileOutputStream(project.getPath().toFile());
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        writer.println(PROGRAM_ID);
        writer.println(VERSION);
        writer.println(ENCODING_TXT);
        writer.println(TYPE_JSON);
        writer.flush();
        ObjectMapper objectMapper = createObjectMapper();

        Marshallable.Context context = new Marshallable.Context();
        Marshalled marshalled = MarshalHelper.marshal(context, project);

        objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValue(out, marshalled);
    }
}
