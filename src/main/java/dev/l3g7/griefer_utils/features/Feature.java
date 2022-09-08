package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.settings.MainPage;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

/**
 * The base class for features.
 */
public abstract class Feature {

	public Feature(Category category) {
	}

	public abstract SettingsElement getMainElement();

	public boolean isCategoryEnabled() {
		return Category.FEATURE.setting.get();
	}

	public static Minecraft mc() {
		return Minecraft.getMinecraft();
	}

	public static EntityPlayerSP player() {
		return mc().thePlayer;
	}

	public Category getCategory() {
		return Category.FEATURE;
	}

	public enum Category {

		FEATURE(MainPage.features),
		TWEAK(null),
		MISC(null);

		public final BooleanSetting setting;

		Category(BooleanSetting setting) {
			this.setting = setting;
		}

	}

}
