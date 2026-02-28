package com.toxicstoxm.YAJL.util.tools;

import com.toxicstoxm.YAJL.core.Logger;
import com.toxicstoxm.YAJL.core.LoggerManager;
import com.toxicstoxm.YAJL.errorhandling.ClassLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClassTools {
    private static final ClassLoader[] classLoaders = new ClassLoader[]{
            ClassLoader.getPlatformClassLoader(),
            ClassLoader.getSystemClassLoader()
    };

    public static @NotNull Deque<Throwable> getExceptionChain(@NotNull Throwable throwable) {
        Deque<Throwable> deque = new ArrayDeque<>();
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            deque.addLast(t);
        }
        return deque;
    }

    private static final ConcurrentMap<String, Optional<Class<?>>> CLASS_CACHE =
            new ConcurrentHashMap<>();

    public static @Nullable Class<?> getClass(@NotNull String binaryName) {
        return CLASS_CACHE
                .computeIfAbsent(binaryName, name -> {
                    for (ClassLoader classLoader : classLoaders) {
                        try {
                            return Optional.of(classLoader.loadClass(name));
                        } catch (SecurityException | IllegalStateException | ClassNotFoundException ignored) {}
                    }
                    return Optional.empty();
                })
                .orElse(null);
    }


    public static @Nullable Class<?> getClassFromTraceElement(@NotNull StackTraceElement element) {
        Class<?> clazz = getClass(element.getClassName());

        if (clazz == null) {
            LoggerManager.internalLog("Failed to resolve class \"" + element.getClassName() + "\" from stack trace via known ClassLoaders");
        }

        return clazz;
    }

    private static final Map<Class<?>, Optional<Field>> LOGGER_FIELD_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static @Nullable Logger getLoggerFromClass(@NotNull Class<?> clazz) {
        Optional<Field> cached;

        synchronized (LOGGER_FIELD_CACHE) {
            cached = LOGGER_FIELD_CACHE.get(clazz);
            if (cached == null) {
                cached = findLoggerField(clazz);
                LOGGER_FIELD_CACHE.put(clazz, cached);
            }
        }

        if (cached.isEmpty()) {
            LoggerManager.internalLog("No ClassLogger was found in class \"" + clazz.getName() + "\"");
            return null;
        }

        Field field = cached.get();

        try {
            return (Logger) field.get(null);
        } catch (IllegalAccessException | ClassCastException | ExceptionInInitializerError e) {
            LoggerManager.internalLog("Failed to access logger field \"" + field.getName() + "\" in class \"" + clazz.getName() + "\"");
            return null;
        }
    }

    private static Optional<Field> findLoggerField(@NotNull Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();

        Field namedCandidate = null;

        for (Field field : fields) {

            // Must be static
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            // Must extend or implement Logger
            if (!Logger.class.isAssignableFrom(field.getType())) {
                continue;
            }

            // 1. Annotation has highest priority
            if (field.isAnnotationPresent(ClassLogger.class)) {
                field.setAccessible(true);
                LoggerManager.internalLog("Found annotated logger \"" + field.getName() + "\" in class \"" + clazz.getName() + "\"");
                return Optional.of(field);
            }

            // 2. Naming convention fallback
            String name = field.getName();
            if (name.equalsIgnoreCase("logger") || name.equalsIgnoreCase("classLogger")) {
                namedCandidate = field;
            }
        }

        if (namedCandidate != null) {
            namedCandidate.setAccessible(true);
            LoggerManager.internalLog("Found logger by naming convention \"" + namedCandidate.getName() + "\" in class \"" + clazz.getName() + "\"");
            return Optional.of(namedCandidate);
        }

        return Optional.empty();
    }

}
