package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.DropDownElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropDownSetting<E extends Enum<E>> extends DropDownElement<E> implements SettingElementBuilder<DropDownSetting<E>> {

    private String configKey = null;
    private E currentValue = null;
    private final DropDownMenu<E> menu;
    private final Class<E> enumClass;
    private final List<Consumer<E>> callbacks = new ArrayList<>();


    public DropDownSetting(Class<E> enumClass) {
        super("§cNo name set", null);
        this.enumClass = enumClass;
        menu = new DropDownMenu<>("", 0, 0, 0, 0);
        menu.fill(enumClass.getEnumConstants());
        Reflection.set(this, menu, "dropDownMenu");
        setChangeListener(this::set);
    }

    @Override
    public DropDownSetting<E> name(String name) {
        menu.setTitle(name);
        return SettingElementBuilder.super.name(name);
    }

    public E get() {
        return menu.getSelected();
    }

    public DropDownSetting<E> set(E newValue) {
        menu.setSelected(newValue);
        currentValue = newValue;
        callbacks.forEach(c -> c.accept(newValue));
        if (configKey != null) {
            Config.set(configKey, newValue.name());
            Config.save();
        }
        return this;
    }

    public DropDownSetting<E> defaultValue(E defaultValue) {
        if(currentValue == null) {
            set(defaultValue);
        }
        return this;
    }

    public DropDownSetting<E> config(String configKey) {
        this.configKey = configKey;
        if(Config.has(configKey)) {
            set(Enum.valueOf(enumClass, Config.get(configKey).getAsString()));
        }
        return this;
    }

    public DropDownSetting<E> callback(Consumer<E> callback) {
        callbacks.add(callback);
        return this;
    }

    @SuppressWarnings("unchecked")
    public DropDownSetting<E> stringProvider(Function<E, String> function) {
        menu.setEntryDrawer((o, x, y, trimmedEntry) -> LabyMod.getInstance().getDrawUtils().drawString(function.apply((E) o), x, y));
        return this;
    }

}
