package dev.l3g7.griefer_utils.features.tweaks.autounnick;

import dev.l3g7.griefer_utils.util.IOUtil;

	public class PrefixFinder {
	private static String[] prefixes = null;

	private final String colorOnlyText;
	private final String originalText;

	public PrefixFinder(String rank, String name) {

		if (rank.charAt(1) != name.charAt(1) || rank.contains("+")) // RW prefix || Streamer/YT +
			originalText = trim(name);
		else
			originalText = trim(rank.length() > name.length() ? rank : name);

		this.colorOnlyText = originalText.replaceAll("§[^0-9a-f]", "");
	}

	public String getPrefix() {
		// Single prefix (<= 2 because of §l / §k)
		if (originalText.lastIndexOf('§') <= 2)
			return originalText.substring(0, originalText.lastIndexOf('§') + 2)
				.replace("§", "");

		char firstCode = colorOnlyText.charAt(1);

		// Double prefix
		if (firstCode == getFormattingAt(1)
				&& firstCode == getFormattingAt(4)) {
			String secondString = String.valueOf(getFormattingAt(2));
			String firstString = String.valueOf(firstCode);
			return firstString + firstString + secondString + secondString;
		}

		if (prefixes == null)
			return "4";

		for (String currentPrefix : prefixes) {
			char[] chars = currentPrefix.toCharArray();
			boolean isPrefix = true;

			// At least 5 chars have to be compared to be sure (Lucky and Halloween 1 both start with 66ee)
			for (int i = 0; i < 5; i++)
				isPrefix &= (chars[i % chars.length] == getFormattingAt(i));

			if (isPrefix)
				return currentPrefix;
		}

		return "4";
	}

	private String trim(String text) {
		while (text.startsWith("§r"))
			text = text.substring(2);

		return text;
	}

	public Character getFormattingAt(int index) {
		Character formattingCode = null;

		char[] chars = colorOnlyText.toCharArray();

		int currentIndex = 0;
		for (int i = 0; currentIndex <= index;) {
			if (chars[i++] == '§')
				formattingCode = chars[i++];
			else
				currentIndex++;
		}

		return formattingCode;
	}

	static {
		IOUtil.request("https://grieferutils.l3g7.dev/repeating_prefixes/").asJsonArray(jsonArray -> {
			String[] prefixes = new String[jsonArray.size()];

			for (int i = 0; i < jsonArray.size(); i++)
				prefixes[i] = jsonArray.get(i).getAsString();

			PrefixFinder.prefixes = prefixes;
		}).orElse(Throwable::printStackTrace);
	}
}
