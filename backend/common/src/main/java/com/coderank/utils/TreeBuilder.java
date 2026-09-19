package com.coderank.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 通用树形结构构建工具。
 *
 * <p>通过对象的 id 字段、parentId 字段，以及一个 children 赋值器，把平铺列表组装成树。
 * 支持任意实体类型，不依赖具体结构。可通过 {@link Comparator} 指定排序字段，构建时对根节点和每一层 children 排序。</p>
 *
 * <pre>
 * 示例（不带排序）：
 * List<MenuVO> tree = TreeBuilder.build(list,
 *         MenuVO::getId,
 *         MenuVO::getParentId,
 *         MenuVO::setChildren,
 *         0L);
 *
 * 示例（按 sort 升序，null 排最后）：
 * List<MenuVO> tree = TreeBuilder.build(list,
 *         MenuVO::getId,
 *         MenuVO::getParentId,
 *         MenuVO::setChildren,
 *         0L,
 *         Comparator.comparing(MenuVO::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
 * </pre>
 */
public final class TreeBuilder {

    private TreeBuilder() {
    }

    /**
     * 构建树（保持传入列表顺序，不额外排序）。
     *
     * @param list            平铺的节点列表
     * @param idGetter        取节点 id
     * @param parentIdGetter  取节点父 id
     * @param childrenSetter  设置节点的子节点列表（一般是 setChildren）
     * @param rootId          根节点标识：parentId 等于该值（或为 null、或指向不存在的父节点）视为根
     * @param <T>             节点类型
     * @return 根节点列表（已嵌套 children）
     */
    public static <T> List<T> build(List<T> list,
                                    Function<T, ?> idGetter,
                                    Function<T, ?> parentIdGetter,
                                    BiConsumer<T, List<T>> childrenSetter,
                                    Object rootId) {
        return build(list, idGetter, parentIdGetter, childrenSetter, rootId, null);
    }

    /**
     * 构建树，并按指定比较器排序。
     *
     * @param list            平铺的节点列表
     * @param idGetter        取节点 id
     * @param parentIdGetter  取节点父 id
     * @param childrenSetter  设置节点的子节点列表（一般是 setChildren）
     * @param rootId          根节点标识：parentId 等于该值（或为 null、或指向不存在的父节点）视为根
     * @param comparator      排序比较器；对根节点和每一层 children 生效；为 null 时保持传入顺序
     * @param <T>             节点类型
     * @return 根节点列表（已嵌套 children）
     */
    public static <T> List<T> build(List<T> list,
                                    Function<T, ?> idGetter,
                                    Function<T, ?> parentIdGetter,
                                    BiConsumer<T, List<T>> childrenSetter,
                                    Object rootId,
                                    Comparator<T> comparator) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // id -> 节点 索引
        Map<Object, T> idMap = new LinkedHashMap<>();
        for (T node : list) {
            idMap.put(idGetter.apply(node), node);
        }

        // 收集根节点
        List<T> roots = new ArrayList<>();
        Set<Object> isParent = new HashSet<>();
        for (T node : list) {
            Object pid = parentIdGetter.apply(node);
            boolean isRoot = (pid == null) || Objects.equals(pid, rootId) || !idMap.containsKey(pid);
            if (isRoot) {
                roots.add(node);
            } else {
                isParent.add(pid);
            }
        }
        if (comparator != null) {
            roots.sort(comparator);
        }

        // 父 id -> 直接子节点列表
        Map<Object, List<T>> childMap = new LinkedHashMap<>();
        for (T node : list) {
            Object pid = parentIdGetter.apply(node);
            if (pid == null || Objects.equals(pid, rootId)) {
                continue;
            }
            childMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(node);
        }

        // 递归为每个根节点填充 children
        for (T root : roots) {
            fillChildren(root, idGetter, childMap, childrenSetter, comparator);
        }
        return roots;
    }

    /**
     * 递归填充节点的 children，并继续下沉。
     */
    private static <T> void fillChildren(T parent,
                                         Function<T, ?> idGetter,
                                         Map<Object, List<T>> childMap,
                                         BiConsumer<T, List<T>> childrenSetter,
                                         Comparator<T> comparator) {
        Object id = idGetter.apply(parent);
        List<T> children = childMap.get(id);
        if (children != null && !children.isEmpty()) {
            if (comparator != null) {
                children.sort(comparator);
            }
            for (T child : children) {
                fillChildren(child, idGetter, childMap, childrenSetter, comparator);
            }
            childrenSetter.accept(parent, children);
        }
    }
}
