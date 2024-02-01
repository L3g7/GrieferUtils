package dev.l3g7.griefer_utils.v1_8_9.util;

import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.v1_8_9.client.gui.screen.LabyScreenRenderer;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

public class LabyMod4Util {

	public static boolean isActivityOpen(Class<? extends Activity> clazz) {
		if (!(mc().currentScreen instanceof LabyScreenRenderer renderer))
			return false;

		return clazz.isInstance(renderer.screen());
	}

}
