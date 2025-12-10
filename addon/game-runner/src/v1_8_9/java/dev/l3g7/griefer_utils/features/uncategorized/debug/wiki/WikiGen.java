/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug.wiki;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.widgets.Laby4Widget;
import dev.l3g7.griefer_utils.features.widgets.Widget;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Laby4Setting;
import net.labymod.api.client.gui.icon.Icon;
import net.minecraft.item.ItemStack;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
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

	private static JsonObject serialize(BaseSetting<?> mainSetting) {
		if (!(mainSetting instanceof Laby4Setting<?, ?> setting))
			return null;

		if (mainSetting == button || mainSetting == EnchantmentRenderer.enabled)
			return null;

		JsonObject obj = new JsonObject();
		obj.addProperty("name", setting.getStorage().name);
		obj.addProperty("description", setting.getStorage().description);
		obj.addProperty("icon", serializeIcon(setting.getStorage().icon));

		if (mainSetting.getChildSettings().size() > 0)
			obj.add("subsettings", serializeSubsettings(mainSetting.getChildSettings()));

		return obj;
	}

	private static JsonArray serializeSubsettings(List<BaseSetting<?>> settings) {
		JsonArray features = new JsonArray();
		for (BaseSetting<?> childSetting : settings) {
			JsonObject serialized = serialize(childSetting);
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

			Feature.getFeatures().sorted(Comparator.comparing(f -> f.getMainElement().name())).forEachOrdered(feature -> {
				String category = "uncategorized";

				if (feature.getCategory() != null)
					category = feature.getCategory().setting.name();

				if (!result.has(category))
					result.add(category, new JsonArray());

				JsonArray array = result.getAsJsonArray(category);
				JsonObject object = serialize(feature.getMainElement());
				if (object == null)
					return;

				if (feature.getClass().isAnnotationPresent(Feature.FeatureCategory.class))
					object.addProperty("category", true);

				array.add(object);
			});

			List<BaseSetting<?>> widgets = FileProvider.getClassesWithSuperClass(Widget.class).stream()
				.filter(meta -> !meta.isAbstract())
				.map(meta -> (Widget) FileProvider.getSingleton(meta.load()))
				.map(Widget::<Laby4Widget>getVersionedWidget)
				.sorted(Comparator.comparing(Laby4Widget::getComparisonName))
				.map(Laby4Widget::getSetting)
				.collect(Collectors.toList());
			result.add("Module", serializeSubsettings(widgets));

			File file = new File("GrieferUtils/wiki_dump.json");
			file.getParentFile().mkdirs();
			file.createNewFile();

			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(result.toString().getBytes(StandardCharsets.UTF_8));
			}

			LabyBridge.labyBridge.notify("ok", "ok");
		});

}
