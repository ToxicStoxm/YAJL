package com.toxicstoxm.YAJL.tools;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Utility class for handling string operations, primarily for converting objects
 * (including arrays, collections, and maps) into readable string representations.
 * <p>
 * This class ensures proper formatting of nested structures and detects circular
 * references in collections and maps to prevent infinite recursion.
 * </p>
 */
public class StringTools {

    /**
     * Computes a string representation of an object, supporting arrays, collections,
     * and maps while detecting circular references.
     *
     * @param o The object to convert to a string.
     * @return A string representation of the object.
     */
    public static String computeToString(Object o) {
        if (o == null) return "null";

        if (o.getClass().isArray()) {
            switch (o) {
                case Object[] objects -> {
                    return "Array" + Arrays.deepToString(objects);
                }
                case int[] ints -> {
                    return "int[] " + Arrays.toString(ints);
                }
                case long[] longs -> {
                    return "long[] " + Arrays.toString(longs);
                }
                case double[] doubles -> {
                    return "double[] " + Arrays.toString(doubles);
                }
                case float[] floats -> {
                    return "float[] " + Arrays.toString(floats);
                }
                case char[] chars -> {
                    return "char[] " + Arrays.toString(chars);
                }
                case byte[] bytes -> {
                    return "byte[] " + Arrays.toString(bytes);
                }
                case short[] shorts -> {
                    return "short[] " + Arrays.toString(shorts);
                }
                case boolean[] booleans -> {
                    return "boolean[] " + Arrays.toString(booleans);
                }
                default -> {
                }
            }
        }

        if (o instanceof Collection<?> collection) {
            return computeCollectionToString(collection, new HashSet<>());
        }

        if (o instanceof Map<?, ?> map) {
            return computeMapToString(map, new HashSet<>());
        }

        return Objects.toString(o);
    }

    /**
     * Converts a collection to a string representation, handling nested collections
     * and detecting circular references to prevent infinite recursion.
     *
     * @param collection The collection to convert.
     * @param seen A set tracking already processed objects to detect circular references.
     * @return A formatted string representation of the collection.
     */
    private static @NotNull String computeCollectionToString(Collection<?> collection, @NotNull Set<Object> seen) {
        if (seen.contains(collection)) return "[...] (circular reference detected)";
        seen.add(collection);

        StringBuilder sb = new StringBuilder("Collection[");
        Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object item = iterator.next();
            sb.append(computeToString(item));
            if (iterator.hasNext()) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Converts a map to a string representation, handling nested maps and detecting
     * circular references to prevent infinite recursion.
     *
     * @param map The map to convert.
     * @param seen A set tracking already processed objects to detect circular references.
     * @return A formatted string representation of the map.
     */
    private static @NotNull String computeMapToString(Map<?, ?> map, @NotNull Set<Object> seen) {
        if (seen.contains(map)) return "{...} (circular reference detected)";
        seen.add(map);

        StringBuilder sb = new StringBuilder("Map{");
        Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<?, ?> entry = iterator.next();
            sb.append(computeToString(entry.getKey())).append(" -> ").append(computeToString(entry.getValue()));
            if (iterator.hasNext()) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

}
