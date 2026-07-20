/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.util;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingActivityInitEvent;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.navigation.elements.ScreenBaseNavigationElement;
import net.labymod.api.client.gui.screen.ScreenWrapper;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.ScreenRendererWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.SettingElement;
import net.labymod.core.addon.AddonClassLoader;
import net.labymod.core.client.gui.navigation.elements.LabyModNavigationElement;
import net.labymod.core.client.gui.screen.activity.activities.NavigationActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.mods.ModsEditorActivity;

public class Laby4Util {

	private static SettingActivityInitEvent lastSettingActivityInitEvent;

	@EventListener
	private static void onSettingActivityInitEvent(SettingActivityInitEvent event) {
		lastSettingActivityInitEvent = event;
	}

	public static boolean isSettingOpened(BaseSetting<?> setting) {
		// Check if in setting activity
		if (!(getActivity() instanceof NavigationActivity navActivity))
			return false;

		ScreenBaseNavigationElement<?> element = Reflection.get(navActivity, "element");
		if (!(element instanceof LabyModNavigationElement))
			return false;

		if (!(element.getScreen() instanceof ModsEditorActivity activity))
			return false;

		ScreenRendererWidget panelRenderer = (ScreenRendererWidget) activity.document().getChild("mods-panel-renderer");
		if (!panelRenderer.isVisible())
			return false;

		Iterable<SettingElement> openSettings = Reflection.get(panelRenderer.getScreen(), "openSettings");

		Setting current = openSettings.iterator().next();
		while (current != null) {
			if (current == setting)
				return true;

			current = current.parent();
		}

		return false;
	}

	public static boolean isVanillaTheme() {
		return !Laby.labyAPI().themeService().currentTheme().getId().equals("fancy");
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

	public static void setPageTitle(String title) { // NOTE: Check parent
		if (lastSettingActivityInitEvent == null)
			return;

		ComponentWidget widget = lastSettingActivityInitEvent.get("setting-header", "title");
		if (widget != null && widget.renderable() != null)
			Reflection.set(widget.renderable(), "text", title);
	}

}
