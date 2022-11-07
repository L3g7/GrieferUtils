/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.event;

import dev.l3g7.griefer_utils.event.events.RenderWorldEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.render.ChunkIndicator;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.file_provider.meta.AnnotationMeta;
import dev.l3g7.griefer_utils.file_provider.meta.MethodMeta;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import static dev.l3g7.griefer_utils.util.reflection.Reflection.c;

/**
 * Handles the logic for registering methods annotated with {@link EventListener} to the {@link MinecraftForge#EVENT_BUS}.
 */
public class EventHandler implements Opcodes {

	private static final int BUS_ID = 0; // MinecraftForge.EVENT_BUS

	/**
	 * Registers all static methods annotated with {@link EventListener} to the {@link MinecraftForge#EVENT_BUS} as well as non-static methods if the owner is marked with {@link Singleton}.
	 */
	public static void init() {
		AnnotationEventHandler.init();
		for (MethodMeta method : FileProvider.getAnnotatedMethods(EventListener.class)) {

			boolean isSingleton = method.owner().hasAnnotation(Singleton.class);
			// Skip non-static listeners
			if (!method.isStatic() && !isSingleton)
				continue;

			Class<? extends Event> eventClass = getEventClass(method);
			ListenerList listeners = Reflection.construct(eventClass).getListenerList();

			// Get metadata
			AnnotationMeta meta = method.getAnnotation(EventListener.class);
			EventPriority priority = meta.getValue("priority", true);
			boolean triggerWhenDisabled = meta.getValue("triggerWhenDisabled", false);
			boolean receiveCanceled = meta.getValue("receiveCanceled", false);
			boolean receiveSubclasses = meta.getValue("receiveSubclasses", false);

			listeners.register(BUS_ID, priority, e -> {
				if ((receiveCanceled || !e.isCanceled())
					&& (receiveSubclasses || e.getClass() == eventClass)) {
					Object owner = resolveOwner(method, isSingleton);
					if (triggerWhenDisabled || !(owner instanceof Feature) || ((Feature) owner).isEnabled())
						Reflection.invoke(resolveOwner(method, isSingleton), method.load(), e);
				}
			});
		}
	}

	/**
	 * Registers all non-static methods annotated with {@link EventListener} to the {@link MinecraftForge#EVENT_BUS}.
	 */
	public static void register(Object obj) {
		AnnotationEventHandler.register(obj);

		Class<?> clazz = obj.getClass();
		for (Method method : Reflection.getAnnotatedMethods(clazz, EventListener.class)) {

			// Skip static listeners
			if ((method.getModifiers() & ACC_STATIC) != 0)
				return;

			Class<? extends Event> eventClass = getEventClass(new MethodMeta(null, method));
			ListenerList listeners = Reflection.construct(eventClass).getListenerList();

			// Get metadata
			EventListener meta = method.getAnnotation(EventListener.class);
			EventPriority priority = meta.priority();
			boolean receiveCanceled = meta.receiveCanceled();
			boolean triggerWhenDisabled = meta.triggerWhenDisabled();
			boolean receiveSubclasses = meta.receiveSubclasses();

			Predicate<Event> check = event ->
				(receiveCanceled || !event.isCanceled()
					&& (receiveSubclasses || event.getClass() == eventClass));

			// Check if obj is Feature
			if (obj instanceof Feature) {
				// Check if feature is disabled
				Feature feature = (Feature) obj;
				listeners.register(BUS_ID, priority, event -> {
					if (check.test(event) && (triggerWhenDisabled || feature.isEnabled()))
						Reflection.invoke(obj, method, event);
				});
			} else {
				// Don't check
				listeners.register(BUS_ID, priority, event -> {
					if (check.test(event))
						Reflection.invoke(obj, method, event);
				});
			}
		}
	}

	/**
	 * Validates the parameters of the method and returns the class of the {@link Event} being listened to.
	 */
	private static Class<? extends Event> getEventClass(MethodMeta method) {

		// Check count
		Type[] params = Type.getArgumentTypes(method.desc());
		if (params.length != 1)
			throw new IllegalArgumentException("Method " + method + " has @EventListener annotation, but requires " + params.length + " arguments");

		// Check type
		Class<?> eventClass = Reflection.load(params[0].getClassName());
		if (!Event.class.isAssignableFrom(eventClass))
			throw new IllegalArgumentException("Method " + method + " has @EventListener annotation, but takes " + eventClass);

		return c(eventClass);
	}

	/**
	 * @return the owner singleton if isSingleton is true, null otherwise.
	 */
	private static Object resolveOwner(MethodMeta method, boolean isSingleton) {
		if (!isSingleton)
			return null;

		return FileProvider.getSingleton(method.owner().load());
	}

}
