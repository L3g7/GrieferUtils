package dev.l3g7.griefer_utils.event.event_bus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * very basic forge-less event handler
 */
public class EventBus {

    private static final Map<Class<?>, List<Listener>[]> listeners = new HashMap<>();

    public static void register(Object object) {
        Class<?> clazz = object instanceof Class<?> ? (Class<?>) object : object.getClass();
        for (Method method : clazz.getMethods())
            registerMethod(object, method);
    }

    @SuppressWarnings("unchecked")
    private static void registerMethod(Object object, Method method) {
        if (method.isAnnotationPresent(EventListener.class)) {

            // Check parameter count and type
            if (method.getParameterCount() != 1)
                throw new IllegalArgumentException("illegal parameter count for method " + method);

            Class<?> eventClass = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(eventClass))
                throw new IllegalArgumentException("illegal parameter type for method " + method);

            // Add new listener to listenerList
            EventListener eventListener = method.getAnnotation(EventListener.class);
            int priority = eventListener.priority().ordinal();

            List<Listener>[] listenerLists = listeners.computeIfAbsent(eventClass, v -> new List[EventPriority.values().length]);

            if (listenerLists[priority] == null)
                listenerLists[priority] = new ArrayList<>();

            listenerLists[priority].add(new Listener(method, object, eventListener.receiveCanceled()));
        }
    }

    public static <E extends Event> E post(E event) {
        if (!listeners.containsKey(event.getClass())) // No listeners registered
            return event;

        List<Listener>[] listenerLists = listeners.get(event.getClass());
        for (int i = 0; i < EventPriority.values().length; i++) {
            if (listenerLists[i] == null) // No listeners for this priority
                continue;

            for (Listener listener : listenerLists[i]) {
                try {
                    listener.run(event);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        }
        return event;
    }

    private static class Listener {

        private final Method method;
        private final Object instance;
        private final boolean callIfCanceled;

        public Listener(Method method, Object instance, boolean callIfCanceled) {
            this.method = method;
            this.method.setAccessible(true);
            this.instance = instance;
            this.callIfCanceled = callIfCanceled;
        }

        public void run(Event arg) {
            if (callIfCanceled || !(arg instanceof Event.Cancelable) || !((Event.Cancelable) arg).isCanceled()) {
                try {
                    method.invoke(instance, arg);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

    }

}

