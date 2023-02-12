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

package dev.l3g7.griefer_utils.event;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.file_provider.meta.AnnotationMeta;
import dev.l3g7.griefer_utils.core.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.file_provider.meta.MethodMeta;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static dev.l3g7.griefer_utils.core.reflection.Reflection.c;

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
			if (method.isStatic()) {
				register(method, method.owner());
				continue;
			}

			boolean isSingleton = method.owner().hasAnnotation(Singleton.class);
			if (isSingleton)
				register(method, method.owner());
			else {
				for (ClassMeta classMeta : FileProvider.getClassesWithSuperClass(method.owner().name))
					if (classMeta.hasAnnotation(Singleton.class))
						register(method, classMeta);
			}
		}
	}

	private static void register(MethodMeta method, ClassMeta ownerClass) {
		boolean isSingleton = ownerClass.hasAnnotation(Singleton.class);
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
				Object owner = resolveOwner(ownerClass, isSingleton);
				try {
					if (triggerWhenDisabled || !(owner instanceof Feature) || ((Feature) owner).isEnabled())
						Reflection.invoke(owner, method.load(), e);
				} catch (NullPointerException e_) {
					e_.printStackTrace();
				}
			}
		});
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
	private static Object resolveOwner(ClassMeta owner, boolean isSingleton) {
		if (!isSingleton)
			return null;

		return FileProvider.getSingleton(owner.load());
	}

}
