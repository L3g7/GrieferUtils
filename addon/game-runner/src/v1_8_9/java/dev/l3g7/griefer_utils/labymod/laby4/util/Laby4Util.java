/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.util;

import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.labymod.laby4.Main;
import dev.l3g7.griefer_utils.labymod.laby4.bridges.LabyBridgeImpl;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.navigation.elements.ScreenBaseNavigationElement;
import net.labymod.api.client.gui.screen.ScreenWrapper;
import net.labymod.api.client.gui.screen.activity.Activity;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.WrappedWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.ScreenRendererWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.SettingElement;
import net.labymod.api.event.Event;
import net.labymod.api.event.LabyEvent;
import net.labymod.api.event.method.SubscribeMethod;
import net.labymod.api.models.addon.info.InstalledAddonInfo;
import net.labymod.core.addon.AddonClassLoader;
import net.labymod.core.client.gui.navigation.elements.LabyModNavigationElement;
import net.labymod.core.client.gui.screen.activity.activities.NavigationActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.child.mods.ModsEditorActivity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Deque;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static net.labymod.api.Laby.labyAPI;

public class Laby4Util {

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
		if (panelRenderer == null || !panelRenderer.isVisible())
			return false;

		Deque<SettingElement> openSettings = Reflection.get(panelRenderer.getScreen(), "openSettings");

		Setting current = openSettings.peekFirst();
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

	public static <T extends Widget> T get(Widget start, String... idPath) {
		Widget widget = start;
		for (String id : idPath) {
			widget = ((AbstractWidget<?>) widget).getChild(id);
			if (widget == null)
				return null;

			//noinspection deprecation
			if (widget instanceof WrappedWidget ww)
				widget = ww.childWidget();
		}
		return c(widget);
	}

	public static String getNamespace() {
		return ((AddonClassLoader) Laby4Util.class.getClassLoader()).getAddonInfo().getNamespace();
	}

	public static <T extends Event> void register(Class<T> event, Consumer<T> callback) {
		labyAPI().eventBus().registry().register(new CallbackSubscribeMethod<>(event, callback));
	}

	private static class CallbackSubscribeMethod<T extends Event> implements SubscribeMethod {
		private final LabyEvent annotation;
		private final AddonClassLoader classLoader;
		private final Class<T> event;
		private final Consumer<T> callback;

		public CallbackSubscribeMethod(Class<T> event, Consumer<T> callback) {
			this.annotation = event.getAnnotation(LabyEvent.class);
			this.classLoader = LabyBridgeImpl.class.getClassLoader() instanceof AddonClassLoader cl ? cl : null;
			this.event = event;
			this.callback = callback;
		}

		public void invoke(Event event) {
			callback.accept(c(event));
		}

		public @Nullable InstalledAddonInfo getAddon() {
			return Main.getAddon().info();
		}

		public @Nullable ClassLoader getClassLoader() {return classLoader;}

		public @Nullable Object getListener() {return null;}

		public byte getPriority() {return 127;}

		public @Nullable Method getMethod() {return null;}

		public @NotNull Class<?> getEventType() {return event;}

		public @Nullable LabyEvent getLabyEvent() {return annotation;}

		public boolean isInClassLoader(ClassLoader other) {return true;}

		public SubscribeMethod copy(Object newListener) {
			return new CallbackSubscribeMethod<>(event, callback);
		}
	}

}
