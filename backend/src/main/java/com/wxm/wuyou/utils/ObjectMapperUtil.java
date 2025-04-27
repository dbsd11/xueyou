package com.wxm.wuyou.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ObjectMapperUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 优化选项：在序列化时忽略未知属性
        objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        // 优化选项：在序列化时写入null值到JSON中
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        // 优化选项：在序列化时对空的JavaBean抛出异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 优化选项：在序列化日期时以时间戳形式写入
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        // 优化选项：在序列化Duration时以时间戳形式写入
        objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, true);
        // 优化选项：在反序列化时对null值尝试转换为原始类型抛出异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

        // 支持LocalDateTime的处理
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ISO_LOCAL_TIME));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        objectMapper.registerModule(javaTimeModule);
    }

    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return objectMapper.createArrayNode();
    }

    public static <T> List<T> strToList(String str, Class<T> tClass) {
        try {
            CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, tClass);
            return objectMapper.readValue(str, collectionType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public static <T> String serialize(T object) {
        if (object == null) {
            return null;
        }

        String result = null;
        try {
            result = objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static <T> T deserialize(String jsonStr, Class<T> cls) {
        T result = null;
        try {
            result = objectMapper.readValue(jsonStr, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static <T> T deserialize(String jsonStr, TypeReference<T> typeReference) {
        T result = null;
        try {
            result = objectMapper.readValue(jsonStr, typeReference);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JsonNode deserializeTree(String jsonStr) {
        try {
            return objectMapper.readTree(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Map values from a MultiValueMap retaining all values into an Iterable.
     *
     * @param multiMap
     * @return
     */
    public static <K, V> Map<K, Iterable<V>> mapFrom(MultiValueMap<K, V> multiMap) {
        Map<K, Iterable<V>> map = new LinkedHashMap<>();
        if (multiMap != null) {
            multiMap.forEach(map::put);
        }
        return map;
    }
}
