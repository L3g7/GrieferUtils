/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.core.event_bus;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.file_provider.meta.MethodMeta;
import dev.l3g7.griefer_utils.core.misc.functions.Supplier;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventRegisterer {

	private static final Map<String, List<LazyRegistration>> lazyRegistrations = new ConcurrentHashMap<>();

	static void handleLazyRegistrations(String eventClass) {
		List<LazyRegistration> registrations = lazyRegistrations.remove(eventClass);
		if (registrations == null)
			return;

		for (LazyRegistration registration : registrations)
			EventBus.registerMethod(registration.owner.get(), registration.meta.load());
	}

	public static void init() {
		for (MethodMeta method : FileProvider.getAnnotatedMethods(EventListener.class)) {
			if (method.isStatic()) {
				registerLazyRegistration(method, () -> method.owner().load());
				continue;
			}

			boolean isSingleton = method.owner().hasAnnotation(Singleton.class);
			if (isSingleton) {
				registerLazyRegistration(method, () -> FileProvider.getSingleton(method.owner().load()));
			} else {
				for (ClassMeta classMeta : FileProvider.getClassesWithSuperClass(method.owner().name))
					if (classMeta.hasAnnotation(Singleton.class))
						registerLazyRegistration(method, () -> FileProvider.getSingleton(classMeta.load()));
			}
		}
	}

	public static void register(Object object) {
		if (object instanceof Class<?>)
			return;

		Class<?> clazz = object.getClass();

		for (Method method : clazz.getDeclaredMethods()) {
			if (Modifier.isStatic(method.getModifiers()))
				return;

			if (!method.isAnnotationPresent(EventListener.class))
				return;

			registerLazyRegistration(new MethodMeta(new ClassMeta(clazz), method), () -> object);
		}
	}

	public static void unregister(Object object) {
		EventBus.events.values().removeIf(consumers -> {
			consumers.removeEventsOf(object);
			return consumers.isEmpty();
		});
	}

	private static void registerLazyRegistration(MethodMeta method, Supplier<Object> ownerSupplier) {

		// Check count
		Type[] params = Type.getArgumentTypes(method.desc());
		if (params.length != 1)
			throw new IllegalArgumentException("Method " + method + " has @EventListener annotation, but requires " + params.length + " arguments");

		String eventClass = params[0].getClassName();

		List<LazyRegistration> map = lazyRegistrations.computeIfAbsent(eventClass, s -> new ArrayList<>());
		map.add(new LazyRegistration(method, ownerSupplier));
	}

	private static class LazyRegistration {

		private final MethodMeta meta;
		private final Supplier<Object> owner; // Supplier to lazy-load singletons

		private LazyRegistration(MethodMeta meta, Supplier<Object> owner) {
			this.meta = meta;
			this.owner = owner;
		}

	}

}
