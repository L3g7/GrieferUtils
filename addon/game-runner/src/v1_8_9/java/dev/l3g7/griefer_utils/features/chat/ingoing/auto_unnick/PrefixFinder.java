/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.auto_unnick;

import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;

public class PrefixFinder {
	public static String[] prefixes = null;

	public static String getPrefix(String rank, String name) {
		try {
			return getPrefixWithException(rank, name);
		} catch (Throwable t) {
			BugReporter.reportError(new Throwable(t.getMessage() + ": " + rank + " | " + name, t));
			return "4";
		}
	}

	private static String getPrefixWithException(final String rank, final String name) {
		final String colorOnlyText;
		final String originalText;
		if (rank.charAt(1) != name.charAt(1) || rank.contains("+")) // RW prefix || Streamer/YT +
			originalText = trim(name);
		else
			originalText = trim(rank.length() > name.length() ? rank : name);

		colorOnlyText = originalText.replaceAll("§[^0-9a-f]", "");

		// Single prefix (<= 2 because of §l / §k)
		if (originalText.lastIndexOf('§') <= 2)
			return originalText.substring(0, originalText.lastIndexOf('§') + 2)
				.replace("§", "");

		char firstCode = colorOnlyText.charAt(1);

		// Double prefix
		if (firstCode == getFormattingAt(colorOnlyText, 1)
			&& firstCode == getFormattingAt(colorOnlyText, 4)) {
			String secondString = String.valueOf(getFormattingAt(colorOnlyText, 2));
			String firstString = String.valueOf(firstCode);
			return firstString + firstString + secondString + secondString;
		}

		// Prefixes couldn't be loaded
		if (prefixes == null)
			return "4";

		for (String currentPrefix : prefixes) {
			char[] chars = currentPrefix.toCharArray();
			boolean isPrefix = true;

			// At least 5 chars have to be compared to be sure (Lucky and Halloween 1 both start with 66ee)
			for (int i = 0; i < 5; i++)
				isPrefix &= (chars[i % chars.length] == getFormattingAt(colorOnlyText, i));

			if (isPrefix)
				return currentPrefix;
		}

		return "4";
	}

	// Removes all §r at the start of the text
	private static String trim(String text) {
		while (text.startsWith("§r"))
			text = text.substring(2);

		return text;
	}

	// Returns the formatting code of the char at the given index
	public static Character getFormattingAt(String colorOnlyText, int index) {
		Character formattingCode = null;

		char[] chars = colorOnlyText.toCharArray();

		int currentIndex = 0;
		for (int i = 0; currentIndex <= index; ) {
			if (chars[i++] == '§')
				formattingCode = chars[i++];
			else
				currentIndex++;
		}

		return formattingCode;
	}

	@EventListener
	private static void onStaticData(StaticDataReceiveEvent event) {
		prefixes = event.data.repeatingPrefixes;
	}

}