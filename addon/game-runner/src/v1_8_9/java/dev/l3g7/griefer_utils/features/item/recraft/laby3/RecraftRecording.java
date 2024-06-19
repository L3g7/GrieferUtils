/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.features.item.recraft.laby3;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.bridges.laby3.temp.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftRecordingCore;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftRecordingCore.RecordingMode;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import net.labymod.main.LabyMod;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.item.recraft.Recraft.ignoreSubIds;
import static dev.l3g7.griefer_utils.features.item.recraft.RecraftRecordingCore.RecordingMode.CRAFT;

public class RecraftRecording implements dev.l3g7.griefer_utils.features.item.recraft.RecraftRecording {

	private final String[] ROMAN_NUMERALS = new String[] {"", "I", "II", "III", "IV", "V", "VI", "VII"};
	private final RecraftRecordingCore core = new RecraftRecordingCore(this);

	@Override
	public RecraftRecordingCore getCore() {
		return core;
	}

	@Override
	public void setIcon(ItemStack stack) {
		mainSetting.icon(stack);
	}

	public final RecraftRecordingSelectionSetting successor = new RecraftRecordingSelectionSetting(this);

	public final RecordingDisplaySetting mainSetting;

	public RecraftRecording() {
		mainSetting = new RecordingDisplaySetting();

		mode().callback(m ->  {
			mode().icon(m.icon);
			actions().clear();

			boolean removed = mainSetting.getSubSettings().getElements().remove(craftAll());
			if (m == CRAFT) {
				List<SettingsElement> settings = mainSetting.getSubSettings().getElements();
				settings.add(settings.indexOf(mode()) + 1, (SettingsElement) craftAll());
			}

			if (removed == (m != CRAFT) && mc().currentScreen instanceof LabyModAddonsGui)
				mc().currentScreen.initGui();
		});

		name().callback(s -> {
			if (s.trim().isEmpty())
				s = "Unbenannte Aufzeichnung";

			mainSetting.setDisplayName(s);
			setTitle(s);
		});
		name().callback(() -> RecraftBridgeImpl.iterate((i, r) -> r.successor.updateName(r.successor.get())));
	}

	public void setTitle(String title) {
		HeaderSetting titleSetting = (HeaderSetting) mainSetting.getSubSettings().getElements().get(2);
		titleSetting.name("§e§l" + title);
	}

	public boolean playSuccessor() {
		return successor.execute(mode().get());
	}

	JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("name", name().get());
		object.add("keys", key().getStorage().encodeFunc.apply(key().get()));

//		ItemStack icon = mainSetting.iconStorage.itemStack;
//		if (icon != null)
//			object.addProperty("icon", new RecraftAction.Ingredient(icon, icon.stackSize).toLong());

		object.addProperty("ignore_sub_ids", ignoreSubIds().get());
		object.addProperty("mode", mode().get().getName());
		object.addProperty("craftAll", craftAll().get());

		JsonArray jsonActions = new JsonArray();
		for (RecraftAction action : actions())
			jsonActions.add(action.toJson());
		object.add("actions", jsonActions);

		object.addProperty("successor", successor.toInt());

		return object;
	}

	static RecraftRecording read(JsonObject object) {
		RecraftRecording recording = new RecraftRecording();

		recording.name().set(object.get("name").getAsString());
		recording.key().set(recording.key().getStorage().decodeFunc.apply(object.get("keys")));

//		JsonElement icon = object.get("icon");
//		if (icon != null) {
//			if (icon.isJsonPrimitive()) {
//				RecraftAction.Ingredient ingredient = RecraftAction.Ingredient.fromLong(icon.getAsLong());
//				recording.mainSetting.icon(new ItemStack(Item.getItemById(ingredient.itemId), ingredient.compression, ingredient.meta));
//			} else {
//				JsonObject iconObj = icon.getAsJsonObject();
//				recording.mainSetting.icon(new ItemStack(Item.getItemById(iconObj.get("id").getAsInt()), iconObj.get("compression").getAsInt(), iconObj.get("meta").getAsInt()));
//			}
//		}

		if (object.has("ignore_sub_ids")) {
			recording.ignoreSubIds().set(object.get("ignore_sub_ids").getAsBoolean());
		}

		if (object.has("mode")) {
			String mode = object.get("mode").getAsString();
			for (RecordingMode recordingMode : RecordingMode.values())
				if (recordingMode.getName().equals(mode))
					recording.mode().set(recordingMode);

			if (recording.mode().get() == RecordingMode.CRAFT)
				recording.craftAll().set(object.get("craftAll").getAsBoolean());
		} else {
			return recording;
		}

//		if (icon != null && icon.isJsonPrimitive()) {
//			RecraftAction.Ingredient ingredient = RecraftAction.Ingredient.fromLong(icon.getAsLong());
//			recording.mainSetting.icon(new ItemStack(Item.getItemById(ingredient.itemId), ingredient.compression, ingredient.meta));
//		}

		JsonArray jsonActions = object.getAsJsonArray("actions");
		for (JsonElement jsonAction : jsonActions)
			recording.actions().add(recording.mode().get().actionParser.apply(jsonAction));

		if (object.has("successor"))
			recording.successor.fromInt(object.get("successor").getAsInt());

		return recording;
	}

	private class StartRecordingButtonSetting extends SmallButtonSetting implements Laby3Setting<StartRecordingButtonSetting, Object> {

		private final ExtendedStorage<Object> storage = new ExtendedStorage<>(e -> JsonNull.INSTANCE, e -> NULL, NULL);
		@Override
		protected void drawButtonIcon(IconData buttonIcon, int buttonX, int buttonY) {
			GlStateManager.enableBlend();
			int gb = actions().isEmpty() ? 0 : 1;
			GlStateManager.color(1, gb, gb);
			mc().getTextureManager().bindTexture(buttonIcon.getTextureIcon());
			LabyMod.getInstance().getDrawUtils().drawTexture(buttonX + 4, buttonY + 3, 0, 0, 256, 256, 14, 14, 2);
		}

		public StartRecordingButtonSetting() {
			super(new IconData("griefer_utils/icons/camera.png"));
			setDisplayName("Aufzeichnung starten");
			setDescriptionText("Beginnt die Aufzeichung.");
			buttonIcon(new IconData("griefer_utils/icons/recording.png"));
			callback(() -> getCore().startRecording());
		}

		@Override
		public ExtendedStorage<Object> getStorage() {
			return storage;
		}
	}

	public class RecordingDisplaySetting extends ListEntrySetting implements Laby3Setting<RecordingDisplaySetting, Object> {

		private final ExtendedStorage<Object> storage = new ExtendedStorage<>(e -> JsonNull.INSTANCE, e -> NULL, NULL);
		final RecraftRecording recording = RecraftRecording.this;

		public RecordingDisplaySetting() {
			super(true, true, true, new IconData(Material.BARRIER));
			setDisplayName("Unbenannte Aufzeichnung");
			getSubSettings().addAll(c(new ArrayList<>(Arrays.asList(name(), key(), mode(), ignoreSubIds, HeaderSetting.create(), new StartRecordingButtonSetting(),
				HeaderSetting.create().entryHeight(10), HeaderSetting.create("Nachfolgende Aufzeichnung"), successor))));
		}

		@Override
		protected void onChange() {
			if (!container.getSubSettings().getElements().contains(this))
				key().set(ImmutableSet.of());

			RecraftBridgeImpl.save();
		}

		@Override
		protected void openSettings() {
			mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(RecraftBridgeImpl::save, mainSetting));
		}

		@Override
		public ExtendedStorage<Object> getStorage() {
			return storage;
		}
	}

}
