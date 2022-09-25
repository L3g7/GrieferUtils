package dev.l3g7.griefer_utils.features.misc;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.init.Blocks;

@Singleton
public class AutoUpdate extends Feature {

	public final BooleanSetting showUpdateScreen = new BooleanSetting()
		.name("Update-Screen anzeigen")
		.description("Ob ein Update-Screen angezeigt werden soll, wenn GrieferUtils geupdatet wurde.")
		.config("misc.auto_update.show_screen")
		.icon(new ItemBuilder(Blocks.stained_glass_pane).enchant())
		.defaultValue(true);

	public final BooleanSetting enabled = new BooleanSetting()
		.name("§1§fAutoUpdate")
		.description("Updatet GrieferUtils automatisch auf die neuste Version.")
		.config("misc.auto_update.active")
		.icon(new ControlElement.IconData("labymod/textures/settings/settings/serverlistliveview.png"))
		.defaultValue(true)
		.subSettingsWithHeader("AutoUpdate", showUpdateScreen);

	public AutoUpdate() {
		super(Category.MISC);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}
}
