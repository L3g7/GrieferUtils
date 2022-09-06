package dev.l3g7.griefer_utils.features.features.chat_reactor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.ButtonSetting;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ChatReactor extends Feature {

	private static boolean loaded = false;
	private static final List<ChatReactorEntry> entries = new ArrayList<>();

	private static final ButtonSetting newEntrySetting = new ButtonSetting()
			.name("Neue Reaktion erstellen")
			.callback(() -> {
				ChatReactorEntry newEntry = new ChatReactorEntry();
				path().add(newEntry.getSetting());
				entries.add(newEntry);
				mc().currentScreen.initGui();
			});

	private static final BooleanSetting enabled = new BooleanSetting()
			.name("ChatReactor")
			.defaultValue(false)
			.config("features.chat_reactor.active")
			.icon("siren");

	public ChatReactor() {
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

		if (!entries.isEmpty()) {
			JsonArray array = new JsonArray();
			for (ChatReactorEntry entry : entries)
				if (entry.isValid())
					array.add(entry.toJson());

			Config.set("features.chat_reactor.entries", array);
		}

		Config.save();
	}

	protected static void updateSettings() {
		if (!loaded)
			return;

		List<SettingsElement> settings = new ArrayList<>();

		for (ChatReactorEntry entry : entries) {
			if (!entry.isValid())
				continue;

			SettingsElement setting = entry.getSetting();
			setting.getSubSettings().add(new ButtonSetting()
					.name("Reaktion entfernen")
					.callback(() -> {
						entries.remove(entry);
						settings.remove(setting);
						saveEntries();
						updateSettings();
						path().remove(path().size() - 1);
						mc().currentScreen.initGui();
					}));
			settings.add(setting);
		}

		settings.add(newEntrySetting);
		enabled.subSettingsWithHeader("ChatReactor", settings.toArray(new SettingsElement[0]));
	}

	private void loadEntries() {

		String path = "features.chat_reactor.entries";
		if (Config.has(path)) {
			for (JsonElement jsonElement : Config.get(path).getAsJsonArray()) {
				ChatReactorEntry entry = ChatReactorEntry.fromJson(jsonElement.getAsJsonObject());

				if (!entry.isValid())
					continue;

				entries.add(entry);
			}
		}

		loaded = true;
		updateSettings();
	}

	@EventListener
	public void onMsg(MessageReceiveEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		for (ChatReactorEntry entry : entries)
			entry.checkMatch(event.getUnformatted());
	}
}
