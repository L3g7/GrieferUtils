package dev.l3g7.griefer_utils.file_provider;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.file_provider.impl.FileProviderImpl;
import dev.l3g7.griefer_utils.file_provider.impl.JarFileProviderImpl;
import dev.l3g7.griefer_utils.file_provider.impl.ResourceFileProviderImpl;
import dev.l3g7.griefer_utils.util.Reflection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class FileProvider {

    // The modules shouldn't be loaded when the transformer is called, because LabyMod tries to access minecraft in <cinit>
    private static final ImmutableList<String> LATE_LOAD_PACKAGES = ImmutableList.of("dev.l3g7.griefer_utils.features.modules");
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, Class<?>> classes = new HashMap<>();
    private static FileProviderImpl fileProvider = null;
    private static boolean loadLateLoadPackages = false;

    public static FileProviderImpl getProvider() {
        if(fileProvider == null) {
            for (FileProviderImpl impl : new FileProviderImpl[]{new JarFileProviderImpl(), new ResourceFileProviderImpl()}) {
                if(impl.isAvailable()) {
                    fileProvider = impl;
                    LOGGER.debug("Using " + impl.getClass().getSimpleName() + ", found " + impl.getData().size() + " files");
                    return fileProvider;
                }
            }
            throw new RuntimeException("No available file provider could be found!");
        }

        return fileProvider;
    }

    public static Stream<String> getAllClassNames() {
        return getProvider().getData().keySet().stream()
                .filter(name -> name.endsWith(".class"))
                .map(name -> name.replace('/', '.'))
                .map(name -> name.substring(0, name.lastIndexOf(".")))
                .filter(f -> loadLateLoadPackages || LATE_LOAD_PACKAGES.stream().noneMatch(f::startsWith));
    }

    public static Stream<Class<?>> getClassesInPackage(String pkg) {
        return getAllClassNames()
                .filter(name -> name.startsWith(pkg))
                .map(name -> classes.computeIfAbsent(name, Reflection::loadClass));
    }

    public static Stream<Class<?>> getAllClasses() {
        return getAllClassNames()
                .map(name -> classes.computeIfAbsent(name, Reflection::loadClass));
    }

    public static <A extends Annotation> Stream<Method> getAnnotatedMethods(Class<A> annotationClass) {
        return getAllClasses()
                .map(Class::getDeclaredMethods).flatMap(Arrays::stream)
                .filter(m -> m.isAnnotationPresent(annotationClass));
    }

    public static <A extends Annotation> void callAllAnnotatedMethods(Class<A> annotationClass, Object... args) {
        getAnnotatedMethods(annotationClass)
                .forEach(m -> Reflection.invoke(m, m.getDeclaringClass(), args));
    }

    private static final Map<Class<?>, Object> singletons = new HashMap<>();

    public static void loadLateLoadPackages() {
        loadLateLoadPackages = true;
    }

    public static <T> T getSingleton(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Singleton.class))
            throw new IllegalArgumentException("Class " + clazz.getName() + " is no singleton!");

        return (T) singletons.computeIfAbsent(clazz, c -> Reflection.loadClass(false, c));
    }

}
