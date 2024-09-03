/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.event_bus;

import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.functions.Predicate;
import dev.l3g7.griefer_utils.core.api.util.LambdaUtil;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

class EventBus {

	/**
	 * The listeners associated with each event, sorted by priority.
	 */
	static final Map<Class<?>, Listener.ListenerList> events = Collections.synchronizedMap(new ConcurrentHashMap<>());

	/**
	 * Triggers all listeners annotated with an event handler targeting the given event.
	 */
	static void fire(Event event) {
		EventRegisterer.handleLazyRegistrations(event.getClass().getName());

		Listener.ListenerList list = events.get(event.getClass());
		if (list == null)
			return;

		for (Listener listener : list) {
			try {
				listener.consumer.accept(event);
			} catch (Throwable t) {
				BugReporter.reportError(t);
			}
		}
	}

	/**
	 * Registers the given method.
	 */
	static void registerMethod(Object owner, Method method) {

		// Check parameter type
		Class<?> eventClass = method.getParameterTypes()[0];
		if (!Event.class.isAssignableFrom(eventClass))
			throw new IllegalArgumentException("illegal parameter type for method " + method);

		// Extract listener metadata
		EventListener listener = method.getAnnotation(EventListener.class);
		int priority = listener.priority().ordinal();
		boolean receiveCanceled = listener.receiveCanceled();
		boolean mustBeEnabled = !listener.triggerWhenDisabled() && owner instanceof Disableable;

		// Create listener list
		Listener.ListenerList listeners = events.computeIfAbsent(eventClass, e -> new Listener.ListenerList());

		// Create type parameter check
		Predicate<Event> typeCheck;

		Type eventType = method.getGenericParameterTypes()[0];
		if (!(eventType instanceof ParameterizedType)) {
			// event doesn't have type parameters
			typeCheck = event -> true;
		} else {
			Type[] typeParams = ((ParameterizedType) eventType).getActualTypeArguments();
			Predicate<?>[] typeChecks = new Predicate[typeParams.length];

			// Create check for every type argument
			for (int i = 0; i < typeParams.length; i++) {
				Type typeParam = typeParams[i];
				if (typeParam instanceof Class) {
					Class<?> requiredClass = (Class<?>) typeParam;
					Field definingField = findGenericDefiningField(eventClass, eventClass.getTypeParameters()[i]);
					definingField.setAccessible(true);
					// Check if type of given event is applicable to required type
					typeChecks[i] = event -> requiredClass.isInstance(definingField.get(event));
				} else if (typeParam instanceof WildcardType) {
					WildcardType wc = (WildcardType) typeParam;
					// Assert wildcard is unbounded (<?>)
					if (wc.getLowerBounds().length > 0 || !Arrays.equals(wc.getUpperBounds(), new Object[]{Object.class}))
						// I can't think of any use case for bounded wildcards in event listeners
						throw new UnsupportedOperationException("Bounded wildcards are not implemented.");

					// Unbounded wildcards match every object
					typeChecks[i] = event -> true;
				} else {
					typeChecks[i] = event -> true;
				}
			}

			typeCheck = Predicate.all(c(typeChecks));
		}

		Consumer<Event> consumer = LambdaUtil.createFunctionalInterface(Consumer.class, method, owner);
		listeners.add(new Listener(owner, priority, event -> {
			// Check whether event is canceled
			if (event.isCanceled() && !receiveCanceled)
				return;

			if (mustBeEnabled && !((Disableable) owner).isEnabled())
				return;

			// Check type parameters
			if (!typeCheck.test(event))
				return;

			consumer.accept(event);
		}));
	}

	/**
	 * Searches for the field defining the given generic parameter of a class.
	 */
	private static Field findGenericDefiningField(Class<?> owner, Type genericParameter) {
		for (Field field : owner.getDeclaredFields())
			if (genericParameter == field.getGenericType())
				return field;

		if (!(owner.getGenericSuperclass() instanceof ParameterizedType))
			throw new IllegalStateException("Could not find field defining generic parameter " + genericParameter + " of " + owner.getName());

		Type[] actualTypes = ((ParameterizedType) owner.getGenericSuperclass()).getActualTypeArguments();
		for (int i = 0; i < actualTypes.length; i++)
			if (actualTypes[i] == genericParameter)
				return findGenericDefiningField(owner.getSuperclass(), owner.getSuperclass().getTypeParameters()[i]);

		throw new IllegalStateException("Could not find field defining generic parameter " + genericParameter + " of " + owner.getName());
	}

}
