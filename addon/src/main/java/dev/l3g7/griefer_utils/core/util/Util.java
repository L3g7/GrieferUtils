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

package dev.l3g7.griefer_utils.core.util;

import dev.l3g7.griefer_utils.core.reflection.Reflection;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.text.DecimalFormat;

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

		Reflection.set(throwable, formattedMessage, "detailMessage");
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
		return addMessage(elevate(throwable), message, args);
	}

	/**
	 * Terminates Java.
	 * Should only be used for debug purposes.
	 */
	@Deprecated
	public static void die(int status) {
		FMLCommonHandler.instance().exitJava(status, false);
	}


	private static final DecimalFormat DOUBLE_NUMBER = new DecimalFormat("00");

	public static String formatTime(long endTime) {
		long seconds = (endTime - System.currentTimeMillis()) / 1000L;
		long h = seconds / 60 / 60;
		long m = seconds / 60 % 60;
		long s = seconds % 60;

		String time = DOUBLE_NUMBER.format(m) + ":" + DOUBLE_NUMBER.format(s);
		if (h > 0)
			time = DOUBLE_NUMBER.format(h) + ":" + time;

		return time;
	}

	public static String formatTime(long endTime, boolean shorten) {
		long secondsRaw = (endTime - System.currentTimeMillis()) / 1000L;
		if(secondsRaw <= 0L)
			return shorten ? "0s" : "0 Sekunden";
		return formatTime(secondsRaw / 60L / 60L, secondsRaw / 60L % 60L, secondsRaw % 60L, shorten);
	}

	public static String formatTime(long hours, long minutes, long seconds, boolean shorten) {
		String result = "";
		if (hours > 0L)
			result += shorten ? hours + "h " : hours == 1L ? "eine Stunde, " : hours + " Stunden, ";
		if (minutes > 0L)
			result += shorten ? minutes + "m " : minutes == 1L ? "eine Minute, " : minutes + " Minuten, ";
		result += shorten ? seconds + "s" : seconds == 1L ? "eine Sekunde" : seconds + " Sekunden";
		return result;
	}
}