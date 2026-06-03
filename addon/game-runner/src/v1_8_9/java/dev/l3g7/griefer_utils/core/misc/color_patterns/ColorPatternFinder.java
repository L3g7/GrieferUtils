package dev.l3g7.griefer_utils.core.misc.color_patterns;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;

import static dev.l3g7.griefer_utils.core.misc.color_patterns.ColorPattern.BOLD;
import static dev.l3g7.griefer_utils.core.misc.color_patterns.ColorPattern.OBFUSCATED;

class ColorPatternFinder {

	private static final String RED = "c";
	private static final String WHITE = "f";

	public static final ColorPattern WHITE_COLOR_PATTERN = new ColorPattern(WHITE, WHITE, BOLD);
	public static final ColorPattern MAGIC_COLOR_PATTERN = new ColorPattern(RED, RED, OBFUSCATED);

	private static String[] repeatingPrefixes = new String[0];

	/**
	 * @param rank formatted
	 * @param name formatted
	 */
	public static ColorPattern findPattern(String rank, String name) {
		if (name.contains("§k"))
			return MAGIC_COLOR_PATTERN;

		String rankColors = extractColors(rank);
		if (rankColors.equals("f"))
			return WHITE_COLOR_PATTERN;

		String nameColors = extractColors(name);
		if (!isStartingSubset(rankColors, nameColors))
			// Colors of rank do not match name
			return new ColorPattern(rankColors, nameColors, BOLD);

		String colors = rankColors.length() > nameColors.length() ? rankColors : nameColors;
		if (StringUtil.repeat(colors.charAt(0), colors.length()).equals(colors))
			// Fixed color
			return new ColorPattern(colors.charAt(0), extractDecorations(rank).contains("l"));

		// Find exact match
		for (String pattern : repeatingPrefixes)
			if (colors.startsWith(pattern))
				return new ColorPattern(pattern);

		// Find approx. match
		for (String pattern : repeatingPrefixes)
			if (isStartingSubset(colors, pattern))
				return new ColorPattern(pattern);

		return new ColorPattern(colors);
	}

	private static String extractColors(String text) {
		StringBuilder colors = new StringBuilder();
		char[] chars = text.toCharArray();
		char currentColor = '\0';

		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c != '§') {
				if (currentColor != '\0')
					colors.append(currentColor);

				continue;
			}

			char formattingCode = chars[++i];
			if (formattingCode >= '0' && formattingCode <= '9'
				|| formattingCode >= 'a' && formattingCode <= 'f') {
				currentColor = formattingCode;
			}
		}

		return colors.toString();
	}

	private static String extractDecorations(String text) {
		StringBuilder colors = new StringBuilder();
		char[] chars = text.toCharArray();
		char currentColor = '\0';

		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c != '§') {
				if (currentColor != '\0')
					colors.append(currentColor);

				continue;
			}

			char formattingCode = chars[++i];
			if (formattingCode >= 'k' && formattingCode <= 'o') {
				currentColor = formattingCode;
			}
		}

		return colors.toString();
	}

	/**
	 * @return true if any of the strings starts with the other.
	 */
	private static boolean isStartingSubset(String a, String b) {
		return a.length() > b.length() ? a.startsWith(b) : b.startsWith(a);
	}

	@EventListener
	private static void onStaticData(StaticDataReceiveEvent event) {
		repeatingPrefixes = event.data.repeatingPrefixes;
	}

}
