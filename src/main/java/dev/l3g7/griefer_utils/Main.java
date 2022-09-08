package dev.l3g7.griefer_utils;

import dev.l3g7.griefer_utils.settings.MainPage;
import net.labymod.api.LabyModAddon;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;

/**
 * description missing.
 */
public class Main extends LabyModAddon {

	@Override
	public void onEnable() {
		if (!LabyModCoreMod.isForge())
			return;

	}

	@Override
	public void loadConfig() {}

	@Override
	protected void fillSettings(List<SettingsElement> list) {
		if (!LabyModCoreMod.isForge())
			return;

		list.addAll(MainPage.settings);
	}

}
