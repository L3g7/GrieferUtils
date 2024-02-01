package dev.l3g7.griefer_utils.v1_8_9.util;

import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import net.labymod.api.client.gui.screen.LabyScreen;
import net.labymod.api.client.gui.screen.ScreenInstance;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.activities.labymod.child.SettingContentActivity;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.ScreenRendererWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.core.client.gui.navigation.elements.LabyModNavigationElement;
import net.labymod.core.client.gui.screen.activity.activities.NavigationActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.LabyModActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.SettingsActivity;
import net.labymod.v1_8_9.client.gui.screen.LabyScreenRenderer;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

public class LabyMod4Util {

	public static boolean isActivityOpen(Class<? extends Activity> clazz) {
		return clazz.isInstance(getActivity());
	}

	public static boolean isSettingOpened(BaseSetting<?> setting) {
		Setting current = getCurrentSetting();
		while (current != null) {
			if (current == setting)
				return true;

			current = current.parent();
		}

		return false;
	}

	private static LabyScreen getActivity() {
		if (!(mc().currentScreen instanceof LabyScreenRenderer renderer))
			return null;

		return renderer.screen();
	}

	public static Setting getCurrentSetting() {
		LabyScreen labyScreen = getActivity();
		if (!(labyScreen instanceof NavigationActivity navActivity))
			return null;

		LabyModNavigationElement navElement = Reflection.get(navActivity, "element");
		LabyModActivity activity = (LabyModActivity) navElement.getScreen();
		if (activity == null)
			return null;

		if (activity.getById("settings") != activity.getActiveTab())
			return null;

		ScreenInstance instance = Reflection.get(activity.getActiveTab(), "instance");
		SettingsActivity settingsActivity = (SettingsActivity) instance;

		ScreenRendererWidget screenRendererWidget = Reflection.get(settingsActivity, "screenRendererWidget");
		ScreenInstance screen = screenRendererWidget.getScreen();
		if (!(screen instanceof SettingContentActivity settingContentActivity))
			return null;

		return settingContentActivity.getCurrentHolder();
	}

}
