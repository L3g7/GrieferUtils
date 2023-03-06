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

package dev.l3g7.griefer_utils.settings.elements.player_list_setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.ModTextField;
import net.labymod.main.ModTextures;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class PlayerListSetting extends ControlElement implements ElementBuilder<PlayerListSetting>, ValueHolder<PlayerListSetting, List<PlayerListEntry>> {

	private final Storage<List<PlayerListEntry>> storage = new Storage<>(list -> {
		JsonArray array = new JsonArray();
		list.forEach(e -> array.add(new JsonPrimitive(e.id)));
		return array;
	}, array -> {
		List<PlayerListEntry> list = new ArrayList<>();
		array.getAsJsonArray().forEach(e -> list.add(new PlayerListEntry(null, e.getAsString())));
		return list;
	}, new ArrayList<>());

	private SettingsElement container = this;

	public PlayerListSetting() {
		super("§cEs gab einen Fehler!", null);
		setSettingEnabled(true);
	}

	public void setContainer(SettingsElement container) {
		this.container = container;
	}

	@Override
	public PlayerListSetting config(String configKey) {
		ValueHolder.super.config(configKey);

		List<SettingsElement> settings = new ArrayList<>();
		for (PlayerListEntry entry : get())
			settings.add(new PlayerDisplaySetting(entry));

		PlayerAddSetting addSetting = new PlayerAddSetting();
		settings.add(addSetting);
		getSettings().remove(this);
		((ElementBuilder<?>) container).subSettings(settings);

		return this;
	}

	private void renderData(int x, int y, int size, PlayerListEntry data) {
		DrawUtils drawUtils = drawUtils();
		if (data.skin != null) {
			GlStateManager.bindTexture(data.skin.getGlTextureId());

			if (!data.isMojang()) {
				drawUtils.drawTexture(x, y, 0, 0, 256, 256, size, size);
				return;
			}

			int yHeight = data.slim ? 64 : 32; // Old textures are 32x64
			drawUtils.drawTexture(x, y, 32, yHeight, 32, yHeight, size, size); // First layer
			drawUtils.drawTexture(x, y, 160, yHeight, 32, yHeight, size, size); // Second layer
		} else {
			mc.getTextureManager().bindTexture(ModTextures.MISC_HEAD_QUESTION);
			drawUtils.drawTexture(x, y, 0, 0, 256, 256, size, size);
		}
	}

	private List<SettingsElement> getSettings() {
		return container.getSubSettings().getElements();
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	public Storage<List<PlayerListEntry>> getStorage() {
		return storage;
	}

	public boolean contains(String name, UUID uuid) {
		if (name == null && uuid == null)
			return false;

		for (PlayerListEntry entry : get())
			if (name == null ? uuid.toString().equalsIgnoreCase(entry.id) : name.equalsIgnoreCase(entry.name))
				return true;

		return false;
	}

	private class PlayerDisplaySetting extends ControlElement {

		private final PlayerListEntry data;
		private boolean deleteHovered = false;

		public PlayerDisplaySetting(PlayerListEntry entry) {
			super("§cUnknown name", new IconData(ModTextures.MISC_HEAD_QUESTION));
			data = entry;
		}

		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			super.mouseClicked(mouseX, mouseY, mouseButton);
			if (deleteHovered) {
				getSettings().remove(this);
				get().remove(data);
				save();
				getStorage().callbacks.forEach(c -> c.accept(get()));
				mc.currentScreen.initGui(); // Update settings
			}
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			setDisplayName(data.name == null ? "§cNutzer konnte nicht geladen werden!" : data.name);
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			drawUtils().drawRectangle(x - 1, y, x, maxY, 0x78787878);

			renderData(x + 3, y + 3, 16, data);

			if (mouseOver) {
				mc.getTextureManager().bindTexture(new IconData("labymod/textures/misc/blocked.png").getTextureIcon());
				deleteHovered = mouseX > maxX - 19 && mouseX < maxX - 6 && mouseY > y + 5 && mouseY < y + 18;
				drawUtils().drawTexture( maxX - 16 - (deleteHovered ? 4 : 3), y + (deleteHovered ? 3.5 : 4.5), 256, 256, deleteHovered ? 16 : 14, deleteHovered ? 16 : 14);
			}
		}

	}

	private class PlayerAddSetting extends EntryAddSetting {

		PlayerAddSetting() {
			super("Spieler hinzufügen");
			callback(() -> Minecraft.getMinecraft().displayGuiScreen(new AddPlayerGui(Minecraft.getMinecraft().currentScreen)));
		}

		private class AddPlayerGui extends GuiScreen {

			private final GuiScreen backgroundScreen;
			private ModTextField inputField;
			private GuiButton doneButton;
			private PlayerListEntry entry;

			public AddPlayerGui(GuiScreen backgroundScreen) {
				this.backgroundScreen = backgroundScreen;
				MinecraftForge.EVENT_BUS.register(this);
			}

			public void initGui() {
				super.initGui();
				backgroundScreen.width = width;
				backgroundScreen.height = height;
				if (backgroundScreen instanceof LabyModModuleEditorGui)
					PreviewRenderer.getInstance().init(AddPlayerGui.class);

				inputField = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 150, height / 4 + 45, 300, 20);
				inputField.setFocused(true);
				buttonList.add(new GuiButton(0, width / 2 - 105, height / 4 + 85, 100, 20, "Abbrechen"));
				buttonList.add(doneButton = new GuiButton(1, width / 2 + 5, height / 4 + 85, 100, 20, "Hinzufügen"));
			}

			private void updateValidity() {
				entry = PlayerListEntry.getEntry(inputField.getText());

				if (!entry.exists) {
					inputField.setTextColor(0xFFFF0000);
					doneButton.enabled = false;
				} else {
					inputField.setTextColor(0xFFFFFFFF);
					doneButton.enabled = entry.loaded;
				}
			}

			public void drawScreen(int mouseX, int mouseY, float partialTicks) {
				backgroundScreen.drawScreen(0, 0, partialTicks);
				drawRect(0, 0, width, height, Integer.MIN_VALUE);

				updateValidity();
				inputField.drawTextBox();

				super.drawScreen(mouseX, mouseY, partialTicks);
				renderData((width - 32) / 2, height / 4, 32, entry);
			}

			public void updateScreen() {
				backgroundScreen.updateScreen();
				inputField.updateCursorCounter();
			}

			protected void actionPerformed(GuiButton button) throws IOException {
				super.actionPerformed(button);
				switch (button.id) {
					case 1:
						getSettings().add(getSettings().indexOf(PlayerAddSetting.this), new PlayerDisplaySetting(entry));
						get().add(entry);
						save();
						getStorage().callbacks.forEach(c -> c.accept(get()));
						// Fall-through
					case 0:
						Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);
						backgroundScreen.initGui(); // Update settings
				}
			}

			protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
				super.mouseClicked(mouseX, mouseY, mouseButton);
				inputField.mouseClicked(mouseX, mouseY, mouseButton);
			}

			protected void keyTyped(char typedChar, int keyCode) {
				if (keyCode == 1) // ESC
					Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);

				inputField.textboxKeyTyped(typedChar, keyCode);
				updateValidity();
			}
		}

	}

}
