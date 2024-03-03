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

package dev.l3g7.griefer_utils.features.item.recraft;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.misc.functions.Function;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction.Ingredient;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftAction;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftPlayer;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftRecorder;
import dev.l3g7.griefer_utils.features.item.recraft.decompressor.DecompressAction;
import dev.l3g7.griefer_utils.features.item.recraft.decompressor.DecompressPlayer;
import dev.l3g7.griefer_utils.features.item.recraft.decompressor.DecompressRecorder;
import dev.l3g7.griefer_utils.features.item.recraft.recipe.RecipeAction;
import dev.l3g7.griefer_utils.features.item.recraft.recipe.RecipePlayer;
import dev.l3g7.griefer_utils.features.item.recraft.recipe.RecipeRecorder;
import dev.l3g7.griefer_utils.misc.Named;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.elements.*;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.LinkedList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class RecraftRecording {

	private final String[] ROMAN_NUMERALS = new String[] {"", "I", "II", "III", "IV", "V", "VI", "VII"};

	public LinkedList<RecraftAction> actions = new LinkedList<>();

	final KeySetting key = new KeySetting()
		.name("Taste")
		.description("Mit welcher Taste diese Aufzeichung abgespielt werden soll.")
		.icon("key")
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && FileProvider.getSingleton(Recraft.class).isEnabled())
				play(false);
		});

	final StringSetting name = new StringSetting()
		.name("Name")
		.description("Wie diese Aufzeichnung heißt.")
		.icon(Material.NAME_TAG);

	public final BooleanSetting ignoreSubIds = new BooleanSetting()
		.name("Sub-IDs ignorieren")
		.description("Ob beim Auswählen der Zutaten die Sub-IDs (z.B. unterschiedliche Holz-Typen) beachtet werden sollen.")
		.icon(new ItemStack(Blocks.log, 1, 2));

	public final DropDownSetting<RecordingMode> mode = new DropDownSetting<>(RecordingMode.class)
		.name("Modus")
		.description("Ob die Aufzeichnung /craft, /rezepte ausführt oder dekomprimiert.")
		.icon("knowledge_book")
		.defaultValue(RecordingMode.RECIPE);

	public final BooleanSetting craftAll = new BooleanSetting()
		.name("Alles vercraften")
		.description("Ob die Aufzeichnung so lange wiederholt werden soll, bis alle Items im Inventar verbraucht wurden.")
		.icon("arrow_circle");

	public final RecraftRecordingSelectionSetting successor = new RecraftRecordingSelectionSetting(this);

	public final RecordingDisplaySetting mainSetting;

	public RecraftRecording() {
		mainSetting = new RecordingDisplaySetting();

		mode.callback(m ->  {
			mode.getIconStorage().itemStack = null;
			mode.icon(m.icon);
			actions.clear();
			mainSetting.getIconStorage().itemStack = null;
			mainSetting.icon(Material.BARRIER);

			boolean removed = mainSetting.getSubSettings().getElements().remove(craftAll);
			if (m == RecordingMode.CRAFT) {
				List<SettingsElement> settings = mainSetting.getSubSettings().getElements();
				settings.add(settings.indexOf(mode) + 1, craftAll);
			}

			if (removed == (m != RecordingMode.CRAFT) && mc().currentScreen instanceof LabyModAddonsGui)
				mc().currentScreen.initGui();
		});

		name.callback(s -> {
			if (s.trim().isEmpty())
				s = "Unbenannte Aufzeichnung";

			mainSetting.name(s);
			setTitle(s);
		});
		name.callback(() -> Recraft.iterate((i, r) -> r.successor.updateName(r.successor.get())));
	}

	public void setTitle(String title) {
		HeaderSetting titleSetting = (HeaderSetting) mainSetting.getSubSettings().getElements().get(2);
		titleSetting.name("§e§l" + title);
	}

	public void startRecording() {
		mode.get().recorder.accept(this);
	}

	public void play(boolean isSuccessor) {
		Recraft.playingSuccessor = isSuccessor;
		mode.get().player.accept(this);
	}

	public boolean playSuccessor() {
		return successor.execute(mode.get());
	}

	JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("name", name.get());
		object.add("keys", key.getStorage().encodeFunc.apply(key.get()));

		ItemStack icon = mainSetting.iconStorage.itemStack;
		if (icon != null)
			object.addProperty("icon", new Ingredient(icon, icon.stackSize).toLong());

		object.addProperty("ignore_sub_ids", ignoreSubIds.get());
		object.addProperty("mode", mode.get().getName());
		object.addProperty("craftAll", craftAll.get());

		JsonArray jsonActions = new JsonArray();
		for (RecraftAction action : actions)
			jsonActions.add(action.toJson());
		object.add("actions", jsonActions);

		object.addProperty("successor", successor.toInt());

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

		if (object.has("ignore_sub_ids")) {
			recording.ignoreSubIds.set(object.get("ignore_sub_ids").getAsBoolean());
		}

		if (object.has("mode")) {
			String mode = object.get("mode").getAsString();
			for (RecordingMode recordingMode : RecordingMode.values())
				if (recordingMode.getName().equals(mode))
					recording.mode.set(recordingMode);

			if (recording.mode.get() == RecordingMode.CRAFT)
				recording.craftAll.set(object.get("craftAll").getAsBoolean());
		} else {
			return recording;
		}

		if (icon != null && icon.isJsonPrimitive()) {
			Ingredient ingredient = Ingredient.fromLong(icon.getAsLong());
			recording.mainSetting.icon(new ItemStack(Item.getItemById(ingredient.itemId), ingredient.compression, ingredient.meta));
		}

		JsonArray jsonActions = object.getAsJsonArray("actions");
		for (JsonElement jsonAction : jsonActions)
			recording.actions.add(recording.mode.get().actionParser.apply(jsonAction));

		if (object.has("successor"))
			recording.successor.fromInt(object.get("successor").getAsInt());

		return recording;
	}

	public enum RecordingMode implements Named {

		RECIPE("Rezept", "knowledge_book", RecipeRecorder::startRecording, RecipePlayer::play, RecipeAction::fromJson),
		CRAFT("/craft", ItemUtil.createItem(Blocks.crafting_table, 0, true), CraftRecorder::startRecording, CraftPlayer::play, CraftAction::fromJson),
		DECOMPRESS("Dekomprimieren", "chest", DecompressRecorder::startRecording, DecompressPlayer::play, DecompressAction::fromJson);

		final String displayName;
		final Object icon;
		final Consumer<RecraftRecording> recorder, player;
		final Function<JsonElement, RecraftAction> actionParser;

		RecordingMode(String displayName, Object icon, Consumer<RecraftRecording> recorder, Consumer<RecraftRecording> player, Function<JsonElement, RecraftAction> actionParser) {
			this.displayName = displayName;
			this.icon = icon;
			this.recorder = recorder;
			this.player = player;
			this.actionParser = actionParser;
		}

		@Override
		public String getName() {
			return displayName;
		}

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
			callback(RecraftRecording.this::startRecording);
		}

	}

	public class RecordingDisplaySetting extends ListEntrySetting {

		private final IconStorage iconStorage = new IconStorage();
		final RecraftRecording recording = RecraftRecording.this;

		public RecordingDisplaySetting() {
			super(true, true, true);
			name("Unbenannte Aufzeichnung");
			icon(Material.BARRIER);
			subSettings(name, key, mode, ignoreSubIds, new HeaderSetting(), new StartRecordingButtonSetting(),
				new HeaderSetting().entryHeight(10), new HeaderSetting("Nachfolgende Aufzeichnung"), successor);
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
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5, 0.5, 2);
			double sX = (x + 13) * 2;
			double sY = (y + 1) * 2;

			if (mode.get() == RecordingMode.CRAFT) {
				drawUtils().drawItem(ItemUtil.createItem(new ItemStack(Blocks.crafting_table), true, null), sX, sY, null);
			} else {
				String icon = mode.get() == RecordingMode.RECIPE ? "knowledge_book" : "chest";
				drawUtils().bindTexture("griefer_utils/icons/" + icon + ".png");
				drawUtils().drawTexture(sX, sY, 256, 256, 16, 16);
			}
			GlStateManager.popMatrix();
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
