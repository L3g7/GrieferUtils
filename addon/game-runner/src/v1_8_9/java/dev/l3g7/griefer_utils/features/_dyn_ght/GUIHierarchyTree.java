package dev.l3g7.griefer_utils.features._dyn_ght;

import dev.l3g7.griefer_utils.core.settings.GUIEntry;

import java.util.HashMap;

/**
 * Temporary class for dynamically reordering the GUI tree.
 */
@SuppressWarnings("SameParameterValue")
public class GUIHierarchyTree {

	static final XK chat = new XK("chat");
	static final CK chat_ingoing = new CK("chat.ingoing");
	static final CK chat_outgoing = new CK("chat.outgoing");
	static final CK chat_filter = new CK("chat.filter");
	static final CK guis = new CK("gui");
	static final CK guis_grieferGames = new CK("gui.griefer_games");
	static final CK guis_integrations = new CK("gui.integrations");
	static final CK guis_books = new CK("gui.books");
	static final XK item = new XK("item");
	static final XK item_inventoryTweaks = new XK("item.inventory_tweaks");
	static final XK item_itemInfo = new XK("item.item_info");
	static final XK item_itemSaver = new XK("item.item_saver");
	static final XK render = new XK("render");
	static final CK render_light = new CK("render.light");
	static final CK render_skulls = new CK("render.skulls");
	static final XK player = new XK("player");
	static final CK player_movement = new CK("player.movement");
	static final CK player_nametags = new CK("player.nametags");
	static final XK world = new XK("world");
	static final CK world_building = new CK("world.building");
	static final CK world_joining = new CK("world.joining");
	static final CK world_scoreboard = new CK("world.scoreboard");
	static final PK uncategorized = new PK(null);

	static Feat[] CATS = new Feat[]{
		cat("Chat", "chat", chat,
			cat("§gEingehender Chat", "chat", chat_ingoing,
				feat("Automatisch entnicken", "name_tag", chat),
				feat("Chat aufhellen", "light_bulb", chat),
				feat("Chat aufräumen", "crossed_out_book", chat),
				feat("Chatmenü", "player_menu", chat),
				feat("ChatReactor", "cpu", chat),
				feat("ChatTime", "clock", chat),
				feat("Echtgeld-Erkennung", "coin", chat),
				feat("Echtgeld- Erkennung", "coin", chat),
				feat("Interagierbare /freunde", "players", chat),
				feat("Interagierbare Nachrichten", "mouse_left", chat),
				feat("Kopf vor Nachrichten", "steve", chat),
				feat("Magischen Text entfernen", "tabping_colored", chat)
			),
			cat("§hAusgehender Chat", "book_and_quill", chat_outgoing,
				feat("AntiCommandChoker", "crossed_out_chat", chat),
				feat("/switch verbessern", "portal", chat),
				feat("Befehle loggen", "book_and_quill", chat),
				feat("Befehlsradialmenü", "command_menu", chat),
				feat("Befehlsvorschläge", "command_suggestions", chat),
				feat("Farb-Vorschau", "color_palette", chat),
				feat("Lange Nachrichten aufteilen", "shears", chat),
				feat("Multi-Hotkey", "key", chat),
				feat("Plot-Chat-Indikator", "chat_orange", chat),
				feat("Rechner", "calculator", chat),
				feat("SlowChat-Cooldown", "hourglass", chat)
			),
			cat("§iChat-Filter", "hopper", chat_filter,
				feat("Chat-Filter-Länge entsperren", "measurement", chat),
				feat("Filtervorlagen", "enchanted_book", chat),
				feat("Webhooks in Filtern", "discord", chat)
			)
		),
		cat("GUIs", "wooden_board", guis,
			cat("§gGrieferGames", "griefer_games", guis_grieferGames,
				feat("/homes verbessern", "filled_map", player),
				feat("/zuhause verbessern", "region_map", player),
				feat("Trichteranzeige verbessern", "hopper", world),
				feat("Adventurer verbessern", "enchanted_fire_charge", world),
				feat("Jobbörse verbessern", "wooden_board", world),
				feat("NPC-Klick-Helfer", "mouse_right", world),
				feat("Orb-Händler fixen", "orbseller", world),
				feat("Cooldown-Benachrichtigungen", "bell", player)
			),
			cat("§hIntegrationen", "cpu", guis_integrations,
				feat("Botshop-Gui", "high_res/byte_and_bit", uncategorized),
				feat("Biom- und Strukturen-Suche", "region_map", world),
				feat("§xGriefer.Info", "high_res/griefer_info", uncategorized)
			),
			cat("§iBücher", "enchanted_book", guis_books,
				feat("Bücher Öffnen fixen", "lectern", item),
				feat("Bücher unterdrücken", "crossed_out_book_read", player)
			),
			feat("Interagierbare Profile", "mouse_left", player),
			feat("Item-Suche", "magnifying_glass", world),
			feat("LabyMod-Switcher fixen", "labymod", player)
		),

		cat("Items", "gold_ingot", item,
			cat("§gInventar verbessern", "bundle", item_inventoryTweaks,
				feat("Besseres Shiften", "arrows_up", item_inventoryTweaks),
				feat("Strg + Q verbessern", "hopper", item_inventoryTweaks)
			),
			cat("§hItem-Infos", "magnifying_glass", item_itemInfo,
				feat("Item-Zähler", "bundle", item_itemInfo),
				feat("Karten-Vorschau", "filled_map", world),
				feat("Reparaturwert anzeigen", "weakness", item_itemInfo),
				feat("LuckyBlock-Typ anzeigen", "lucky_block", item_itemInfo),
				feat("Kopf-Vorschau", "steve", world),
				feat("Spawn-Ei-Typ anzeigen", "creeper_spawn_egg", item_itemInfo)
			),
			cat("§iItem-Schutz", "shield_with_sword", item_itemSaver,
				feat("Bei Rüstungsschaden warnen", "diamond_chestplate", item_itemSaver),
				feat("Drachenei-Saver", "dragon_egg", world),
				feat("Kisten-Saver", "chest", item_itemSaver),
				feat("Orb-Saver", "orb", item_itemSaver),
				feat("Partikel-Saver", "particle", item_itemSaver),
				feat("Prefix-Saver", "tabping_colored", item_itemSaver),
				feat("Rand-Saver", "glass_pane", item_itemSaver),
				feat("Spezifischer Item-Saver", "shield_with_sword", item_itemSaver),
				feat("Werkzeug-Saver", "tools", item_itemSaver),
				feat("§z/kopf Vorschau", "steve", item_itemSaver)
			),
			feat("Recraft", "crafting_table", item)
		),

		cat("Render", "lens", render,
			cat("§gLicht", "light_bulb", render_light,
				feat("FullBright", "light_bulb", render),
				feat("Lichtbugs anzeigen", "light_bulb_glitch", render),
				feat("Lichtlevel anzeigen", "light_bulb", render)
			),
			cat("§hKöpfe", "steve", render_skulls,
				feat("Kopf-Texturen fixen", "skull_steve", world),
				feat("Kopf-Verzauberung fixen", "enchanted_steve", render)
			),
			feat("§iOverlays entfernen", "glass_pane", render),
			feat("Effekt-Partikel verstecken", "particle", render),
			feat("Unsichtbare Entities anzeigen", "invisibility", render),
			feat("Verbuggte Karten fixen", "filled_map", world)
		),

		cat("Spieler", "steve", player,
			cat("§gBewegung", "speed", player_movement,
				feat("Automatisch sprinten", "speed", player),
				feat("Sichereres Sneaken", "sneaking", player)
			),
			cat("§hNametags", "name_tag", player_nametags,
				feat("Clantags", "name_tag_rainbow", render),
				feat("Nametag mit Prefix", "name_tag_rainbow", render),
				feat("Nametags durch Wände anzeigen", "name_tag_yellow", render),
				feat("Standard-Prefixe", "name_tag_yellow", chat)
			),
			feat("Automatisch essen", "bone_with_meat", player),
			feat("Automatisch nicken wenn AFK", "afk_timer", player),
			feat("Items auf dem Kopf anzeigen", "firework_on_head", player),
			feat("Spieler verstecken", "invisibility", render),
			feat("Spielermenü aufräumen", "player_menu", player),
			feat("Rüstung verstecken", "diamond_chestplate", player),
			feat("Trusted MM-Liste", "player_green", player),
			feat("§xScammerliste", "player_red", player),
			feat("§yVerkleidung in 3rd Person", "mob_icons/faithless/creeper", world),
			feat("§zWalking Minimes entfernen", "crossed_out_minime", render)
		),

		cat("Welt", "earth", world,
			cat("§gBuilding", "brick", world_building,
				feat("Ausrichten", "axes", player),
				feat("Automatische Werkzeugauswahl", "tools", item),
				feat("Barrieren anzeigen", "barrier", render),
				feat("Blockauswahl mit Inventar", "mouse_middle", item_inventoryTweaks),
				feat("Ghost-Blöcke entfernen", "crossed_out_block_outline", world),
				feat("Chunk-Grenzen anzeigen", "earth", world),
				feat("Platzieren vereinfachen", "easy_place_overlay", world),
				feat("Plot-Grenzen anzeigen", "earth", world),
				feat("Redstone-Helfer", "redstone_comparator", world),
				feat("Schematica verbessern", "litematica/litematica", world),
				feat("§7Schematica verbessern", "litematica/litematica", world),
				feat("Schilder verbessern", "sign", world),
				feat("Verbrauchte Blöcke nachziehen", "bundle", item_inventoryTweaks)
			),
			cat("§hJoining", "portal", world_joining,
				feat("Automatisch /portal", "portal", world),
				feat("Portal-Cooldown", "hourglass", world)
			),
			cat("§iScoreboard", "wooden_board", world_scoreboard,
				feat("Bankguthaben im Scoreboard", "bank", player),
				feat("Orbguthaben im Scoreboard", "orb", player),
				feat("Scoreboard aufräumen", "wooden_board", player),
				feat("Scoreboard bei F3 verstecken", "wooden_board", render)
			),
			feat("Chunks geladen lassen", "filled_map", world),
			feat("Flugbahn anzeigen", "crosshair", render),
			feat("Interagierbare Plot-Schilder", "sign", world),
			feat("Jail-Barrieren", "iron_bars", world),
			feat("Joins anzeigen", "players", world),
			feat("QR-Code Scanner", "qr_code", world),
			feat("Spawner verbessern", "spawner", world)
		),

		feat("§yEinstellungen", "cog", uncategorized)
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
		if (feats.put(feat.name, feat) != null)
			throw new IllegalStateException("Duplicate '" + feat.name + "'");

		for (Feat feat1 : feat.feats)
			register(feat1);
	}

	static <T extends K & CKI> Feat cat(String name, String icon, T key, Feat... features) {
		return new Feat(name, icon, features, "", key, GUIEntry.SwitchSettingBuilder.class);
	}

	static <T extends K & PKI> Feat feat(String name, String icon, T parentKey) {
		return new Feat(name, icon, new Feat[0], parentKey);
	}

	static class K {
		private final String key;

		K(String key) {this.key = key;}

		public String key() {return key;}
	}

	interface PKI {}
	interface CKI {}
	static class PK extends K implements PKI {
		PK(String key) {super(key);}
	}
	static class CK extends K implements CKI {
		CK(String key) {super(key);}
	}
	static class XK extends K implements CKI, PKI {
		XK(String key) {super(key);}
	}

	public static final class Feat {
		private final String name;
		private final String icon;
		private final Feat[] feats;
		private Feat parent;
		private final String description;
		private final K key;
		private final Class<? extends GUIEntry.SettingBuilder> setting;

		Feat(String name, String icon, Feat[] feats, String description, K key, Class<? extends GUIEntry.SettingBuilder> setting) {
			this.name = name;
			this.icon = icon;

			this.feats = feats;
			for (Feat feat : feats)
				feat.parent = this;

			this.description = description;
			this.key = key;
			this.setting = setting;
		}

		Feat(String name, String icon, Feat[] feats, K key) {
			this(name, icon, feats, null, key, null);
		}

		public String name() {
			return name;
		}

		public String icon() {return icon;}

		public String parentCfg() {
			if (key instanceof PKI)
				return key.key();
			else
				throw new UnsupportedOperationException("Cannot get parent cfg of CK");
		}

		public String config() {
			if (key instanceof CKI)
				return key.key();
			else
				throw new UnsupportedOperationException("Cannot get config of PK");
		}

		public Feat parent() {return parent;}

		public Class<? extends GUIEntry.SettingBuilder> setting() {return setting;}

		public String description() {return description;}

		@Override
		public String toString() {
			return "Feat[" +
				"name=" + name + ", " +
				"icon=" + icon + ", " +
				"parent=" + parent + ']';
		}


	}
}
