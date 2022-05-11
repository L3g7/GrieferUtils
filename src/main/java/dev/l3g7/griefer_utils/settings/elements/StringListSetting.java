/*

The code here is extremely ugly, feel free to improve ;D
It was written ages ago, but I'm too lazy to rewrite everything

 */

package dev.l3g7.griefer_utils.settings.elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.SettingsUpdateEvent;
import dev.l3g7.griefer_utils.misc.Config;
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
            StreamSupport.stream(Config.get(configKey).getAsJsonArray().spliterator(), true).map(JsonElement::getAsString).forEach(values::add);
        return this;
    }

    public String getConfigKey() {
        return configKey;
    }

    public List<String> getValues() {
        return values;
    }

    private StringSetting createStringSetting(int index) {
        return new StringSetting()
                .name(String.format(namePattern, index + 1))
                .icon(icon)
                .defaultValue(values.size() > index ? values.get(index) : "")
                .callback(value -> {
                    while (values.size() <= index)
                        values.add("");
                    values.set(index, value);

                    // Save
                    JsonArray json = new JsonArray();
                    values.stream().filter(s -> !s.isEmpty()).map(JsonPrimitive::new).forEach(json::add);

                    Config.set(getConfigKey(), json);
                    Config.save();
                });
    }

    private void updateSettings(List<SettingsElement> list) {
        // Get start and end of StringListSetting
        int startIndex = list.indexOf(this) + 1;
        Optional<SettingsElement> nextInvalidElement = list.stream().skip(startIndex).filter(e -> !(e instanceof StringSetting)).findAny();
        int endIndex = nextInvalidElement.map(list::indexOf).orElse(-1);


        if (!initialized) {
            // Add all values
            for (int i = 0; i < values.size(); i++)
                list.add(startIndex + i + 1, createStringSetting(i));

            // Add empty setting
            list.add(startIndex + values.size() + 1, createStringSetting(values.size()));
            initialized = true;
        }

        // Filter list
        List<StringSetting> stringSettings = list.stream()
                .skip(startIndex) // Ignore all elements before StringListElement
                .limit(endIndex == -1 ? Integer.MAX_VALUE : endIndex - startIndex)
                .map(StringSetting.class::cast)
                .collect(Collectors.toList());

        // Add new setting if last setting is not empty
        if (!stringSettings.get(stringSettings.size() - 1).get().isEmpty())
            list.add(endIndex, createStringSetting(values.size()));


        // Remove setting if empty
        for (int i = 0; i < stringSettings.size() - 1; i++) { // ignore last setting
            StringSetting setting = stringSettings.get(i);
            if (setting.get().isEmpty()) {
                stringSettings.get(i + 1).focus(true);
                list.remove(setting);
                values.remove(i);
                break;
            }
        }
    }

    // Make StringListSetting invisible
    public void drawDescription(int a, int b, int c) {}
    public int getEntryHeight() { return -2; } // Weird bug in LabyMod, probably a 1px space between elements
    public void keyTyped(char a, int b) {}
    public void mouseClickMove(int a, int b, int c) {}
    public void mouseClicked(int a, int b, int c) {}
    public void mouseRelease(int a, int b, int c) {}
    public void unfocus(int a, int b, int c) {}

    @EventListener
    public static void onSettingsUpdate(SettingsUpdateEvent event) {
        // Trigger updateSettings on every visible StringListSetting
        event.getList().stream().filter(StringListSetting.class::isInstance)
                .map(StringListSetting.class::cast)
                .findAny().ifPresent(e -> e.updateSettings(event.getList()));
    }

}