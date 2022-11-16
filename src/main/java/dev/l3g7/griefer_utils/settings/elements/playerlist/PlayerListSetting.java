package dev.l3g7.griefer_utils.settings.elements.playerlist;

import com.google.gson.JsonArray;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.SettingsUpdateEvent;
import dev.l3g7.griefer_utils.features.features.player_list.PlayerListProvider;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PlayerListSetting extends SettingsElement {

	private boolean initialized = false;

	private String configKey;
	private String namePattern;

	private final List<PlayerSetting> values = new ArrayList<>();

	public PlayerListSetting() {
		super("", null);
	}

	public PlayerListSetting name(String name) {
		this.namePattern = name;
		return this;
	}

	public PlayerListSetting config(String configKey) {
		this.configKey = configKey;

		if (Config.has(configKey))
			StreamSupport.stream(Config.get(configKey).getAsJsonArray().spliterator(), false)
				.map(PlayerSetting::fromJson)
				.forEach(values::add);
		return this;
	}

	public List<String> getNames() {
		return values.stream().map(StringSetting::get).collect(Collectors.toList());
	}

	public List<PlayerListProvider.PlayerListEntry> getEntries() {
		return values.stream()
			.map(PlayerSetting::toPlayerListEntry)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	private void saveConfig() {
		JsonArray json = new JsonArray();
		values.stream()
			.filter(p -> !p.get().isEmpty())
			.map(PlayerSetting::toJson)
			.forEach(json::add);

		Config.set(configKey, json);
		Config.save();
	}

	private PlayerSetting createPlayerSetting(int index) {
		PlayerSetting setting;

		setting = values.size() == index ? new PlayerSetting() : values.get(index);

		setting.name(String.format(namePattern, index + 1));
		setting.setPlayerSettingConsumer(value -> {
			while (values.size() <= index)
				values.add(new PlayerSetting());
			values.set(index, value);

			saveConfig();
		});

		return setting;
	}

	private void updateNames(List<SettingsElement> settings, List<SettingsElement> tempElements) {
		int startIndex = settings.indexOf(this) + 1;
		Optional<SettingsElement> nextInvalidElement = tempElements.stream().skip(startIndex).filter(e -> !(e instanceof PlayerSetting)).findAny();
		int endIndex = nextInvalidElement.map(settings::indexOf).orElse(settings.size());

		for (int i = startIndex; i < endIndex; i++) {
			boolean isFocused = ((ModTextField) Reflection.get(settings.get(i), "textField")).isFocused();
			PlayerSetting setting = createPlayerSetting(i - startIndex);
			setting.focus(isFocused);
			settings.set(i, setting);
		}

		tempElements.clear();
		tempElements.addAll(settings);
	}

	private void updateSettings(List<SettingsElement> settings, List<SettingsElement> tempElements) {
		// Get start and end of StringListSetting
		int startIndex = tempElements.indexOf(this) + 1;
		Optional<SettingsElement> nextInvalidElement = tempElements.stream().skip(startIndex).filter(e -> !(e instanceof PlayerSetting)).findAny();
		int endIndex = nextInvalidElement.map(tempElements::indexOf).orElse(-1);

		if (!initialized) {
			// Add all values
			for (int i = 0; i < values.size(); i++) {
				PlayerSetting setting = createPlayerSetting(i);
				settings.add(startIndex + i, setting);
                tempElements.add(startIndex + i, setting);
			}

			// Add empty setting
			PlayerSetting setting = createPlayerSetting(values.size());
			settings.add(startIndex + values.size(), setting);
	        tempElements.add(startIndex + values.size(), setting);
			values.add(setting);

			initialized = true;
		}

        // Filter list
        List<PlayerSetting> playerSettings = tempElements.stream()
                .skip(startIndex) // Ignore all elements before StringListElement
                .limit(endIndex == -1 ? Integer.MAX_VALUE : endIndex - startIndex)
                .map(PlayerSetting.class::cast)
                .collect(Collectors.toList());

        // Add new setting if last setting is not empty
        if (!playerSettings.get(playerSettings.size() - 1)
	        .get()
	        .isEmpty()) {

			PlayerSetting setting = createPlayerSetting(values.size());
	        tempElements.add(endIndex == -1 ? tempElements.size() : endIndex, setting);
	        settings.add(endIndex == -1 ? settings.size() : endIndex, setting);
			values.add(setting);

	        updateNames(settings, tempElements);
        }

        // Remove setting if empty
        for (int i = 0; i < playerSettings.size() - 1; i++) { // ignore last setting
            PlayerSetting setting = playerSettings.get(i);
            if (setting.get().isEmpty()) {
                playerSettings.get(i + 1).focus(true);

                tempElements.remove(setting);
	            settings.remove(setting);
                values.remove(i);

				updateNames(settings, tempElements);
				saveConfig();
                break;
            }
        }
	}

	// Make StringListSetting invisible
	public void drawDescription(int a, int b, int c) {}
	public int getEntryHeight() { return -2; } // Probably a 1px space between elements
	public void keyTyped(char a, int b) {}
	public void mouseClickMove(int a, int b, int c) {}
	public void mouseClicked(int a, int b, int c) {}
	public void mouseRelease(int a, int b, int c) {}
	public void unfocus(int a, int b, int c) {}

	@EventListener
	public static void onSettingsUpdate(SettingsUpdateEvent event) {
		// Trigger updateSettings if a StringListSetting is visible
		event.getTempElements().stream().filter(PlayerListSetting.class::isInstance)
			.map(PlayerListSetting.class::cast)
			.findAny().ifPresent(e -> e.updateSettings(event.getSettings(), event.getTempElements()));
	}

}