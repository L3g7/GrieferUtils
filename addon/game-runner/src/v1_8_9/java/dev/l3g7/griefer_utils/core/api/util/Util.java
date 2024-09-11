/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.util;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.function.BiConsumer;

/**
 * Everything that doesn't fit into the other utility classes.
 */
public class Util {

	private static final DecimalFormat DOUBLE_NUMBER = new DecimalFormat("00");

	private static final BiConsumer<Throwable, String> setDetailMessageFunc;

	static {
		try {
			if (System.getProperty("java.version").startsWith("1.")) {
				// Java 8 (or lower)
				Field detailMessage = Throwable.class.getDeclaredField("detailMessage");
				detailMessage.setAccessible(true);
				setDetailMessageFunc = (throwable, formattedMessage) -> {
					try {
						detailMessage.set(throwable, formattedMessage);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				};
			} else {
				// Java 9 or higher
				// Create elevated lookup
				MethodHandles.Lookup lookup = MethodHandles.lookup();
				Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
				theUnsafe.setAccessible(true);
				Unsafe unsafe = (Unsafe) theUnsafe.get(null);
				unsafe.putInt(lookup, 12 /* allowedModes */, -1 /* TRUSTED */);

				var setter = lookup.findSetter(Throwable.class, "detailMessage", String.class);

				setDetailMessageFunc = (throwable, formattedMessage) -> {
					try {
						setter.invoke(throwable, formattedMessage);
					} catch (Throwable e) {
						throw new RuntimeException(e);
					}
				};
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Adds a message in front of the existing message.
	 */
	public static <T extends Throwable> T addMessage(T throwable, String message, Object... args) {
		String formattedMessage = args.length == 0 ? message : String.format(message, args);

		if (throwable.getMessage() != null)
			formattedMessage += " (" + throwable.getMessage() + ")";

		setDetailMessageFunc.accept(throwable, formattedMessage);
		return throwable;
	}

	/**
	 * Elevates a Throwable to a RuntimeException without modifying the stack trace.
	 */
	public static RuntimeException elevate(Throwable throwable) {
		return new RuntimeException(null, throwable, true, false) {};
	}

	/**
	 * Elevates a Throwable and adds a message.
	 *
	 * @see Util#elevate(Throwable)
	 */
	public static RuntimeException elevate(Throwable throwable, String message, Object... args) {
		String formattedMessage = args.length == 0 ? message : String.format(message, args);
		return new RuntimeException(formattedMessage, throwable, true, false) {};
	}

	public static String formatTime(long endTime) {
		long seconds = (endTime - System.currentTimeMillis()) / 1000L;
		return formatTimeSeconds(seconds);
	}

	public static String formatTimeSeconds(long seconds) {
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
		return formatTimeSeconds(secondsRaw, shorten);
	}

	public static String formatTimeSeconds(long seconds, boolean shorten) {
		if (seconds <= 0L)
			return shorten ? "0s" : "0 Sekunden";
		return formatTime(seconds / 60L / 60L, seconds / 60L % 60L, seconds % 60L, shorten);
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
