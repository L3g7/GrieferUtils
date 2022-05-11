package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.NumberElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NumberSetting extends NumberElement implements SettingElementBuilder<NumberSetting> {

    private final List<Consumer<Integer>> callbacks = new ArrayList<>();
    private String configKey = null;
    private Integer currentValue = null;

    public NumberSetting() {
        super("§cNo name set", null, 0);
        addCallback(newValue -> {
            currentValue = newValue;
            callbacks.forEach(c -> c.accept(newValue));
            if (configKey != null) {
                Config.set(configKey, newValue);
                Config.save();
            }
        });
    }

    public int get() {
        return currentValue;
    }

    public NumberSetting set(int newValue) {
        currentValue = newValue;

        Reflection.set(this, newValue, NumberElement.class, "currentValue");
        Reflection.invoke(this, "updateValue");

        callbacks.forEach(c -> c.accept(newValue));
        if (configKey != null) {
            Config.set(configKey, newValue);
            Config.save();
        }
        return this;
    }

    public NumberSetting defaultValue(int defaultValue) {
        if (currentValue == null) {
            set(defaultValue);
        }
        return this;
    }

    public NumberSetting config(String configKey) {
        this.configKey = configKey;
        if (Config.has(configKey)) {
            set(Config.get(configKey).getAsInt());
        }
        return this;
    }

    public NumberSetting callback(Consumer<Integer> callback) {
        callbacks.add(callback);
        return this;
    }

    public NumberSetting min(int min) {
        setMinValue(min);
        return this;
    }

    public NumberSetting max(int max) {
        setMaxValue(max);
        return this;
    }

}
