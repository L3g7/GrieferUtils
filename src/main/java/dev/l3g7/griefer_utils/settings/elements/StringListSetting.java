package dev.l3g7.griefer_utils.settings.elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.SettingsUpdateEvent;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class StringListSetting extends SettingsElement {

	private boolean initialized = false;

	private String configKey;
	private String namePattern;
	private Object icon;

	private final List<String> values = new ArrayList<>();

	public StringListSetting() {
		super("", null);
	}

	public StringListSetting name(String name) {
		this.namePattern = name;
		return this;
	}

	public StringListSetting icon(Object icon) {
		this.icon = icon;
		return this;
	}

	public StringListSetting config(String configKey) {
		this.configKey = configKey;

		if (Config.has(configKey))
			StreamSupport.stream(Config.get(configKey).getAsJsonArray().spliterator(), false).map(JsonElement::getAsString).forEach(values::add);
		return this;
	}

	public List<String> getValues() {
		values.removeIf(String::isEmpty);
		return values;
	}

	private void saveConfig() {
		JsonArray json = new JsonArray();
		values.stream().filter(s -> !s.isEmpty()).map(JsonPrimitive::new).forEach(json::add);

		Config.set(configKey, json);
		Config.save();
	}

	private StringSetting createStringSetting(int index) {
		return new StringSetting()
			.name(String.format(namePattern, index + 1))
			.icon(icon)
			.defaultValue(values.size() == index ? "" : values.get(index))
			.callback(value -> {
				while (values.size() <= index)
					values.add("");
				values.set(index, value);

				saveConfig();
			});
	}

	private void updateNames(List<SettingsElement> settings, List<SettingsElement> tempElements) {
		int startIndex = settings.indexOf(this) + 1;
		Optional<SettingsElement> nextInvalidElement = tempElements.stream().skip(startIndex).filter(e -> !(e instanceof StringSetting)).findAny();
		int endIndex = nextInvalidElement.map(settings::indexOf).orElse(settings.size());

		for (int i = startIndex; i < endIndex; i++) {
			boolean isFocused = ((ModTextField) Reflection.get(settings.get(i), "textField")).isFocused();
			StringSetting setting = createStringSetting(i - startIndex);
			setting.focus(isFocused);
			settings.set(i, setting);
		}

		tempElements.clear();
		tempElements.addAll(settings);
	}

	private void updateSettings(List<SettingsElement> settings, List<SettingsElement> tempElements) {
		// Get start and end of StringListSetting
		int startIndex = tempElements.indexOf(this) + 1;
		Optional<SettingsElement> nextInvalidElement = tempElements.stream().skip(startIndex).filter(e -> !(e instanceof StringSetting)).findAny();
		int endIndex = nextInvalidElement.map(tempElements::indexOf).orElse(-1);


		if (!initialized) {
			// Add all values
			for (int i = 0; i < values.size(); i++) {
				StringSetting setting = createStringSetting(i);
				settings.add(startIndex + i, setting);
                tempElements.add(startIndex + i, setting);
			}

			// Add empty setting
			StringSetting setting = createStringSetting(values.size());
			settings.add(startIndex + values.size() - 1, setting);
	        tempElements.add(startIndex + values.size() - 1, setting);

			initialized = true;
		}

        // Filter list
        List<StringSetting> stringSettings = tempElements.stream()
                .skip(startIndex) // Ignore all elements before StringListElement
                .limit(endIndex == -1 ? Integer.MAX_VALUE : endIndex - startIndex)
                .map(StringSetting.class::cast)
                .collect(Collectors.toList());

        // Add new setting if last setting is not empty
        if (!stringSettings.get(stringSettings.size() - 1).get().isEmpty()) {

			StringSetting setting = createStringSetting(values.size());
	        tempElements.add(endIndex == -1 ? tempElements.size() : endIndex, setting);
	        settings.add(endIndex == -1 ? settings.size() : endIndex, setting);

	        updateNames(settings, tempElements);
        }

        // Remove setting if empty
        for (int i = 0; i < stringSettings.size() - 1; i++) { // ignore last setting
            StringSetting setting = stringSettings.get(i);
            if (setting.get().isEmpty()) {
                stringSettings.get(i + 1).focus(true);

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
		event.getTempElements().stream().filter(StringListSetting.class::isInstance)
			.map(StringListSetting.class::cast)
			.findAny().ifPresent(e -> e.updateSettings(event.getSettings(), event.getTempElements()));
	}

}