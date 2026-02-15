package dev.l3g7.griefer_utils.features._dyn_ght;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.settings.GUIEntry;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import net.labymod.api.client.gui.icon.Icon;
import net.minecraft.init.Items;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;

@SuppressWarnings("SameParameterValue")
public class GUIHierarchyTree {

	static Feat[] CATS = new Feat[]{
		cat("Chat", "XZRF:chat",
			cat("§gEingehender Chat", "XZRF:chat",
				feat("Automatisch entnicken", "XZRF:name_tag"),
				feat("Chat aufhellen", "XZRF:light_bulb"),
				feat("Chat aufräumen", "XZRF:crossed_out_book"),
				feat("Chatmenü", "XZRF:player_menu"),
				feat("ChatReactor", "XZRF:cpu"),
				feat("ChatTime", "XZRF:clock"),
				feat("Echtgeld- Erkennung", "Echtgeld-Erkennung", "XZRF:coin"),
				feat("Interagierbare /freunde", "XZRF:players"),
				feat("Interagierbare Nachrichten", "XZRF:mouse_left"),
				feat("Kopf vor Nachrichten", "XZRF:steve"),
				feat("Magischen Text entfernen", "XZRF:tabping_colored")
			),
			cat("§hAusgehender Chat", "XZRF:book_and_quill",
				feat("AntiCommandChoker", "XZRF:crossed_out_chat"),
				feat("/switch verbessern", "XZRF:portal"),
				feat("Befehle loggen", "XZRF:book_and_quill"),
				feat("Befehlsradialmenü", "XZRF:command_menu"),
				feat("Befehlsvorschläge", "XZRF:command_suggestions"),
				feat("Farb-Vorschau", "XZRF:color_palette"),
				feat("Lange Nachrichten aufteilen", "XZRF:shears"),
				feat("Multi-Hotkey", "XZRF:key"),
				feat("Plot-Chat-Indikator", "XZRF:chat_orange"),
				feat("Rechner", "XZRF:calculator"),
				feat("SlowChat-Cooldown", "XZRF:hourglass")
			),
			cat("§iChat-Filter", "XZRF:hopper",
				feat("Chat-Filter-Länge entsperren", "XZRF:measurement"),
				feat("Filtervorlagen", "XZRF:enchanted_book"),
				feat("Webhooks in Filtern", "XZRF:discord")
			)
		),
		cat("GUIs", "XZRF:wooden_board",
			cat("§gGrieferGames", "XZRF:griefer_games",
				feat("/homes verbessern", "XZRF:filled_map"),
				feat("/zuhause verbessern", "XZRF:region_map"),
				feat("Trichteranzeige verbessern", "XZRF:hopper"),
				feat("Adventurer verbessern", "XZRF:enchanted_fire_charge"),
				feat("Jobbörse verbessern", "XZRF:wooden_board"),
				feat("NPC-Klick-Helfer", "XZRF:mouse_right"),
				feat("Orb-Händler fixen", "XZRF:orbseller"),
				feat("Cooldown-Benachrichtigungen", "XZRF:bell")
			),
			cat("§hIntegrationen", "XZRF:cpu",
				feat("Botshop-Gui", "XZRF:high_res/byte_and_bit"),
				feat("Biom- und Strukturen-Suche", "XZRF:region_map"),
				feat("§xGriefer.Info", "XZRF:high_res/griefer_info")
			),
			cat("§iBücher", "XZRF:enchanted_book",
				feat("Bücher Öffnen fixen", "XZRF:lectern"),
				feat("Bücher unterdrücken", "XZRF:crossed_out_book_read")
			),
			feat("Interagierbare Profile", "XZRF:mouse_left"),
			feat("Item-Suche", "XZRF:magnifying_glass"),
			feat("LabyMod-Switcher fixen", "XZRF:labymod")
		),

		cat("Items", "XZRF:gold_ingot",
			cat("§gInventar verbessern", "XZRF:bundle",
				feat("Besseres Shiften", "XZRF:arrows_up"),
				feat("Strg + Q verbessern", "XZRF:hopper")
			),
			cat("§hItem-Infos", "XZRF:magnifying_glass",
				feat("Item-Zähler", "XZRF:bundle"),
				feat("Karten-Vorschau", "XZRF:filled_map"),
				feat("Reparaturwert anzeigen", "XZRF:weakness"),
				feat("LuckyBlock-Typ anzeigen", "XZRF:lucky_block"),
				feat("Kopf-Vorschau", "XZRF:steve"),
				feat("Spawn-Ei-Typ anzeigen", "XZRF:creeper_spawn_egg")
			),
			cat("§iItem-Schutz", "XZRF:shield_with_sword",
				feat("Bei Rüstungsschaden warnen", "XZRF:diamond_chestplate"),
				feat("Drachenei-Saver", "XZRF:dragon_egg"),
				feat("Kisten-Saver", "XZRF:chest"),
				feat("Orb-Saver", "XZRF:orb"),
				feat("Partikel-Saver", "XZRF:particle"),
				feat("Prefix-Saver", "XZRF:tabping_colored"),
				feat("Rand-Saver", "XZRF:glass_pane"),
				feat("Spezifischer Item-Saver", "XZRF:shield_with_sword"),
				feat("Werkzeug-Saver", "XZRF:tools"),
				feat("§z/kopf Vorschau", "XZRF:steve")
			),
			feat("Recraft", "XZRF:crafting_table")
		),

		cat("Render", "XZRF:lens",
			cat("§gLicht", "XZRF:light_bulb",
				feat("FullBright", "XZRF:light_bulb"),
				feat("Lichtbugs anzeigen", "XZRF:light_bulb_glitch"),
				feat("Lichtlevel anzeigen", "XZRF:light_bulb")
			),
			cat("§hKöpfe", "XZRF:steve",
				feat("Kopf-Texturen fixen", "XZRF:skull_steve"),
				feat("Kopf-Verzauberung fixen", "XZRF:enchanted_steve")
			),
			feat("§hOverlays entfernen", "XZRF:glass_pane"),
			feat("Effekt-Partikel verstecken", "XZRF:particle"),
			feat("Unsichtbare Entities anzeigen", "XZRF:invisibility"),
			feat("Verbuggte Karten fixen", "XZRF:filled_map")
		),

		cat("Spieler", "XZRF:steve",
			cat("§gBewegung", "XZRF:speed",
				feat("Automatisch sprinten", "XZRF:speed"),
				feat("Sichereres Sneaken", "XZRF:sneaking")
			),
			cat("§hNametags", "XZRF:name_tag",
				feat("Clantags", "XZRF:name_tag_rainbow"),
				feat("Nametag mit Prefix", "XZRF:name_tag_rainbow"),
				feat("Nametags durch Wände anzeigen", "XZRF:name_tag_yellow"),
				feat("Standard-Prefixe", "XZRF:name_tag_yellow")
			),
			feat("Automatisch essen", "XZRF:bone_with_meat"),
			feat("Automatisch nicken wenn AFK", "XZRF:afk_timer"),
			feat("Items auf dem Kopf anzeigen", "XZRF:firework_on_head"),
			feat("Spieler verstecken", "XZRF:invisibility"),
			feat("Spielermenü aufräumen", "XZRF:player_menu"),
			feat("Rüstung verstecken", "XZRF:diamond_chestplate"),
			feat("Trusted MM-Liste", "XZRF:player_green"),
			feat("§xScammerliste", "XZRF:player_red"),
			feat("§yVerkleidung in 3rd Person", "XZRF:mob_icons/faithless/creeper"),
			feat("§zWalking Minimes entfernen", "XZRF:crossed_out_minime")
		),

		cat("Welt", "XZRF:earth",
			cat("§gBuilding", "XZRF:brick",
				feat("Ausrichten", "XZRF:axes"),
				feat("Automatische Werkzeugauswahl", "XZRF:tools"),
				feat("Barrieren anzeigen", "XZRF:barrier"),
				feat("Blockauswahl mit Inventar", "XZRF:mouse_middle"),
				feat("Ghost-Blöcke entfernen", "XZRF:crossed_out_block_outline"),
				feat("Chunk-Grenzen anzeigen", "XZRF:earth"),
				feat("Platzieren vereinfachen", "XZRF:easy_place_overlay"),
				feat("Plot-Grenzen anzeigen", "XZRF:earth"),
				feat("Redstone-Helfer", "XZRF:redstone_comparator"),
				feat("Schematica verbessern", "XZRF:litematica/litematica"),
				feat("Schilder verbessern", "XZRF:sign"),
				feat("Verbrauchte Blöcke nachziehen", "XZRF:bundle")
			),
			cat("§hJoining", "XZRF:portal",
				feat("Automatisch /portal", "XZRF:portal"),
				feat("Portal-Cooldown", "XZRF:hourglass")
			),
			cat("§iScoreboard", "XZRF:wooden_board",
				feat("Bankguthaben im Scoreboard", "XZRF:bank"),
				feat("Orbguthaben im Scoreboard", "XZRF:orb"),
				feat("Scoreboard bei F3 verstecken", "XZRF:wooden_board")
			),
			feat("Chunks geladen lassen", "XZRF:filled_map"),
			feat("Flugbahn anzeigen", "XZRF:crosshair"),
			feat("Interagierbare Plot-Schilder", "XZRF:sign"),
			feat("Jail-Barrieren", "XZRF:iron_bars"),
			feat("Joins anzeigen", "XZRF:players"),
			feat("QR-Code Scanner", "XZRF:qr_code"),
			feat("Spawner verbessern", "XZRF:spawner")
		),

		feat("§yEinstellungen", "XZRF:cog")
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

	public static Icon handleXZRF(Object icon) {
		if (icon == null)
			return null;

		if (icon instanceof Icon) {
			return Icons.of(icon);
		} else if (!(icon instanceof String s)) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new Throwable().printStackTrace(new PrintStream(out));

			String stacktrace = out.toString(StandardCharsets.UTF_8);
			if (!stacktrace.contains("types.CitybuildSettingImpl.<init>") && !stacktrace.contains("laby4.ItemProtectionListSetting.getElements"))
				new Throwable("Icon NonString '" + icon + "'").printStackTrace();

			return Icons.of(Items.apple);
		} else if (!s.startsWith("XZRF:")) {
			new Throwable("Icon NonXZRF '" + icon + "'").printStackTrace();
			return Icons.of(Items.apple);
		} else {
			return Icons.of(handleXZRF(s));
		}
	}

	public static String handleXZRF(String icon) {
		if (icon == null)
			return null;

		if (!icon.startsWith("XZRF:")) {
			new Throwable("Icon NonXZRF '" + icon + "'").printStackTrace();
			return "barrier";
		} else {
			String path = Path.of("assets/griefer_utils/icons/" + icon.substring("XZRF:".length()) + ".png").normalize().toString().replace('\\', '/');
			if (!FileProvider.getFiles().contains(path))
				new Throwable("Icon missing '" + icon + "'").printStackTrace();

			return icon.substring("XZRF:".length());
		}
	}

	public static final class Feat {
		private String nameLaby3;
		private String nameLaby4;
		private String icon;
		private final Feat[] feats;
		private final String refName;
		private Feat parent;
		private String description;
		private Class<? extends GUIEntry.SettingBuilder> setting;

		Feat(String nameLaby3, String nameLaby4, String icon, Feat[] feats, String refName, String description, Class<? extends GUIEntry.SettingBuilder> setting) {
			this.nameLaby3 = nameLaby3;
			this.nameLaby4 = nameLaby4;
			this.icon = icon;

			this.feats = feats;
			for (Feat feat : feats) {
				feat.parent = this;
			}
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

		public String nameLaby3() {return nameLaby3;}

		public String nameLaby4() {return nameLaby4;}

		public String icon() {return icon;}

		public Feat[] feats() {return feats;}

		public Feat parent() {return parent;}

		public Class<? extends GUIEntry.SettingBuilder> setting() {return setting;}

		public String description() {return description;}

		public String refName() {return refName;}

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
