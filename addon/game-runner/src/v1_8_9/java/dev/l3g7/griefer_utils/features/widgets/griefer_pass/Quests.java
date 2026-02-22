package dev.l3g7.griefer_utils.features.widgets.griefer_pass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Quests {

	private static final Map<Pattern, Class<? extends AbstractQuest>> QUESTS = new HashMap<>();
	static final Map<Class<? extends AbstractQuest>, String> TO_STRING_PATTERNS = new HashMap<>();
	static {
		registerQuest(true, "Laufe {AMOUNT} Blöcke", MiscQuests.WalkQuest.class);
		registerQuest(false, "Esse {AMOUNT}x {TARGET}", MiscQuests.EatQuest.class);
		registerQuest(false, "Baue {AMOUNT}x {TARGET} ab", MiscQuests.BreakQuest.class);

		registerQuest(false, "Erstelle {AMOUNT} Portal auf deinem Grundstück", MiscQuests.PortalQuest.class);
	}

	private static void registerQuest(boolean approximate, String pattern, Class<? extends AbstractQuest> clazz) {
		QUESTS.put(Pattern.compile('^' + pattern.replaceAll("\\{AMOUNT}x?", "(\\\\d+)x?").replaceAll("\\{TARGET}", "(.+)") + '$'), clazz);
		String toStringName = pattern.replaceAll(" \\{AMOUNT}x?", "");
		TO_STRING_PATTERNS.put(clazz, toStringName + (approximate ? ": ~%d/%d" : ": %d/%d"));
	}

	public static AbstractQuest parseQuest(String message) {
		for (Map.Entry<Pattern, Class<? extends AbstractQuest>> entry : QUESTS.entrySet()) {
			Matcher matcher = entry.getKey().matcher(message);
			if (!matcher.matches())
				continue;

			try {
				Constructor<? extends AbstractQuest> constructor = entry.getValue().getDeclaredConstructor(Matcher.class, int.class);
				constructor.setAccessible(true);
				return constructor.newInstance(matcher, Integer.parseInt(matcher.group(1)));
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	public static String computeFormatPattern(AbstractQuest quest) {
		String pattern = Quests.TO_STRING_PATTERNS.get(quest.getClass());
		if (!pattern.contains("{TARGET}"))
			return pattern;

		return pattern.replace("{TARGET}", quest.getMatcher().group(2));
	}

}
