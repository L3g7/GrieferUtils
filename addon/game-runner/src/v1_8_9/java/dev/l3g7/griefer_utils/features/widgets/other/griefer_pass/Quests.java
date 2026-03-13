package dev.l3g7.griefer_utils.features.widgets.other.griefer_pass;

import dev.l3g7.griefer_utils.core.api.BugReporter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Quests {

	public static final Map<Pattern, Class<? extends AbstractQuest>> QUESTS = new HashMap<>();
	public static final Map<Pattern, String> DISPLAY_PATTERNS = new HashMap<>();
	public static final Set<Pattern> APPROXIMATE_QUESTS = new HashSet<>();

	static {
		registerQuest(true, "Esse {AMOUNT}x {TARGET}", MiscQuests.EatQuest.class);
		registerQuest(true, "Baue {AMOUNT}x {TARGET} ab", MiscQuests.BreakQuest.class);
		registerQuest(true, "Töte {AMOUNT}x {TARGET}", MiscQuests.KillQuest.class);
		registerQuest(true, "Laufe {AMOUNT} Blöcke", MiscQuests.WalkQuest.class);
		registerQuest(true, "Platziere {AMOUNT} Blöcke auf deinem Grundstück", MiscQuests.PlaceQuest.class);
		registerQuest(true, "Angle {AMOUNT}x {TARGET}", MiscQuests.FishQuest.class);
		registerQuest(true, "Öffne {AMOUNT}x LuckyBlöcke", MiscQuests.OpenLuckyBlockQuest.class);
		registerQuest(true, "Erleide {AMOUNT}x Schaden durch Mobs oder Spieler", MiscQuests.GetDamagedQuest.class);

		registerQuest(false, "Schieße {AMOUNT}x mit Bogen", MiscQuests.ShootBowQuest.class);
		registerQuest(false, "Nutze {AMOUNT}x ein Boot oder Minecart", MiscQuests.RideBoatOrMinecartQuest.class);
		registerQuest(false, "Betritt {AMOUNT}x einen Citybuild", MiscQuests.JoinCitybuildQuest.class);
		registerQuest(false, "Zähme {AMOUNT} Tiere", MiscQuests.TameQuest.class);
		registerQuest(false, "Erhalte {AMOUNT} Effekte", MiscQuests.ReceiveEffectsQuest.class);
		registerQuest(false, "Sammle {AMOUNT}x Items auf", MiscQuests.PickupItemsQuest.class);

		registerQuest(false, "Führe {AMOUNT}x den Befehl p h aus", MessageReceiveQuest.PHQuest.class);
		registerQuest(false, "Führe {AMOUNT}x den Befehl {TARGET} aus", MiscQuests.CommandSendQuest.class);
		registerQuest(false, "Verkleide dich {AMOUNT}x mit /disguise", MessageReceiveQuest.DisguiseQuest.class);
		registerQuest(false, "Erhalte {AMOUNT}x Orbs", MessageReceiveQuest.OrbQuest.class);
		registerQuest(false, "Erledige {AMOUNT} Adventurer-Aufgaben", MessageReceiveQuest.AdventurerQuest.class);
		registerQuest(false, "Zahle insgesamt {AMOUNT}\\$ an andere Spieler \\(nicht pay \\*\\)", "Zahle an andere Spieler", MessageReceiveQuest.PayQuest.class);
		registerQuest(false, "Schreibe {AMOUNT} Chat-Nachrichten", MessageReceiveQuest.WriteMessageQuest.class);
		registerQuest(false, "Erhalte {AMOUNT} Grundstück\\(e\\)", "Erhalte Grundstücke", MessageReceiveQuest.PlotReceiveQuest.class);
		registerQuest(false, "Biete {AMOUNT}x auf eine Auktion", MessageReceiveQuest.BidQuest.class);
		registerQuest(false, "Erstelle {AMOUNT} Jobs beim Jobs-NPC", MessageReceiveQuest.JobCreateQuest.class);
		registerQuest(false, "Liefere {AMOUNT}x Stacks zum Jobs-NPC", MessageReceiveQuest.JobFulfillQuest.class);
		registerQuest(false, "Betritt {AMOUNT}x den Zauberwald", MessageReceiveQuest.JoinMagicForestQuest.class);
		registerQuest(false, "Brich {AMOUNT}x in das Gefängnis ein", MessageReceiveQuest.BreakIntoJailQuest.class);
		registerQuest(false, "Merge {AMOUNT}x ein Grundstück", MessageReceiveQuest.MergeQuest.class);
		registerQuest(false, "Setze {AMOUNT} Plotflags", MessageReceiveQuest.SetPlotFlagsQuest.class);

		registerQuest(false, "Stelle {AMOUNT} Gegenstände her \\(Jedes Crafting zählt einmal\\)", "Stelle Gegenstände her", WindowClickQuest.CraftQuest.class);
		registerQuest(true, "Verzaubere {AMOUNT} Gegenstände", WindowClickQuest.EnchantQuest.class);
		registerQuest(false, "Nehme {AMOUNT}x Items aus passiven Spawnern", WindowClickQuest.SpawnerQuest.class);

		registerQuest(false, "Erstelle {AMOUNT} Portal auf deinem Grundstück", "Erstelle ein Portal auf deinem Grundstück", InstantlyCompletableQuest.class);
		registerQuest(false, "Öffne {AMOUNT}x {TARGET} Kiste", "Öffne {TARGET} Kisten", InstantlyCompletableQuest.class);
		registerQuest(false, "Benutze {AMOUNT}x Items mit Sicherheitscode", InstantlyCompletableQuest.class);
	}

	private static void registerQuest(boolean approximate, String pattern, Class<? extends AbstractQuest> clazz) {
		registerQuest(approximate, pattern, pattern.replaceAll(" \\{AMOUNT}x?", ""), clazz);
	}

	private static void registerQuest(boolean approximate, String regex, String displayPattern, Class<? extends AbstractQuest> clazz) {
		Pattern pattern = Pattern.compile('^' + regex.replaceAll("\\{AMOUNT}x?", "(\\\\d+)x?").replaceAll("\\{TARGET}", "(.+)") + '$');
		QUESTS.put(pattern, clazz);
		DISPLAY_PATTERNS.put(pattern, displayPattern);

		if (approximate)
			APPROXIMATE_QUESTS.add(pattern);
	}

	public static AbstractQuest parseQuest(int index, String message, int amount, int maxAmount, int completions, int maxCompletions) {
		for (Map.Entry<Pattern, Class<? extends AbstractQuest>> entry : QUESTS.entrySet()) {
			Matcher matcher = entry.getKey().matcher(message);
			if (!matcher.matches())
				continue;

			try {
				Constructor<? extends AbstractQuest> constructor = entry.getValue().getDeclaredConstructor();
				constructor.setAccessible(true);
				AbstractQuest quest = constructor.newInstance();

				// Compute display text
				String pattern = Quests.DISPLAY_PATTERNS.get(entry.getKey());
				String displayText;

				if (!pattern.contains("{TARGET}"))
					displayText = pattern;
				else
					displayText = pattern.replace("{TARGET}", matcher.group(2));

				quest.init(index, matcher, displayText, APPROXIMATE_QUESTS.contains(entry.getKey()), maxAmount, maxCompletions);
				quest.increaseCompletions(completions);
				quest.setAmount(amount);
				return quest;
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		BugReporter.reportError(new Throwable("Unknown quest at index " + index + ": " + message));
		return null;
	}
}
