package dev.l3g7.griefer_utils.util;

import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.*;
import java.util.Arrays;

@SuppressWarnings("unchecked")
public class Reflection {

    public static boolean hasSuperclass(Class<?> clazz, Class<?> superClass) {
        if(clazz == null) return false;
        return clazz.getSuperclass() == superClass || hasSuperclass(clazz.getSuperclass(), superClass);
    }

    public static Class<?> loadClass(String className) {
        if (className.endsWith(".class")) className = className.substring(0, className.lastIndexOf('.'));
        try {
            return Class.forName(className);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Tried to load class (" + className + ")");
        }
    }

    public static <I> I loadClass(Class<I> clazz, Object... params) {
        return loadClass(true, clazz, params);
    }

    public static <I> I loadClass(boolean resolveSingleton, Class<I> clazz, Object... params) {
        if (resolveSingleton && clazz.isAnnotationPresent(Singleton.class))
            return FileProvider.getSingleton(clazz);
        try {
            Constructor<?> c = findConstructorGivenParams(clazz, params);
            if(c == null) throw new RuntimeException(new NoSuchMethodException()); // Escalate, as this shouldn't be possible
            return (I) c.newInstance(params);
        } catch (Throwable e) {
            throw new RuntimeException("Tried to load class (" + resolveSingleton + ", " + clazz + ", " + Arrays.toString(params) + ")", e);
        }
    }

    public static <V> V get(Object target, Class<V> type, String... names) {
        return get(target, names);
    }

    public static <V> V get(Object target, String... names) {
        Field field = findField(target, names);
        if(field == null) throw new RuntimeException(new NoSuchFieldException()); // Escalate, as this shouldn't be possible
        return get(field, target);
    }

    public static <V> V get(Field field, Object target) {
        try {
            checkStaticAccess(field, target);
            field.setAccessible(true);
            return (V) field.get(findTarget(target));
        } catch (Throwable e) {
            throw new RuntimeException("Tried to get field (" + field + ", " + target + ")", e);
        }
    }

    public static void set(Object target, Object value, String... names) {
        set(target, value, findClass(target), names);
    }

    public static void set(Object target, Object value, Class<?> targetClass, String... names) {
        Field field = findField(targetClass, names);
        if(field == null) {
            throw new RuntimeException(new NoSuchFieldException("Tried to set field (" + targetClass + ", " + Arrays.toString(names) + ")")); // Escalate, as this shouldn't be possible
        }
        try {
            checkStaticAccess(field, target);
            field.setAccessible(true);
            boolean isFinal = Modifier.isFinal(field.getModifiers());
            if(isFinal) set(field, field.getModifiers() & ~Modifier.FINAL, "modifiers");
            field.set(findTarget(target), value);
            if(isFinal) set(field, field.getModifiers() | Modifier.FINAL, "modifiers");
        } catch (Throwable e) {
            throw new RuntimeException("Tried to set field (" + target + ", " + value + ", " + targetClass + ", " + Arrays.toString(names) + ", " + field + ")", e); // Escalate
        }
    }

    public static Field findField(Object target, String[] names) {
        Class<?> clazz = findClass(target);
        if (clazz == null) return null;
        for (String name : names) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {

            } catch(Throwable e) {
                throw new RuntimeException("Tried to find field (" + target + ", " + Arrays.toString(names) + ")", e);
            }

        }
        return findField(clazz.getSuperclass(), names);
    }

    public static <V> V invoke(Method method, Object target, Class<V> returnType, Object... params) {
        return invoke(method, target, params);
    }

    public static <V> V invoke(Object target, Object methodName, Class<V> returnType, Object... params) {
        return invoke(findMethodGivenParams(target, createArray(methodName), params), target, params);
    }

    public static <V> V invoke(Object target, Object methodName, Object... params) {
        return invoke(findMethodGivenParams(target, createArray(methodName), params), target, params);
    }

    public static <V> V invoke(Method method, Object target, Object... params) {
        if(method == null) throw new RuntimeException(new NoSuchMethodException()); // Escalate, as this shouldn't be possible
        try {
            if(params.length != method.getParameterCount()) {
                throw new IllegalArgumentException(Arrays.toString(params) + " parameters given, expected " + method.getParameterCount() + " for method " + method);
            }
            checkStaticAccess(method, target);
            method.setAccessible(true);
            return (V) method.invoke(findTarget(target), params);
        } catch (Throwable e) {
            throw new RuntimeException("Tried to invoke method (" + method + ", " + target + ", " + Arrays.toString(params) + ")", e);
        }
    }

    private static Method findMethodGivenParams(Object target, String[] methodNames, Object[] params) {
        Class<?> clazz = findClass(target);
        for (String methodName : methodNames) {
            try {
                mtdLoop:
                for (Method m : clazz.getDeclaredMethods()) {
                    try {
                        if (m.getName().equals(methodName)) {
                            Class<?>[] argTypes = m.getParameterTypes();
                            if (argTypes.length == params.length) {
                                for (int i = 0; i < params.length; i++) {
                                    if (!argTypes[i].isInstance(params[i]) && !ClassUtils.primitiveToWrapper(argTypes[i]).isInstance(params[i])) {
                                        continue mtdLoop;
                                    }
                                }
                                m.setAccessible(true);
                                return m;
                            }
                        }
                    } catch (Throwable e) {
                        throw new RuntimeException("Tried to find method given params (" + target + ", " + Arrays.toString(methodNames) + ", " + Arrays.toString(params) + ")", e);
                    }
                }
            } catch (Throwable e) {
                throw new RuntimeException("Tried to find method given params (" + target + ", " + Arrays.toString(methodNames) + ", " + Arrays.toString(params) + ")", e);
            }
        }
        if (clazz.getSuperclass() != null)
            return findMethodGivenParams(clazz.getSuperclass(), methodNames, params);
        return null;
    }

    private static Constructor<?> findConstructorGivenParams(Class<?> clazz, Object[] params) {
        try {
            mtdLoop:
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                try {
                    Class<?>[] argTypes = c.getParameterTypes();
                    if (argTypes.length == params.length) {
                        for (int i = 0; i < params.length; i++) {
                            if (!argTypes[i].isInstance(params[i]) && !ClassUtils.primitiveToWrapper(argTypes[i]).isInstance(params[i])) {
                                continue mtdLoop;
                            }
                        }
                        c.setAccessible(true);
                        return c;
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Tried to find constructor given params (" + clazz + ", " + Arrays.toString(params) + ")", e);
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Tried to find constructor given params (" + clazz + ", " + Arrays.toString(params) + ")", e);
        }
        if (clazz.getSuperclass() != null)
            return findConstructorGivenParams(clazz.getSuperclass(), params);
        return null;
    }

    // Resolves Singletons
    private static Object findTarget(Object target) {
        if (target instanceof Class<?>) {
            Class<?> c = (Class<?>) target;
            if (c.isAnnotationPresent(Singleton.class))
                return FileProvider.getSingleton(c);
        }
        return target;
    }

    private static Class<?> findClass(Object target) {
        if (target == null) return null;
        if (target instanceof Class<?>) return (Class<?>) target;
        if (target instanceof String) return loadClass((String) target);
        return target.getClass();
    }

    private static void checkStaticAccess(Member member, Object target) {
        if(!Modifier.isStatic(member.getModifiers()) && findTarget(target) == null) {
            throw new IllegalArgumentException("Tried to use " + member + " as static with target " + target + "!");
        }
    }

    /**
     * @return an array given an array, single value or null
     */
    @SuppressWarnings("unchecked")
    private static <T> T[] createArray(Object object) {
        if (object == null) return (T[]) Array.newInstance(String.class, 0);
        if (object.getClass().isArray() && object.getClass().getComponentType() == String.class) {
            return (T[]) object;
        }
        if (object instanceof String) {
            Object array = Array.newInstance(String.class, 1);
            Array.set(array, 0, object);
            return (T[]) array;
        }
        throw new IllegalArgumentException(object.getClass().toString());
    }
}