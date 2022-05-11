package dev.l3g7.griefer_utils.event.event_bus;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(value = RUNTIME)
@Target(value = METHOD)
public @interface EventListener {

    EventPriority priority() default EventPriority.NORMAL;
    boolean receiveCanceled() default false;

}