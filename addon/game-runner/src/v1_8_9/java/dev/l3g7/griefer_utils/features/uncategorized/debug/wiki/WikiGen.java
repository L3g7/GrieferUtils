/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Reason;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.ArrayUtil;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.settings.Settings;
import dev.l3g7.griefer_utils.features.widgets.Laby4Widget;
import dev.l3g7.griefer_utils.features.widgets.Widget;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Laby4Setting;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.CitybuildSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.list.WrappingListSetting;
import net.labymod.api.client.gui.icon.Icon;
import net.minecraft.item.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WikiGen {

	private static String serializeIcon(Object icon) {
		if (icon == null)
			return "null";

		Icon lmIcon = (Icon) icon;
		if (lmIcon.getResourceLocation() != null) {
			String path = lmIcon.getResourceLocation().getPath();

			if (lmIcon.getResourceLocation().getNamespace().equals("griefer_utils")) {
				return "griefer_utils" + path.substring("icon/".length());
			}

			return lmIcon.getResourceLocation().toString().replace(':', '/');
		}

		if (!icon.getClass().getSimpleName().equals("ItemStackIcon"))
			return "null";

		ItemStack stack = Reflection.get(icon, "icon");
		return "minecraft/" + stack.getDisplayName().toLowerCase().replace(' ', '_') + (stack.hasEffect() ? ".gif" : ".png");
	}

	private static JsonObject serialize(String parentType, BaseSetting<?> mainSetting) {
		if (!(mainSetting instanceof Laby4Setting<?, ?> setting))
			return null;

		if (mainSetting == button || mainSetting == EnchantmentRenderer.enabled)
			return null;

		var feature = Feature.getFeatures()
			.filter(f -> f.getMainElement() == mainSetting)
			.findFirst();

		var widget = FileProvider.getClassesWithSuperClass(Widget.class).stream()
			.filter(meta -> !meta.isAbstract())
			.map(meta -> (Widget) FileProvider.getSingleton(meta.load()))
			.filter(s -> s.<Laby4Widget>getVersionedWidget().getSetting() == mainSetting)
			.findFirst();

		String type;
		if (feature.isPresent() && feature.get() instanceof Settings)
			type = "settings";
		else if (feature.isPresent() || parentType.equals("settings"))
			type = "feature";
		else if (widget.isPresent())
			type = "widget";
		else if (parentType.equals("category"))
			type = "category";
		else
			type = "setting";

		if (setting instanceof WrappingListSetting<?>) {
			setting = Reflection.get(setting, "inner");
		}

		JsonObject obj = new JsonObject();
		obj.addProperty("name", setting.getStorage().name);
		obj.addProperty("description", setting.getStorage().description);
		obj.addProperty("type", type);

		if (setting instanceof CitybuildSettingImpl)
			obj.addProperty("icon", "minecraft/nether_star.gif");
		else
			obj.addProperty("icon", serializeIcon(setting.getStorage().icon));

		if (mainSetting.getChildSettings().size() > 0)
			obj.add("subsettings", serializeSubsettings(type, mainSetting.getChildSettings()));

		return obj;
	}

	private static JsonArray serializeSubsettings(String parentType, List<BaseSetting<?>> settings) {
		JsonArray features = new JsonArray();
		for (BaseSetting<?> childSetting : settings) {
			JsonObject serialized = serialize(parentType, childSetting);
			if (serialized != null)
				features.add(serialized);
		}

		return features;
	}

	public static final ButtonSetting button = ButtonSetting.create()
		.name("Wiki-Generator")
		.description("Generiert den Dump zum Generieren vom Wiki.")
		.icon("open_book")
		.callback(() -> {
			JsonObject result = new JsonObject();

			// Features
			for (Feature.CategoryData category : Feature.getCategories()) {
				Object parent = Reflection.get(category, "parent");
				if (parent != null)
					continue;

				result.add(category.name(), serialize("category", category.getSetting()));
			}

			// Widgets
			List<BaseSetting<?>> widgets = FileProvider.getClassesWithSuperClass(Widget.class).stream()
				.filter(meta -> !meta.isAbstract())
				.map(meta -> (Widget) FileProvider.getSingleton(meta.load()))
				.map(Widget::<Laby4Widget>getVersionedWidget)
				.sorted(Comparator.comparing(Laby4Widget::getComparisonName))
				.map(Laby4Widget::getSetting)
				.collect(Collectors.toList());

			result.add("§xModule", serialize("category", SwitchSetting.create()
				.name("Module")
				.icon("tab_list")
				.subSettings(widgets)));

			// Settings
			var setting = FileProvider.getSingleton(Settings.class).getMainElement();
			result.add("§yEinstellungen", serialize("settings", setting));

			Path path = Paths.get("GrieferUtils", "auto_dump.json");
			Files.createDirectories(path.getParent());
			IO.write(path).json(result);

			// Exclusives
			JsonObject exclusives = new JsonObject();
			UncheckedFileProvider.getFeatures().forEach(m -> {
				if (m.hasAnnotation(Bridge.ExclusiveTo.class)) {
					var ann = m.getAnnotation(Bridge.ExclusiveTo.class);
					Version version = ann.getValue("value", true);
					Reason reason = ann.getValue("reason", true);
					String customMessage = ann.getValue("customMessage", false);

					if (reason != Reason.IMPLEMENTATION) {
						JsonObject data = new JsonObject();
						data.addProperty("version", version.name());
						data.addProperty("message", customMessage);
						exclusives.add(ArrayUtil.last(m.name.split("/")), data);
					}
				}
			});

			IO.write(Paths.get("GrieferUtils", "auto_exclusives.json")).json(exclusives);
			LabyBridge.labyBridge.notify("ok", "ok");
		});

}
