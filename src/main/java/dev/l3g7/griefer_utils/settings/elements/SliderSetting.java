package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SliderElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SliderSetting extends SliderElement implements SettingElementBuilder<SliderSetting> {

    private final List<Consumer<Integer>> callbacks = new ArrayList<>();
    private String configKey = null;
    private Integer currentValue = null;

    public SliderSetting() {
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

    public SliderSetting set(int newValue) {
        currentValue = newValue;

        Reflection.set(this, newValue, SliderElement.class, "currentValue");

        callbacks.forEach(c -> c.accept(newValue));
        if (configKey != null) {
            Config.set(configKey, newValue);
            Config.save();
        }
        return this;
    }

    public SliderSetting defaultValue(int defaultValue) {
        if (currentValue == null) {
            set(defaultValue);
        }
        return this;
    }

    public SliderSetting config(String configKey) {
        this.configKey = configKey;
        if (Config.has(configKey)) {
            set(Config.get(configKey).getAsInt());
        }
        return this;
    }

    public SliderSetting callback(Consumer<Integer> callback) {
        callbacks.add(callback);
        return this;
    }

    public SliderSetting min(int min) {
        setMinValue(min);
        return this;
    }

    public SliderSetting max(int max) {
        setMaxValue(max);
        return this;
    }

}
