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

package dev.l3g7.griefer_utils.core.event_bus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static dev.l3g7.griefer_utils.core.event_bus.Priority.NORMAL;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener {

	/**
	 * The priority of the event listener.
	 * Listeners with a higher priority will be called sooner than listeners with a lower priority.
	 */
	Priority priority() default NORMAL;

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
