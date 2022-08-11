package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.gui.elements.CheckBox;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ColorPickerCheckBoxBulkElement;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.labymod.gui.elements.CheckBox.EnumCheckBoxValue.DISABLED;
import static net.labymod.gui.elements.CheckBox.EnumCheckBoxValue.INDETERMINATE;

public class RadioSetting<E extends Enum<E>> extends ColorPickerCheckBoxBulkElement implements SettingElementBuilder<RadioSetting<E>> {

    private String configKey = null;
    private E currentValue = null;
    private final Class<E> enumClass;
    private final Map<E, CheckBox> checkBoxes = new HashMap<>();
    private final List<CheckBox> checkBoxList = new ArrayList<>(); // also storing checkboxes as list because checkBoxes.values() doesn't seem to be ordered
    private final List<Consumer<E>> callbacks = new ArrayList<>();

    public RadioSetting(Class<E> enumClass) {
        super("§4");
        this.enumClass = enumClass;
        for(E e : enumClass.getEnumConstants()) {
            CheckBox cb = new CheckBox(e.name(), DISABLED, () -> DISABLED, 0, 0, 20, 20);
            cb.setUpdateListener(value -> {
                switch(value) {
                    case DEFAULT:
                    case ENABLED:
                        cb.updateValue(INDETERMINATE);
                        break;
                    case DISABLED:
                        if(checkBoxes.values().stream().noneMatch(c -> c != cb && c.getValue() == INDETERMINATE))
                            cb.updateValue(INDETERMINATE);
                        break;
                    case INDETERMINATE: {
                        for (CheckBox checkBox : checkBoxes.values()) {
                            if(checkBox != cb)
                                checkBox.updateValue(DISABLED);
                        }

                        currentValue = e;
                        callbacks.forEach(c -> c.accept(e));

                        if(configKey == null)
                            return;

                        Config.set(configKey, e.name());
                        Config.save();
                        break;
                    }
                }
            });
            addCheckbox(cb);
            checkBoxes.put(e, cb);
            checkBoxList.add(cb);
        }
        Collections.reverse(checkBoxList);
    }

    public E get() {
        return currentValue;
    }

    public RadioSetting<E> set(E newValue) {
        if(currentValue == newValue) return this;
        if(currentValue != null) {
            checkBoxes.get(currentValue).updateValue(DISABLED);
        }
        checkBoxes.get(newValue).updateValue(INDETERMINATE);
        if(configKey != null)
            Config.set(configKey, newValue.name());
        callbacks.forEach(c -> c.accept(newValue));
        return this;
    }

    public RadioSetting<E> defaultValue(E defaultValue) {
        if(currentValue == null) {
            set(defaultValue);
        }
        return this;
    }

    public RadioSetting<E> config(String configKey) {
        this.configKey = configKey;
        if(Config.has(configKey)) {
            set(Enum.valueOf(enumClass, Config.get(configKey).getAsString()));
        }
        return this;
    }

    public RadioSetting<E> stringProvider(Function<E, String> function) {
        for(Map.Entry<E, CheckBox> entry : checkBoxes.entrySet()) {
            Reflection.set(entry.getValue(), function.apply(entry.getKey()), "title");
        }
        return this;
    }

    public RadioSetting<E> callback(Consumer<E> callback) {
        callbacks.add(callback);
        return this;
    }

    private ItemStack itemIcon = null;

    @Override
    public void itemIcon(ItemStack item) {
        itemIcon = item;
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        if(itemIcon != null) {
            LabyMod.getInstance().getDrawUtils().drawItem(itemIcon, x + 3, y + 2, null);
        }
        if (iconData != null) {
            if (iconData.hasTextureIcon()) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(iconData.getTextureIcon());
                LabyMod.getInstance().getDrawUtils().drawTexture(x + 3, y + 3, 256.0D, 256.0D, 16.0D, 16.0D);
            } else if (iconData.hasMaterialIcon()) {
                LabyMod.getInstance().getDrawUtils().drawItem(iconData.getMaterialIcon().createItemStack(), x + 3, y + 2, null);
            }
        }

        LabyMod.getInstance().getDrawUtils().drawRectangle(x, y, maxX, maxY, ModColor.toRGB(80, 80, 80, 30));

        mc.fontRendererObj.drawString(displayName, x + 25, y + 9, 0xFFFFFF);

        int c = 0;
        for(CheckBox cb : checkBoxList) {
            cb.setX(maxX - (c += Math.max(Minecraft.getMinecraft().fontRendererObj.getStringWidth(cb.getTitle()) - cb.getWidth() * 2 + 5, cb.getWidth() + 5)));
            cb.setY(y + 5);
            cb.drawCheckbox(mouseX, mouseY);
        }
    }

    @Override
    public int getEntryHeight() {
        return 25;
    }

}
