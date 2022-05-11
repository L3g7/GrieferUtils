package dev.l3g7.griefer_utils.event;

import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.OnEnable;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Consumer;

@Singleton
public class ListenerRegistrar {

    @OnEnable
    public static void registerListeners() {
        registerListenersByAnnotation(SubscribeEvent.class, MinecraftForge.EVENT_BUS::register);
        registerListenersByAnnotation(EventListener.class, EventBus::register);

        FileProvider.getAllClasses()
                .filter(c -> !c.isAnnotationPresent(Singleton.class))
                .filter(c -> Arrays.stream(c.getDeclaredMethods())
                        .filter(m -> Modifier.isStatic(m.getModifiers()))
                        .anyMatch(m -> m.isAnnotationPresent(EventListener.class))
                     || Arrays.stream(c.getMethods())
                        .filter(m -> Modifier.isStatic(m.getModifiers()))
                        .anyMatch(m -> m.isAnnotationPresent(EventListener.class)))
                .forEach(EventBus::register);
    }

    private static void registerListenersByAnnotation(Class<? extends Annotation> annotationClass, Consumer<Object> registerMethod) {
        FileProvider.getAllClasses()
                .filter(c -> c.isAnnotationPresent(Singleton.class))
                .filter(c -> Arrays.stream(c.getDeclaredMethods()).anyMatch(m -> m.isAnnotationPresent(annotationClass))
                        || Arrays.stream(c.getMethods()).anyMatch(m -> m.isAnnotationPresent(annotationClass)))
                .map(FileProvider::getSingleton)
                .forEach(registerMethod);
    }

}
