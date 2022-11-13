package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public interface SettingElementBuilder<T extends SettingsElement> { // Not really an interface, just so I can still use extend

    default T name(String name) {
        ((T) this).setDisplayName(name);
        return (T) this;
    }

    default T description(String description) {
        ((T) this).setDescriptionText(description);
        return (T) this;
    }

    default void itemIcon(ItemStack item) {
        throw new UnsupportedOperationException("not implemented");
    }

    default T icon(Object icon) {
        if(this instanceof ControlElement) {
            ControlElement.IconData iconData;
            if(icon instanceof Material) {
                iconData = new ControlElement.IconData((Material) icon);
            } else if(icon instanceof ControlElement.IconData) {
                iconData = (ControlElement.IconData) icon;
            } else if(icon instanceof String) {
                iconData = new ControlElement.IconData("griefer_utils/icons/" + icon + ".png");
            } else if(icon instanceof ItemStack) {
                itemIcon((ItemStack) icon);
                iconData = new ControlElement.IconData("griefer_utils/icons/transparent.png");
            } else if(icon instanceof ItemBuilder) {
                return icon(((ItemBuilder) icon).build());
            } else if(icon instanceof ResourceLocation) {
	            iconData = new ControlElement.IconData(((ResourceLocation) icon));
            } else
                throw new UnsupportedOperationException(icon.getClass().toString() + " is an unsupported icon type!");
            Reflection.set(this, iconData, "iconData");
            return (T) this;
        }
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + "doesn't support icons!");
    }

    default T subSettings(SettingsElement... settings) {
        ((T) this).getSubSettings().getElements().addAll(Arrays.stream(settings).filter(Objects::nonNull).collect(Collectors.toList()));
        return (T) this;
    }

    default T subSettings(List<SettingsElement> settings) {
        ((T) this).getSubSettings().getElements().addAll(settings);
        return (T) this;
    }

    default T subSettingsWithHeader(String title, SettingsElement... settings) {
        List<SettingsElement> settingsElementList = ((T) this).getSubSettings().getElements();
        settingsElementList.clear();
        settingsElementList.add(new HeaderSetting("§r"));
        settingsElementList.add(new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3));
        settingsElementList.add(new HeaderSetting("§e§l" + title).scale(.7));
        settingsElementList.add(new HeaderSetting("§r").scale(.4).entryHeight(10));
        settingsElementList.addAll(Arrays.asList(settings));
        return (T) this;
    }

    default T settingsEnabled(boolean settingsEnabled) {
        if(this instanceof ControlElement) {
            ((ControlElement) this).setSettingEnabled(settingsEnabled);
            return (T) this;
        }
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + "doesn't support settings!");
    }
}
