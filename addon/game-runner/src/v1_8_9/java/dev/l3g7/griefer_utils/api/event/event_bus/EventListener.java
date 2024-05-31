/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.event.event_bus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener {

	/**
	 * The priority of the event listener.
	 * Listeners with a higher priority will be called sooner than listeners with a lower priority.
	 */
	Priority priority() default Priority.NORMAL;

	/**
	 * Whether the listener should be triggered for canceled events.
	 */
	boolean receiveCanceled() default false;

	/**
	 * Whether the listener should be triggered if it's defining {@link Disableable} is disabled.
	 * Has no effect if the defining class is not a subclass of {@link Disableable} or the listener is static.
	 */
	boolean triggerWhenDisabled() default false;

}
