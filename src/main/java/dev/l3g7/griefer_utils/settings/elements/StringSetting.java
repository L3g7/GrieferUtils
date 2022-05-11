package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.elements.StringElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StringSetting extends StringElement implements SettingElementBuilder<StringSetting> {

    private final List<Consumer<String>> callbacks = new ArrayList<>();
    private String configKey = null;
    private String currentValue = null;

    public StringSetting() {
        super("§cNo name set", null, "", v -> {});
        Reflection.set(this, (net.labymod.utils.Consumer<String>) newValue -> {
            currentValue = newValue;
            callbacks.forEach(c -> c.accept(newValue));
            if (configKey != null) {
                Config.set(configKey, newValue);
                Config.save();
            }
        }, "changeListener");
    }

    public String get() {
        return currentValue;
    }

    public StringSetting set(String newValue) {
        currentValue = newValue;

        Reflection.set(this, newValue, StringElement.class, "currentValue");
        Reflection.invoke(this, "updateValue");

        callbacks.forEach(c -> c.accept(newValue));
        if (configKey != null) {
            Config.set(configKey, newValue);
            Config.save();
        }
        return this;
    }

    public StringSetting defaultValue(String defaultValue) {
        if (currentValue == null) {
            set(defaultValue);
        }
        return this;
    }

    public StringSetting config(String configKey) {
        this.configKey = configKey;
        if (Config.has(configKey)) {
            set(Config.get(configKey).getAsString());
        }
        return this;
    }

    public StringSetting callback(Consumer<String> callback) {
        callbacks.add(callback);
        if (currentValue != null) callback.accept(currentValue);
        return this;
    }

    public void focus(boolean focus) {
        ModTextField textField = Reflection.get(this, "textField");
        textField.setCursorPositionEnd();
        textField.setFocused(focus);
    }
}
