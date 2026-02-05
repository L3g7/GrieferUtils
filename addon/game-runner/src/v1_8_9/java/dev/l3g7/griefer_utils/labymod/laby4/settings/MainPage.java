/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.GUIEntry;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.Feature.CategoryData;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.SwitchSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.RootSettingRegistry;
import net.labymod.api.configuration.settings.type.SettingElement;
import net.labymod.core.client.gui.screen.activity.activities.NavigationActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.AbstractSidebarActivity;
import net.labymod.core.client.gui.screen.activity.activities.labymod.LabyModActivity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.misc.tags.laby4.Laby4TagManager.icon;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@ExclusiveTo(LABY_4)
public class MainPage {

	private static Timer timer = new Timer();
	private static final Set<TextFieldWidget> injectedWidgets = new HashSet<>();

	@OnEnable
	public static void registerSettings() {
		// Create root setting
		RootSetting registry = new RootSetting();
		Laby.labyAPI().coreSettingRegistry().addSetting(registry);

		// Collect settings
		ArrayList<BaseSetting<?>> settings = new ArrayList<>();
		collectSettings(settings);

		// Initialize settings
		settings.forEach(s -> {
			if (s instanceof Laby4Setting<?, ?> b)
				b.create(registry);
		});
		registry.addSettings(Reflection.<List<Setting>>c(settings));
	}

	@EventListener
	private static void onWidget(SettingActivityInitEvent event) {
		if (!(Laby4Util.getActivity() instanceof NavigationActivity n))
			return;

		if (!(n.mostInnerScreenInstance() instanceof LabyModActivity lm))
			return;

		AbstractSidebarActivity settingsActivity = (AbstractSidebarActivity) lm.getById("settings").provideScreen();
		TextFieldWidget searchWidget = Reflection.get(settingsActivity, "searchWidget");

		if (!injectedWidgets.add(searchWidget))
			return;

		Consumer<String> previousListener = Reflection.get(searchWidget, "updateListener");
		searchWidget.updateListener(previousListener.andThen(s -> {
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] bytes = digest.digest(("griefer_utils_salt_" + s).getBytes(StandardCharsets.UTF_8));
				String hash = Base64.getEncoder().encodeToString(bytes);

				if (hash.equals("IBmzqW3cyeMT0Gj/VqDLhnOhI0Qdhx6FgqFsdLbvzGA=")) {
					timer = new Timer();
					timer.schedule(new TimerTask() {
						public void run() {
							if (!(Laby4Util.getActivity() instanceof NavigationActivity))
								return;

							icon = icon.equals("icon") ? s : "icon";
							Laby.labyAPI().minecraft().executeOnRenderThread(() -> searchWidget.setText(""));
							labyBridge.notify("§aEaster Egg", "Easter Egg wurde " + (icon.equals("icon") ? "de" : "") + "aktiviert.");
							if (world() != null)
								mc().displayGuiScreen(null);

							timer = null;
						}
					}, 3179);
				} else if (timer != null) {
					timer.cancel();
					timer = null;
				}
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}));
	}

	private static void collectSettings(List<BaseSetting<?>> settings) {
		// Enable the feature category if one of its features gets enabled
		Feature.getFeatures()
			.sorted(Comparator.comparing(f -> f.getMainElement().name()))
			.forEach(feature -> {
				if (!(feature.getMainElement() instanceof SwitchSettingImpl main))
					return;

				for (BaseSetting<?> element : main.getChildSettings()) {
					if (!(element instanceof SwitchSetting sub))
						continue;

					sub.callback(b -> {
						if (b)
							main.set(true);
					});
				}

				main.setSearchTags(new String[]{main.name()});
			});

		// Initialize settings
		List<GUIEntry> entries = new ArrayList<>(Feature.getCategories());

		Feature.getFeatures().forEach(f -> {
			entries.add(f);
			((SettingElement) f.getMainElement()).setSearchTags(new String[]{
				f.getMainElement().name(),
				f.getClass().getSimpleName()
			});
		});

		// Initialize category settings
		for (CategoryData c : Feature.getCategories())
			((SwitchSettingImpl) c.getSetting()).setSearchTags(new String[]{c.getSetting().name()});

		entries.stream()
			.sorted(Comparator.comparing(GUIEntry::name))
			.forEach(e -> e.addToParent(settings));

		settings.add(HeaderSetting.create());

		// Add uncategorized features
		Feature.getUncategorized().stream()
			.sorted(Comparator.comparing(BaseSetting::name))
			.forEach(settings::add);

		settings.add(HeaderSetting.create());

		// Wiki link
		settings.add(ButtonSetting.create()
			.name("Wiki").icon("open_book")
			.buttonIcon("open_book_outline")
			.callback(() -> labyBridge.openWebsite("https://grieferutils.wiki")));

		// Ko-fi link
		settings.add(ButtonSetting.create()
			.name("Entwickler unterstützen").icon("ko_fi")
			.description("Wenn dir das Addon gefällt kannst du hier das Entwickler-Team dahinter unterstützen §c❤")
			.buttonIcon("ko_fi_outline")
			.callback(() -> labyBridge.openWebsite("https://ko-fi.com/l3g7_3")));

		// Discord link
		settings.add(ButtonSetting.create()
			.name("Discord").icon("discord")
			.buttonIcon("discord_clyde")
			.callback(() -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/discord")));
	}

	private static class RootSetting extends RootSettingRegistry {

		private RootSetting() {
			super(Laby4Util.getNamespace(), Laby4Util.getNamespace());
		}

		@Override
		public Component displayName() {
			return Component.text("GrieferUtils");
		}

		@Override
		public Icon getIcon() {
			return Icons.of("icon");
		}

	}

}
