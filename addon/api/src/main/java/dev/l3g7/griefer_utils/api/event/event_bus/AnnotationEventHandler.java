/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.api.event.event_bus;

import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.event.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.file_provider.meta.MethodMeta;
import org.objectweb.asm.Opcodes;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.invoke;

/**
 * Handles the logic for triggering methods annotated with {@link OnEnable} and {@link OnStartupComplete}.
 */
@Singleton
public class AnnotationEventHandler implements Opcodes {

	private static final Map<Class<? extends Annotation>, List<Runnable>> listeners = new HashMap<>();
	private static final List<Class<? extends Annotation>> annotations = ImmutableList.of(OnEnable.class, OnStartupComplete.class);
	private static boolean initialized = false;

	/**
	 * Registers all static methods annotated with {@link OnEnable} and {@link OnStartupComplete} as well as non-static methods if the owner is marked with {@link Singleton}.
	 */
	private static void init() {
		if (initialized)
			return;

		initialized = true;
		for (Class<? extends Annotation> annotation : annotations) {
			List<Runnable> runnables = listeners.computeIfAbsent(annotation, c -> new ArrayList<>());

			for (MethodMeta method : FileProvider.getAnnotatedMethods(annotation)) {
				boolean isSingleton = method.owner().hasAnnotation(Singleton.class);

				if (!method.isStatic() && !isSingleton)
					continue;

				runnables.add(() -> invoke(resolveOwner(method, isSingleton), method.load()));
			}
		}
	}

	/**
	 * @return the owner singleton if isSingleton is true, null otherwise.
	 */
	private static Object resolveOwner(MethodMeta method, boolean isSingleton) {
		if (!isSingleton)
			return null;

		return FileProvider.getSingleton(method.owner().load());
	}

	/**
	 * Triggers all registered listeners associated with this event.
	 */
	static void triggerEvent(Class<? extends Annotation> clazz) {
		init();

		if (listeners.containsKey(clazz))
			listeners.get(clazz).forEach(Runnable::run);
	}

}
