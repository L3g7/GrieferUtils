package dev.l3g7.griefer_utils.features.features.chat_menu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.ButtonSetting;
import net.labymod.core.LabyModCore;
import net.labymod.ingamechat.tabs.GuiChatNameHistory;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.l3g7.griefer_utils.features.features.chat_menu.ChatMenuEntry.Action.OPEN_URL;
import static dev.l3g7.griefer_utils.features.features.chat_menu.ChatMenuEntry.Action.RUN_CMD;

@Singleton
public class ChatMenu extends Feature {

	protected static final Map<ChatMenuEntry, String> DEFAULT_ENTRIES = new LinkedHashMap<ChatMenuEntry, String>() {{
		put(new ChatMenuEntry("Profil öffnen", RUN_CMD, "/profil %PLAYER%"), "open_profile");
		put(new ChatMenuEntry("Namensverlauf", s -> mc().displayGuiScreen(new GuiChatNameHistory("", s))), "name_history");
		put(new ChatMenuEntry("Namen kopieren", ChatMenu::copyToClipboard), "copy_name");
		put(new ChatMenuEntry("Im Forum suchen", OPEN_URL, "https://forum.griefergames.de/search/?q=%PLAYER%"), "search_forum");
		put(new ChatMenuEntry("Inventar öffnen", RUN_CMD, "/invsee %PLAYER%"), "open_inv");
		put(new ChatMenuEntry("Ausrüstung ansehen", RUN_CMD, "/view %PLAYER%"), "view_gear");
		put(new ChatMenuEntry("EC öffnen", RUN_CMD, "/ec %PLAYER%"), "open_ec");

		for (ChatMenuEntry entry : keySet())
			entry.enableDefault();
	}};

	protected static final List<ChatMenuEntry> customEntries = new ArrayList<>();
	protected static ChatMenuRenderer renderer = null;
	protected static boolean loaded = false;

	private static final ButtonSetting newEntrySetting = new ButtonSetting()
			.name("Neue Option erstellen")
			.callback(() -> {
				ChatMenuEntry newEntry = new ChatMenuEntry();
				path().add(newEntry.getSetting());
				customEntries.add(newEntry);
				mc().currentScreen.initGui();
			});

	private static final BooleanSetting enabled = new BooleanSetting()
			.name("Chatmenü")
			.defaultValue(false)
			.config("features.chat_menu.active")
			.icon("chat_menu/chat");

	public ChatMenu() {
		super(Category.FEATURE);
		loadEntries();
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	public static void saveEntries() {
		if (!loaded) // Don't save the config when starting
			return;

		for (ChatMenuEntry entry : DEFAULT_ENTRIES.keySet())
			Config.set("features.chat_menu.entries." + DEFAULT_ENTRIES.get(entry), entry.isEnabled());

		if (!customEntries.isEmpty()) {
			JsonArray array = new JsonArray();
			for (ChatMenuEntry customEntry : customEntries)
				if (customEntry.isValid())
					array.add(customEntry.toJson());

			Config.set("features.chat_menu.entries.custom", array);
		}

		Config.save();
	}

	protected static void updateSettings() {
		if (!loaded)
			return;

		List<SettingsElement> settings = new ArrayList<>();

		for (ChatMenuEntry entry : DEFAULT_ENTRIES.keySet())
			settings.add(entry.getSetting());

		for (ChatMenuEntry entry : customEntries) {
			if (!entry.isValid())
				continue;

			SettingsElement setting = entry.getSetting();
			setting.getSubSettings().add(new ButtonSetting()
					.name("Option entfernen")
					.callback(() -> {
						customEntries.remove(entry);
						settings.remove(setting);
						updateSettings();
						ArrayList<SettingsElement> list = path();
						list.remove(list.size() - 1);
						mc().currentScreen.initGui();
					}));
			settings.add(setting);
		}

		settings.add(newEntrySetting);

		enabled.subSettingsWithHeader("Chatmenü", settings.toArray(new SettingsElement[0]));
	}

	private void loadEntries() {

		for (ChatMenuEntry entry : DEFAULT_ENTRIES.keySet()) {
			String path = "features.chat_menu.entries." + DEFAULT_ENTRIES.get(entry);

			if (Config.has(path))
				entry.setEnabled(Config.get(path).getAsBoolean());
		}

		String path = "features.chat_menu.entries.custom";
		if (Config.has(path)) {
			for (JsonElement jsonElement : Config.get(path).getAsJsonArray()) {
				ChatMenuEntry entry = ChatMenuEntry.fromJson(jsonElement.getAsJsonObject());

				if (entry.isValid())
					customEntries.add(entry);
			}
		}

		loaded = true;
		updateSettings();
	}

	@SubscribeEvent
	public void onRender(TickEvent.RenderTickEvent event) {
		if (renderer != null)
			renderer.render();
	}

	@SubscribeEvent
	public void onMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (renderer != null && renderer.onMouse()) {
			renderer = null;
			return;
		}

		if (!Mouse.getEventButtonState())
			return;

		if (renderer != null && renderer.outOfBox()) {
			renderer = null;
		}

		if (!isActive() || !isOnGrieferGames() || Mouse.getEventButton() != 1)
			return;

		String value = LabyModCore.getMinecraft().getClickEventValue(Mouse.getX(), Mouse.getY());
		if (value == null || !value.startsWith("/msg "))
			return;

		List<ChatMenuEntry> entries = new ArrayList<>();
		DEFAULT_ENTRIES.keySet().stream().filter(ChatMenuEntry::isEnabled).forEach(entries::add);
		customEntries.stream().filter(e -> e.isEnabled() && e.isValid()).forEach(entries::add);

		renderer = new ChatMenuRenderer(entries, value.substring(5, value.length() - 1));
		event.setCanceled(true);
	}


	@SubscribeEvent
	public void onKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (renderer == null)
			return;

		if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == 1) {
			renderer = null;
			event.setCanceled(true);
		}

		mc().dispatchKeypresses();
	}

	public static void copyToClipboard(String text) {
		StringSelection selection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
		Feature.displayAchievement("\"" + text + "\"",  "wurde in die Zwischenablage kopiert.");
	}

}
