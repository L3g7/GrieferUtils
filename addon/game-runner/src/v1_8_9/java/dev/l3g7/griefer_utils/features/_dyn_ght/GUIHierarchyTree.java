package dev.l3g7.griefer_utils.features._dyn_ght;

import dev.l3g7.griefer_utils.core.settings.GUIEntry;

import java.util.HashMap;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@SuppressWarnings("SameParameterValue")
public class GUIHierarchyTree {

	static Feat[] CATS = new Feat[]{
		cat("Chat", "chat",
			cat("§gEingehender Chat", "chat",
				feat("Automatisch entnicken", "name_tag"),
				feat("Chat aufhellen", "light_bulb"),
				feat("Chat aufräumen", "crossed_out_book"),
				feat("Chatmenü", "player_menu"),
				feat("ChatReactor", "cpu"),
				feat("ChatTime", "clock"),
				feat("Echtgeld- Erkennung", "Echtgeld-Erkennung", "coin"),
				feat("Interagierbare /freunde", "players"),
				feat("Interagierbare Nachrichten", "mouse_left"),
				feat("Kopf vor Nachrichten", "steve"),
				feat("Magischen Text entfernen", "tabping_colored")
			),
			cat("§hAusgehender Chat", "book_and_quill",
				feat("AntiCommandChoker", "crossed_out_chat"),
				feat("/switch verbessern", "portal"),
				feat("Befehle loggen", "book_and_quill"),
				feat("Befehlsradialmenü", "command_menu"),
				feat("Befehlsvorschläge", "command_suggestions"),
				feat("Farb-Vorschau", "color_palette"),
				feat("Lange Nachrichten aufteilen", "shears"),
				feat("Multi-Hotkey", "key"),
				feat("Plot-Chat-Indikator", "chat_orange"),
				feat("Rechner", "calculator"),
				feat("SlowChat-Cooldown", "hourglass")
			),
			cat("§iChat-Filter", "hopper",
				feat("Chat-Filter-Länge entsperren", "measurement"),
				feat("Filtervorlagen", "enchanted_book"),
				feat("Webhooks in Filtern", "discord")
			)
		),
		cat("GUIs", "wooden_board",
			cat("§gGrieferGames", "griefer_games",
				feat("/homes verbessern", "filled_map"),
				feat("/zuhause verbessern", "region_map"),
				feat("Trichteranzeige verbessern", "hopper"),
				feat("Adventurer verbessern", "enchanted_fire_charge"),
				feat("Jobbörse verbessern", "wooden_board"),
				feat("NPC-Klick-Helfer", "mouse_right"),
				feat("Orb-Händler fixen", "orbseller"),
				feat("Cooldown-Benachrichtigungen", "bell")
			),
			cat("§hIntegrationen", "cpu",
				feat("Botshop-Gui", "high_res/byte_and_bit"),
				feat("Biom- und Strukturen-Suche", "region_map"),
				feat("§xGriefer.Info", "high_res/griefer_info")
			),
			cat("§iBücher", "enchanted_book",
				feat("Bücher Öffnen fixen", "lectern"),
				feat("Bücher unterdrücken", "crossed_out_book_read")
			),
			feat("Interagierbare Profile", "mouse_left"),
			feat("Item-Suche", "magnifying_glass"),
			feat("LabyMod-Switcher fixen", "labymod")
		),

		cat("Items", "gold_ingot",
			cat("§gInventar verbessern", "bundle",
				feat("Besseres Shiften", "arrows_up"),
				feat("Strg + Q verbessern", "hopper")
			),
			cat("§hItem-Infos", "magnifying_glass",
				feat("Item-Zähler", "bundle"),
				feat("Karten-Vorschau", "filled_map"),
				feat("Reparaturwert anzeigen", "weakness"),
				feat("LuckyBlock-Typ anzeigen", "lucky_block"),
				feat("Kopf-Vorschau", "steve"),
				feat("Spawn-Ei-Typ anzeigen", "creeper_spawn_egg")
			),
			cat("§iItem-Schutz", "shield_with_sword",
				feat("Bei Rüstungsschaden warnen", "diamond_chestplate"),
				feat("Drachenei-Saver", "dragon_egg"),
				feat("Kisten-Saver", "chest"),
				feat("Orb-Saver", "orb"),
				feat("Partikel-Saver", "particle"),
				feat("Prefix-Saver", "tabping_colored"),
				feat("Rand-Saver", "glass_pane"),
				feat("Spezifischer Item-Saver", "shield_with_sword"),
				feat("Werkzeug-Saver", "tools"),
				feat("§z/kopf Vorschau", "steve")
			),
			feat("Recraft", "crafting_table")
		),

		cat("Render", "lens",
			cat("§gLicht", "light_bulb",
				feat("FullBright", "light_bulb"),
				feat("Lichtbugs anzeigen", "light_bulb_glitch"),
				feat("Lichtlevel anzeigen", "light_bulb")
			),
			cat("§hKöpfe", "steve",
				feat("Kopf-Texturen fixen", "skull_steve"),
				feat("Kopf-Verzauberung fixen", "enchanted_steve")
			),
			feat("§hOverlays entfernen", "glass_pane"),
			feat("Effekt-Partikel verstecken", "particle"),
			feat("Unsichtbare Entities anzeigen", "invisibility"),
			feat("Verbuggte Karten fixen", "filled_map")
		),

		cat("Spieler", "steve",
			cat("§gBewegung", "speed",
				feat("Automatisch sprinten", "speed"),
				feat("Sichereres Sneaken", "sneaking")
			),
			cat("§hNametags", "name_tag",
				feat("Clantags", "name_tag_rainbow"),
				feat("Nametag mit Prefix", "name_tag_rainbow"),
				feat("Nametags durch Wände anzeigen", "name_tag_yellow"),
				feat("Standard-Prefixe", "name_tag_yellow")
			),
			feat("Automatisch essen", "bone_with_meat"),
			feat("Automatisch nicken wenn AFK", "afk_timer"),
			feat("Items auf dem Kopf anzeigen", "firework_on_head"),
			feat("Spieler verstecken", "invisibility"),
			feat("Spielermenü aufräumen", "player_menu"),
			feat("Rüstung verstecken", "diamond_chestplate"),
			feat("Trusted MM-Liste", "player_green"),
			feat("§xScammerliste", "player_red"),
			feat("§yVerkleidung in 3rd Person", "mob_icons/faithless/creeper"),
			feat("§zWalking Minimes entfernen", "crossed_out_minime")
		),

		cat("Welt", "earth",
			cat("§gBuilding", "brick",
				feat("Ausrichten", "axes"),
				feat("Automatische Werkzeugauswahl", "tools"),
				feat("Barrieren anzeigen", "barrier"),
				feat("Blockauswahl mit Inventar", "mouse_middle"),
				feat("Ghost-Blöcke entfernen", "crossed_out_block_outline"),
				feat("Chunk-Grenzen anzeigen", "earth"),
				feat("Platzieren vereinfachen", "easy_place_overlay"),
				feat("Plot-Grenzen anzeigen", "earth"),
				feat("Redstone-Helfer", "redstone_comparator"),
				feat("Schematica verbessern", "litematica/litematica"),
				feat("Schilder verbessern", "sign"),
				feat("Verbrauchte Blöcke nachziehen", "bundle")
			),
			cat("§hJoining", "portal",
				feat("Automatisch /portal", "portal"),
				feat("Portal-Cooldown", "hourglass")
			),
			cat("§iScoreboard", "wooden_board",
				feat("Bankguthaben im Scoreboard", "bank"),
				feat("Orbguthaben im Scoreboard", "orb"),
				feat("Scoreboard aufräumen", "wooden_board"),
				feat("Scoreboard bei F3 verstecken", "wooden_board")
			),
			feat("Chunks geladen lassen", "filled_map"),
			feat("Flugbahn anzeigen", "crosshair"),
			feat("Interagierbare Plot-Schilder", "sign"),
			feat("Jail-Barrieren", "iron_bars"),
			feat("Joins anzeigen", "players"),
			feat("QR-Code Scanner", "qr_code"),
			feat("Spawner verbessern", "spawner")
		),

		feat("§yEinstellungen", "cog")
	};

	static HashMap<String, Feat> feats = new HashMap<>();

	static {
		for (Feat cat : CATS)
			register(cat);
	}

	public static Feat get(String in) {
		Feat feat = feats.get(in);
		if (feat == null)
			throw new IllegalStateException("Missing '" + in + "'");

		return feat;
	}

	static void register(Feat feat) {
		if (feats.put(feat.refName, feat) != null)
			throw new IllegalStateException("Duplicate '" + feat.nameLaby4 + "'");

		for (Feat feat1 : feat.feats)
			register(feat1);
	}

	static Feat cat(String name, String icon, String description, Class<? extends GUIEntry.SettingBuilder> builder, Feat... features) {
		return new Feat(name, name, icon, features, name, description, builder);
	}

	static Feat cat(String name, String icon, String description, Feat... features) {
		return new Feat(name, name, icon, features, name, description, GUIEntry.SwitchSettingBuilder.class);
	}

	static Feat cat(String name, String icon, Feat... features) {
		return new Feat(name, name, icon, features, name, "", GUIEntry.SwitchSettingBuilder.class);
	}

	static Feat feat(String name, String icon) {
		return new Feat(name, icon, new Feat[0]);
	}

	static Feat feat(String name, String nameLaby4, String icon) {
		return new Feat(name, nameLaby4, icon, new Feat[0], nameLaby4);
	}

	static Feat feat(String ref) {
		return new Feat(ref, null, new Feat[0]);
	}

	public static final class Feat {
		private final String nameLaby3;
		private final String nameLaby4;
		private final String icon;
		private final Feat[] feats;
		private final String refName;
		private Feat parent;
		private final String description;
		private final Class<? extends GUIEntry.SettingBuilder> setting;

		Feat(String nameLaby3, String nameLaby4, String icon, Feat[] feats, String refName, String description, Class<? extends GUIEntry.SettingBuilder> setting) {
			this.nameLaby3 = nameLaby3;
			this.nameLaby4 = nameLaby4;
			this.icon = icon;

			this.feats = feats;
			for (Feat feat : feats)
				feat.parent = this;

			this.refName = refName;
			this.description = description;
			this.setting = setting;
		}

		Feat(String nameLaby3, String nameLaby4, String icon, Feat[] feats, String refName) {
			this(nameLaby3, nameLaby4, icon, feats, refName, null, null);
		}

		Feat(String name, String icon, Feat[] feats) {
			this(name, name, icon, feats, name);
		}

		public String name() {
			return LABY_4.isActive() ? nameLaby4 : nameLaby3;
		}

		public String icon() {return icon;}

		public Feat parent() {return parent;}

		public Class<? extends GUIEntry.SettingBuilder> setting() {return setting;}

		public String description() {return description;}

		@Override
		public String toString() {
			return "Feat[" +
				"nameLaby3=" + nameLaby3 + ", " +
				"nameLaby4=" + nameLaby4 + ", " +
				"icon=" + icon + ", " +
				"parent=" + parent + ", " +
				"refName=" + refName + ']';
		}


	}
}
