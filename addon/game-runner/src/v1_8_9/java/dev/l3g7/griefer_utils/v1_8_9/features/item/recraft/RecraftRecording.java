/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.recraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.laby4.settings.types.ButtonSettingImpl;
import dev.l3g7.griefer_utils.laby4.settings.types.CategorySettingImpl;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.KeySetting;
import dev.l3g7.griefer_utils.settings.types.StringSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.Action.Ingredient;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.LinkedList;

class RecraftRecording {

	private final String[] ROMAN_NUMERALS = new String[] {"", "I", "II", "III", "IV", "V", "VI", "VII"};

	LinkedList<Action> actions = new LinkedList<>();

	final KeySetting key = KeySetting.create()
		.name("Taste")
		.description("Mit welcher Taste diese Aufzeichung abgespielt werden soll.")
		.icon("key")
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && FileProvider.getSingleton(Recraft.class).isEnabled())
				RecraftPlayer.play(this);
		});

	final StringSetting name = StringSetting.create()
		.name("Name")
		.description("Wie diese Aufzeichnung heißt.")
		.icon(Items.name_tag);

	final RecordingDisplaySetting mainSetting = new RecordingDisplaySetting();

	public RecraftRecording() {
		name.callback(s -> {
			if (s.trim().isEmpty())
				s = "Unbenannte Aufzeichnung";

			mainSetting.name(s);
			setTitle(s);
		});
	}

	public void setTitle(String title) {
		HeaderSetting titleSetting = (HeaderSetting) mainSetting.getSubSettings().get(2);
		titleSetting.name("§e§l" + title);
	}

	JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("name", name.get());
		object.add("keys", key.getStorage().encodeFunc.apply(key.get()));

		ItemStack icon = null; //TODO: mainSetting.iconStorage.itemStack;
		if (icon != null)
			object.addProperty("icon", new Ingredient(icon, icon.stackSize).toLong());

		JsonArray jsonActions = new JsonArray();
		for (Action action : actions)
			jsonActions.add(action.toJson());
		object.add("actions", jsonActions);

		return object;
	}

	static RecraftRecording read(JsonObject object) {
		RecraftRecording recording = new RecraftRecording();
		recording.name.set(object.get("name").getAsString());

		recording.key.set(recording.key.getStorage().decodeFunc.apply(object.get("keys")));

		JsonElement icon = object.get("icon");
		if (icon != null) {
			if (icon.isJsonPrimitive()) {
				Ingredient ingredient = Ingredient.fromLong(icon.getAsLong());
				recording.mainSetting.icon(new ItemStack(Item.getItemById(ingredient.itemId), ingredient.compression, ingredient.meta));
			} else {
				JsonObject iconObj = icon.getAsJsonObject();
				recording.mainSetting.icon(new ItemStack(Item.getItemById(iconObj.get("id").getAsInt()), iconObj.get("compression").getAsInt(), iconObj.get("meta").getAsInt()));
			}
		}

		if (icon != null && icon.isJsonPrimitive()) {
			Ingredient ingredient = Ingredient.fromLong(icon.getAsLong());
			recording.mainSetting.icon(new ItemStack(Item.getItemById(ingredient.itemId), ingredient.compression, ingredient.meta));
		}

		JsonArray jsonActions = object.getAsJsonArray("actions");
		for (JsonElement jsonAction : jsonActions)
			recording.actions.add(Action.fromJson(jsonAction));

		return recording;
	}

	private class StartRecordingButtonSetting extends ButtonSettingImpl { // TODO: do not use implementation
/*
TODO:
		@Override
		protected void drawButtonIcon(IconData buttonIcon, int buttonX, int buttonY) {
			GlStateManager.enableBlend();
			int gb = actions.isEmpty() ? 0 : 1;
			GlStateManager.color(1, gb, gb);
			mc().getTextureManager().bindTexture(buttonIcon.getTextureIcon());
			drawUtils().drawTexture(buttonX + 4, buttonY + 3, 0, 0, 256, 256, 14, 14, 2);
		}
*/
		public StartRecordingButtonSetting() {
			name("Aufzeichnung starten");
			description("Beginnt die Aufzeichung.");
			icon("camera");
			buttonIcon("griefer_utils/icons/recording.png");
			callback(() -> RecraftRecorder.startRecording(RecraftRecording.this));
		}

	}

	class RecordingDisplaySetting extends CategorySettingImpl { // TODO: ListEntrySetting
		final RecraftRecording recording = RecraftRecording.this;
/*
		private final IconStorage iconStorage = new IconStorage();

		public RecordingDisplaySetting() {
			super(true, true, true);
			name("Unbenannte Aufzeichnung");
			icon(Material.BARRIER);
			subSettings(name, key, new HeaderSetting(), new StartRecordingButtonSetting());
		}

		@Override
		public IconStorage getIconStorage() {
			return iconStorage;
		}

		@Override
		public void drawIcon(int x, int y) {
			ItemStack itemIcon = getIconStorage().itemStack;
			if (itemIcon == null)
				return;

			drawUtils().drawItem(itemIcon, x + 3, y + 2, ROMAN_NUMERALS[itemIcon.stackSize]);
		}

		@Override
		protected void onChange() {
			if (!container.getSubSettings().getElements().contains(this))
				key.set(ImmutableSet.of());

			FileProvider.getSingleton(Recraft.class).save();
		}

		@Override
		protected void openSettings() {
			mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> FileProvider.getSingleton(Recraft.class).save(), mainSetting));
		}
*/
	}

}
