/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.event_bus;

import java.lang.annotation.Annotation;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

/**
 * The base class for all events.
 */
public class Event {

	/**
	 * Whether the event is canceled.
	 * The consequences of canceling an event are implementation specific.
	 */
	private boolean isCanceled = false;

	public boolean isCanceled() {
		return isCanceled;
	}

	public void cancel() {
		isCanceled = true;
	}

	/**
	 * Triggers all methods listening to this event.
	 * @return itself.
	 */
	public Event fire() {
		EventBus.fire(this);
		return this;
	}

	/**
	 * Triggers all listeners associated with this event.
	 */
	public static void fire(Class<? extends Annotation> event) {
		AnnotationEventHandler.triggerEvent(event);
	}

	/**
	 * An event with a typed return value for {@link Event#fire()}.
	 */
	public static class TypedEvent<E extends TypedEvent<E>> extends Event {

		public E fire() {
			return c(super.fire());
		}

	}

}
