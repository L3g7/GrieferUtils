package dev.l3g7.griefer_utils.settings;

import dev.l3g7.griefer_utils.features.player.ArmorBreakWarning;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The main page of the addon's settings.
 */
public class MainPage {


	public static final BooleanSetting features = new BooleanSetting()
		.name("Features")
		.icon("cpu")
		.defaultValue(true)
		.config("features.active")
		.subSettings(
			new HeaderSetting("§r"),
			new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
			new HeaderSetting("§e§lFeatures").scale(.7),
			new HeaderSetting("§r").scale(.4).entryHeight(10));


	public static final List<SettingsElement> settings = new ArrayList<>(Arrays.asList(
		new HeaderSetting("§r"),
		new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
		new HeaderSetting("§e§lStartseite").scale(.7),
		new HeaderSetting("§r").scale(.4).entryHeight(10),
		features));

	static {
		ArmorBreakWarning armorBreakWarning = new ArmorBreakWarning();
		features.subSettings(armorBreakWarning.getMainElement());
		MinecraftForge.EVENT_BUS.register(armorBreakWarning);
	}

}
