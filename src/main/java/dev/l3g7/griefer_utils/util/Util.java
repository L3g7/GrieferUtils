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

package dev.l3g7.griefer_utils.util;

import dev.l3g7.griefer_utils.util.reflection.Reflection;

/**
 * Everything that doesn't fit into the other utility classes.
 */
public class Util {

	/**
	 * Adds a message in front of the existing message.
	 */
	public static <T extends Throwable> T addMessage(T throwable, String message, Object... args) {
		String formattedMessage = args.length == 0 ? message : String.format(message, args);

		if (throwable.getMessage() != null)
			formattedMessage += " (" + throwable.getMessage() + ")";

		Reflection.set(throwable, "detailMessage", formattedMessage);
		return throwable;
	}

	/**
	 * Elevates a Throwable to a RuntimeException without modifying the stack trace.
	 */
	public static RuntimeException elevate(Throwable throwable) {
		return Reflection.construct(RuntimeException.class, null, throwable, true, false);
	}

	/**
	 * Elevates a Throwable and adds a message.
	 *
	 * @see Util#addMessage(Throwable, String, Object...)
	 * @see Util#elevate(Throwable)
	 */
	public static RuntimeException elevate(Throwable throwable, String message, Object... args) {
		return elevate(addMessage(throwable, message, args));
	}

}
