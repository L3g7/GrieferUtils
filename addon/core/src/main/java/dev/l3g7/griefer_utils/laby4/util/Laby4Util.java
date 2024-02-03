/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.util;

import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.navigation.elements.ScreenNavigationElement;
import net.labymod.api.client.gui.screen.ScreenInstance;
import net.labymod.api.client.gui.screen.ScreenWrapper;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.activity.activities.labymod.child.SettingContentActivity;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.ScreenRendererWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.core.addon.AddonClassLoader;
import net.labymod.core.client.gui.navigation.elements.LabyModNavigationElement;
import net.labymod.core.client.gui.screen.activity.activities.NavigationActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.LabyModActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.SettingsActivity;

public class Laby4Util {

	public static boolean isSettingOpened(BaseSetting<?> setting) {
		// Check if in setting activity
		if (!(getActivity() instanceof NavigationActivity navActivity))
			return false;

		ScreenNavigationElement element = Reflection.get(navActivity, "element");
		if (!(element instanceof LabyModNavigationElement))
			return false;

		LabyModActivity activity = (LabyModActivity) element.getScreen();
		if (activity == null)
			return false;

		if (activity.getById("settings") != activity.getActiveTab())
			return false;

		// Extract current setting
		ScreenInstance instance = Reflection.get(activity.getActiveTab(), "instance");
		SettingsActivity settingsActivity = (SettingsActivity) instance;

		ScreenRendererWidget screenRendererWidget = Reflection.get(settingsActivity, "screenRendererWidget");
		ScreenInstance screen = screenRendererWidget.getScreen();
		if (!(screen instanceof SettingContentActivity settingContentActivity))
			return false;

		// Check if setting or parent matches
		Setting current = settingContentActivity.getCurrentHolder();
		while (current != null) {
			if (current == setting)
				return true;

			current = current.parent();
		}

		return false;
	}

	public static Activity getActivity() {
		ScreenWrapper screen = Laby.labyAPI().minecraft().minecraftWindow().currentScreen();

		if (screen == null || !screen.isActivity())
			return null;

		return screen.asActivity();
	}

	public static String getNamespace() {
		return ((AddonClassLoader) Laby4Util.class.getClassLoader()).getAddonInfo().getNamespace();
	}

}
