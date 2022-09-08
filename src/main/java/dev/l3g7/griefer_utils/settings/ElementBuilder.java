package dev.l3g7.griefer_utils.settings;

import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * An interface for builder-like setting creation.
 *
 * @param <S> The implementation and element class
 */
@SuppressWarnings("unchecked")
public interface ElementBuilder<S extends SettingsElement & ElementBuilder<S>> {

	default S name(String name) {
		((S) this).setDisplayName(name);
		return (S) this;
	}

	default S description(String description) {
		((S) this).setDescriptionText(description);
		return (S) this;
	}

	default S icon(Object icon) {
		if (!(this instanceof ControlElement))
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + "doesn't support icons!");

		IconData iconData;
		if (icon instanceof Material)
			iconData = new IconData((Material) icon);
		else if (icon instanceof String)
			iconData = new IconData("griefer_utils/icons/" + icon + ".png");
		else
			throw new UnsupportedOperationException(icon.getClass().getSimpleName() + " is an unsupported icon type!");

		Reflection.set(this, "iconData", iconData);
		return (S) this;
	}

	default S subSettings(SettingsElement... settings) {
		return subSettings(Arrays.asList(settings));
	}

	default S subSettings(List<SettingsElement> settings) {
		((S) this).getSubSettings().getElements().addAll(settings);
		return (S) this;
	}

	default S subSettingsWithHeader(String title, SettingsElement... settings) {
		((S) this).getSubSettings().getElements().clear();
		subSettings(
			new HeaderSetting("§r"),
			new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
			new HeaderSetting("§e§l" + title).scale(.7),
			new HeaderSetting("§r").scale(.4).entryHeight(10)
		);
		return subSettings(settings);
	}

	default S settingsEnabled(boolean settingsEnabled) {
		if (!(this instanceof ControlElement))
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + "doesn't support settings!");

		((ControlElement) this).setSettingEnabled(settingsEnabled);
		return (S) this;
	}

}
