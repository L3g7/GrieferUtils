/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.item.recraft.Action.Ingredient;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.elements.*;
import net.labymod.utils.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.LinkedList;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

class RecraftRecording {

	private final String[] ROMAN_NUMERALS = new String[] {"", "I", "II", "III", "IV", "V", "VI", "VII"};

	LinkedList<Action> actions = new LinkedList<>();

	final KeySetting key = new KeySetting()
		.name("Taste")
		.description("Mit welcher Taste diese Aufzeichung abgespielt werden soll.")
		.icon("key")
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && FileProvider.getSingleton(Recraft.class).isEnabled())
				RecraftPlayer.play(this);
		});

	final StringSetting name = new StringSetting()
		.name("Name")
		.description("Wie diese Aufzeichnung heißt.")
		.icon(Material.NAME_TAG);

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
		HeaderSetting titleSetting = (HeaderSetting) mainSetting.getSubSettings().getElements().get(2);
		titleSetting.name("§e§l" + title);
	}

	JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("name", name.get());
		object.add("keys", key.getStorage().encodeFunc.apply(key.get()));

		ItemStack icon = mainSetting.iconStorage.itemStack;
		if (icon != null)
			object.add("icon", new Ingredient(icon, icon.stackSize).toJson());

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

		if (object.has("icon")) {
			JsonObject icon = object.getAsJsonObject("icon");
			recording.mainSetting.icon(new ItemStack(Item.getItemById(icon.get("id").getAsInt()), icon.get("compression").getAsInt(), icon.get("meta").getAsInt()));
		}

		JsonArray jsonActions = object.getAsJsonArray("actions");
		for (JsonElement jsonAction : jsonActions)
			recording.actions.add(Action.fromJson(jsonAction));

		return recording;
	}

	private class StartRecordingButtonSetting extends SmallButtonSetting {

		@Override
		protected void drawButtonIcon(IconData buttonIcon, int buttonX, int buttonY) {
			GlStateManager.enableBlend();
			int gb = actions.isEmpty() ? 0 : 1;
			GlStateManager.color(1, gb, gb);
			mc().getTextureManager().bindTexture(buttonIcon.getTextureIcon());
			drawUtils().drawTexture(buttonX + 4, buttonY + 3, 0, 0, 256, 256, 14, 14, 2);
		}

		public StartRecordingButtonSetting() {
			name("Aufzeichnung starten");
			description("Beginnt die Aufzeichung.");
			icon("camera");
			buttonIcon(new IconData("griefer_utils/icons/recording.png"));
			callback(() -> RecraftRecorder.startRecording(RecraftRecording.this));
		}

	}

	class RecordingDisplaySetting extends ListEntrySetting {

		private final IconStorage iconStorage = new IconStorage();
		final RecraftRecording recording = RecraftRecording.this;

		public RecordingDisplaySetting() {
			super(true, true, true);
			name("Unbenannte Aufzeichnung");
			icon(Material.BARRIER);
			subSettings(name, key, new HeaderSetting(), new StartRecordingButtonSetting());
			container = FileProvider.getSingleton(Recraft.class).getMainElement();
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

	}

}
