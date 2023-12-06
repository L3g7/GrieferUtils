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
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.item.recraft.Action.Ingredient;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedList;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

class RecraftRecording {

	private final String[] ROMAN_NUMERALS = new String[] {"", "I", "II", "III", "IV", "V", "VI", "VII"};

	LinkedList<Action> actions = new LinkedList<>();

	private final KeySetting key = new KeySetting()
		.name("Taste")
		.description("Mit welcher Taste diese Aufzeichung abgespielt werden soll.")
		.icon("key")
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && FileProvider.getSingleton(Recraft.class).isEnabled())
				RecraftPlayer.play(this);
		});

	private final StringSetting name = new StringSetting()
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

	void write(PacketBuffer out) {
		out.writeString(name.get());

		out.writeByte(key.get().size());
		for (Integer i : key.get())
			out.writeByte(i);

		ItemStack icon = mainSetting.iconStorage.itemStack;
		if (icon != null)
			new Ingredient(icon, icon.stackSize).write(out);
		else
			out.writeByte(0);

		out.writeByte((byte) actions.size());
		for (Action action : actions)
			action.write(out);
	}

	static RecraftRecording read(PacketBuffer in) {
		RecraftRecording recording = new RecraftRecording();
		recording.name.set(in.readStringFromBuffer(Short.MAX_VALUE));

		byte keys = in.readByte();
		Integer[] keyArray = new Integer[keys];
		for (byte i = 0; i < keys; i++)
			keyArray[i] = (int) in.readByte();

		recording.key.set(ImmutableSet.copyOf(keyArray));

		int itemId = in.readVarIntFromBuffer();
		if (itemId != 0)
			recording.mainSetting.icon(new ItemStack(Item.getItemById(itemId), in.readByte(), in.readByte()));

		byte actions = in.readByte();
		for (byte i = 0; i < actions; i++)
			recording.actions.add(Action.readObject(in));

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

	class RecordingDisplaySetting extends ControlElement implements ElementBuilder<RecordingDisplaySetting> {

		private final IconStorage iconStorage = new IconStorage();

		private boolean hoveringDelete = false;
		private boolean hoveringEdit = false;

		public RecordingDisplaySetting() {
			super("Unbenannte Aufzeichnung", new IconData(Material.BARRIER));
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
		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			super.mouseClicked(mouseX, mouseY, mouseButton);

			if (hoveringEdit) {
				mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> FileProvider.getSingleton(Recraft.class).save(), mainSetting));
				return;
			}

			if (!hoveringDelete)
				return;

			Recraft recraft = FileProvider.getSingleton(Recraft.class);
			recraft.getMainElement().getSubSettings().getElements().remove(this);
			key.set(ImmutableSet.of());
			Recraft.recordings.remove(RecraftRecording.this);
			recraft.save();
			mc.currentScreen.initGui();
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			hideSubListButton();
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			drawUtils().drawRectangle(x - 1, y, x, maxY, 0x78787878);
			drawIcon(x, y);

			mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

			int xPosition = maxX - 20;
			double yPosition = y + 4.5;

			hoveringDelete = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 15.5 && mouseY <= yPosition + 16;

			xPosition -= 20;

			hoveringEdit = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 15.5 && mouseY <= yPosition + 16;

			if (!mouseOver)
				return;

			mc.getTextureManager().bindTexture(new ResourceLocation("labymod/textures/misc/blocked.png"));
			drawUtils().drawTexture(maxX - (hoveringDelete ? 20 : 19), y + (hoveringDelete ? 3.5 : 4.5), 256, 256, hoveringDelete ? 16 : 14, hoveringDelete ? 16 : 14);

			mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/pencil.png"));
			drawUtils().drawTexture(maxX - (hoveringEdit ? 40 : 39), y + (hoveringEdit ? 3.5 : 4.5), 256, 256, hoveringEdit ? 16 : 14, hoveringEdit ? 16 : 14);
		}

	}

}
