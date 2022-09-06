package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.BooleanElement;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BooleanSetting extends BooleanElement implements SettingElementBuilder<BooleanSetting> {

    private final List<Consumer<Boolean>> callbacks = new ArrayList<>();
    private String configKey = null;
    private Boolean currentValue = null;

    public BooleanSetting() {
        super("§cNo name set", null, v -> {}, false);
        custom("An", "Aus");
        this.addCallback(newValue -> {
            this.currentValue = newValue;
            this.callbacks.forEach(c -> c.accept(newValue));
            if (configKey != null) {
                Config.set(this.configKey, newValue);
                Config.save();
            }
        });
        this.setSettingEnabled(true);
    }

    public Boolean get() {
        return currentValue;
    }

    public BooleanSetting set(boolean newValue) {
        this.currentValue = newValue;

        Reflection.set(this, newValue, BooleanElement.class, "currentValue");

        this.callbacks.forEach(c -> c.accept(newValue));
        if (configKey != null) {
            Config.set(this.configKey, newValue);
            Config.save();
        }
        return this;
    }

    public BooleanSetting defaultValue(boolean defaultValue) {
        if (this.currentValue == null) {
            set(defaultValue);
        }
        return this;
    }

    public BooleanSetting config(String configKey) {
        this.configKey = configKey;
        if (Config.has(configKey)) {
            set(Config.get(configKey).getAsBoolean());
        }
        return this;
    }

    public BooleanSetting callback(Consumer<Boolean> callback) {
        callbacks.add(callback);
        return this;
    }

    public BooleanSetting custom(String enabledText, String disabledText) {
        custom(new String[]{enabledText, disabledText});
        return this;
    }

    private ItemStack itemIcon = null;

    @Override
    public void itemIcon(ItemStack item) {
        this.itemIcon = item;
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        if(this.itemIcon != null) {
            LabyMod.getInstance().getDrawUtils().drawItem(this.itemIcon, x + 3, y + 2, null);
        }
        super.draw(x, y, maxX, maxY, mouseX, mouseY);
    }
}
