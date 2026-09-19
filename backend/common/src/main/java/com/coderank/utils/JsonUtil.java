package com.coderank.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.coderank.enums.ErrorCode;
import com.coderank.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类（基于 Jackson）。
 *
 * <p>统一封装对象与 JSON 字符串之间的转换，以及常见 JSON 节点操作。
 * 采用静态单例 {@link ObjectMapper}（线程安全），配置了忽略未知字段、空值不输出等常见行为。</p>
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            // 序列化：null 值不输出，使 JSON 更简洁
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            // 反序列化：忽略对象中不存在的字段，避免报错
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // 反序列化：忽略多余的空值/冲突，提升健壮性
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false)
            // 序列化：日期统一时间戳输出（可按需关闭，改需调整）
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonUtil() {
    }

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param value 目标对象
     * @return JSON 字符串
     */
    public static String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * 将对象序列化为格式化的 JSON 字符串（美化缩进）。
     *
     * @param value 目标对象
     * @return 美化后的 JSON 字符串
     */
    public static String toJsonPretty(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型对象。
     *
     * @param json       JSON 字符串
     * @param targetType 目标类型
     * @param <T>        targetType 的类型
     * @return 目标对象
     */
    public static <T> T fromJson(String json, Class<T> targetType) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, targetType);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为泛型对象（如集合、Map 等复杂结构）。
     *
     * @param json         JSON 字符串
     * @param targetType  目标类型引用，示例：{@code new TypeReference<List<Long>>() {}}
     * @param <T>          targetType 的类型
     * @return 目标对象
     */
    public static <T> T fromJson(String json, TypeReference<T> targetType) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, targetType);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * 将 JSON 字符串解析为 {@link JsonNode} 通用节点，便于动态读写。
     *
     * @param json JSON 字符串
     * @return JSON 节点
     */
    public static JsonNode parse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * 将 JSON 字符串转为 Map。
     *
     * @param json JSON 字符串
     * @return Map，key 为字段名
     */
    public static Map<String, Object> toMap(String json) {
        return fromJson(json, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 将 JSON 数组字符串转为 List。
     *
     * @param json       JSON 数组字符串
     * @param targetType 元素类型
     * @param <T>        元素类型对应的泛型
     * @return 元素列表
     */
    public static <T> List<T> toList(String json, Class<T> targetType) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(
                    json,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, targetType));
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    /**
     * 判断字符串是否为合法的 JSON。
     *
     * @param json 字符串
     * @return 合法返回 true，否则返回 false
     */
    public static boolean isJson(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }
        try {
            MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 从 JSON 字符串中按路径提取节点值，路径示例：{@code data.user.name}。
     *
     * @param json JSON 字符串
     * @param path 以点号分隔的路径，空或 null 时返回根节点
     * @return 对应节点的字符串值，不存在时返回 null
     */
    public static String getByPath(String json, String path) {
        JsonNode node = parse(json);
        return getByPath(node, path);
    }

    /**
     * 从 {@link JsonNode} 中按路径提取节点值。
     *
     * @param node 根节点
     * @param path 以点号分隔的路径，空或 null 时返回节点本身
     * @return 对应节点的字符串值，不存在时返回 null
     */
    public static String getByPath(JsonNode node, String path) {
        if (node == null) {
            return null;
        }
        if (path == null || path.isBlank()) {
            return node.asText();
        }
        JsonNode current = node;
        for (String segment : path.split("\\.")) {
            if (current == null || !current.has(segment)) {
                return null;
            }
            current = current.get(segment);
        }
        return current == null || current.isNull() ? null : current.asText();
    }

    /**
     * 创建空的 JSON 对象节点，便于动态构造。
     *
     * @return 空对象节点
     */
    public static ObjectNode createObjectNode() {
        return MAPPER.createObjectNode();
    }

    /**
     * 获取底层静态单例 ObjectMapper（用于需要额外自定义配置的场景）。
     *
     * @return ObjectMapper 实例
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
