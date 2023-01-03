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

package dev.l3g7.griefer_utils.features.chat.chat_menu;

import dev.l3g7.griefer_utils.features.chat.chat_menu.ChatMenuEntry.Action;
import dev.l3g7.griefer_utils.features.chat.chat_menu.ChatMenuEntry.IconType;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.gui.elements.ModTextField;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.main.LabyMod;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.AddonElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.labymod.main.ModTextures.MISC_HEAD_QUESTION;

public class AddChatMenuEntryGui extends GuiScreen {

	private final GuiScreen backgroundScreen;
	private ModTextField commandInput;
	private ModTextField nameInput;
	private GuiButton cancelButton;
	private GuiButton doneButton;
	private final ChatMenuEntry entry;
	private final ChatMenuEntry ogEntry;

	private final List<ActionButton> actionButtons = new ArrayList<>();
	private final List<IconTypeButton> iconTypeButtons = new ArrayList<>();
	private GuiButton buttonBack;
	private DropDownMenu<Action> textCompareDropDown;

	private ModTextField fileInput;
	private GuiButton fileButton;
	private ItemSetting itemSetting;

	private final Scrollbar scrollbar = new Scrollbar(1);

	public AddChatMenuEntryGui(ChatMenuEntry entry, GuiScreen backgroundScreen) {
		this.entry = entry == null ? new ChatMenuEntry() : entry;
		this.ogEntry = this.entry;
		this.backgroundScreen = backgroundScreen;
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void initGui() {
		super.initGui();
		this.scrollbar.setPosition(this.width / 2 + 172 , 50, this.width / 2 + 172 + 4, this.height - 15);
		this.scrollbar.setSpeed(20);
		this.scrollbar.init();

		backgroundScreen.width = width;
		backgroundScreen.height = height;
		int y = 50 + 80;

		commandInput = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 120, y + 95, 240, 20);
		commandInput.setText((String) entry.command);
		commandInput.setMaxStringLength(Integer.MAX_VALUE);


		nameInput = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 120, y + 85 + 68, 240, 20);
		nameInput.setText(entry.name);
		nameInput.setMaxStringLength(Integer.MAX_VALUE);

		buttonList.clear();
		buttonList.add(cancelButton = new GuiButton(0, width / 2 - 105, y + 95, 100, 20, entry.completed ? "Löschen" : "Abbrechen"));
		buttonList.add(doneButton = new GuiButton(1, width / 2 + 5, y + 95, 100, 20, entry.completed ? "Speichern" : "Hinzufügen"));

		int bgn = (width - 336) / 2;

		actionButtons.clear();
		int x = 0;
		for (int i = 0; i < Action.values().length; i++) {
			Action value = Action.values()[i];
			if(value == Action.CONSUMER)
				continue;
			int sLen = drawUtils().getStringWidth(value.name) + 29;
			ActionButton btn = new ActionButton(100 + i, bgn + x, y + 25, sLen, 23, value);
			buttonList.add(btn.wrappedButton);
			actionButtons.add(btn);
			x += sLen + 4;
		}
		buttonList.add(buttonBack = new GuiButton(1, this.width / 2 - 100, 20, 22, 20, "<"));

		bgn = (width - 240) / 2;

		iconTypeButtons.clear();
		x = 0;
		for (int i = 0; i < IconType.values().length; i++) {
			IconType value = IconType.values()[i];
			if(value == IconType.SYSTEM)
				continue;
			int sLen = drawUtils().getStringWidth(value.name) + 29 + 20;
			IconTypeButton btn = new IconTypeButton(200 + i, bgn + x, y + 183 + 25 + 3, sLen, 23, value);
			buttonList.add(btn.wrappedButton);
			iconTypeButtons.add(btn);
			x += sLen + 4;
		}
		buttonList.add(buttonBack = new GuiButton(1, this.width / 2 - 100, 20, 22, 20, "<"));

		textCompareDropDown = new DropDownMenu<>("", 0, 0, 0, 0);
		textCompareDropDown.fill(Action.values());
		textCompareDropDown.setSelected(entry.action);
		textCompareDropDown.setEntryDrawer((o, ex, ey, trimmedEntry) -> drawUtils().drawString(((Action) o).name, ex, ey));
		textCompareDropDown.setY(y + 183 + 45);
		textCompareDropDown.setWidth(240);
		textCompareDropDown.setHeight(17);



		fileInput = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), bgn + 26, y + 183 + 88, 240 - 60 - 6 - 26, 20);
		fileInput.setPlaceHolder("");
		fileInput.setMaxStringLength(Integer.MAX_VALUE);
		buttonList.add(fileButton = new GuiButton(4, this.width / 2 + 60, y + 183 + 88, 60, 20, "Auswählen"));

		itemSetting = new ItemSetting()
			.name("Item")
			.description("Die ID / der Namespace des Items / Blocks, das als Icon angezeigt werden soll")
			.callback(stack -> entry.icon = stack);

		itemSetting.init();
		if (entry.iconType == IconType.ITEM)
			itemSetting.set((ItemStack) entry.icon);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(0, 0, 0, 0);
		super.drawScreen(mouseX, mouseY, partialTicks);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		int height = (entry.action == null ? 217 : (entry.iconType != null && entry.iconType != IconType.DEFAULT ? 403 + 60 : 403)) + 20;
		int guiScale = new ScaledResolution(mc).getScaleFactor();

		scrollbar.update(height - (int) scrollbar.getTop() + (entry.action == null ? 0 : entry.iconType != null && entry.iconType != IconType.DEFAULT ? getLowerSpacing(guiScale) : ((26 * guiScale) - 34)));

		GL11.glColorMask(false, false, false, false);
		for (GuiButton guiButton : this.buttonList) guiButton.drawButton(this.mc, mouseX, mouseY);
		GL11.glColorMask(true, true, true, true);

		LabyModAddonsGui addonsGui = (LabyModAddonsGui) backgroundScreen;
		DrawUtils draw = LabyMod.getInstance().getDrawUtils();
		draw.drawAutoDimmedBackground(scrollbar.getScrollY());

		scrollbar.draw(mouseX, mouseY);
		drawUtils().drawCenteredString("§e§l" + Constants.ADDON_NAME, width / 2f, 81 + scrollbar.getScrollY(), 1.3);
		drawUtils().drawCenteredString("§e§lChatmenü", width / 2f, 105 + scrollbar.getScrollY(), .7);

		GL11.glTranslated(0, scrollbar.getScrollY(), 0);
		mouseY -= scrollbar.getScrollY();
		for (ActionButton actionButton : actionButtons) {
			GuiButton button = actionButton.wrappedButton;
			int x = button.xPosition;
			int y = button.yPosition;
			drawUtils().drawRectangle(x, y, x + button.width, y + button.height, ModColor.toRGB(80, 80, 80, 60));
			mc.getTextureManager().bindTexture(actionButton.image.getTextureIcon());
			boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + button.width && mouseY < y + button.height;

			if (hovered || (entry.action == actionButton.action)) {
				drawUtils().drawTexture(x + 2, y + 2, 256.0, 256.0, 18, 18);
				drawUtils().drawString(button.displayString, x + 25, (double) y + 7);
			} else {
				drawUtils().drawTexture(x + 3, y + 3, 256.0, 256.0, 16.0, 16.0);
				int r = 180;
				drawUtils().drawString(fontRendererObj, button.displayString, x + 24, y + 7, ModColor.toRGB(r, r, r, 0));
			}
		}
		int x = width / 2 - 120;
		drawUtils().drawString("Aktion", width / 2f - 168, (actionButtons.get(0).wrappedButton.yPosition) - fontRendererObj.FONT_HEIGHT - 8, 1.2);

		doneButton.yPosition = cancelButton.yPosition = height - 40;
		doneButton.enabled = !commandInput.getText().isEmpty() && !nameInput.getText().isEmpty() && (entry.iconType == IconType.DEFAULT || entry.icon != null);
		buttonBack.id = doneButton.enabled ? 1 : 7;
		doneButton.drawButton(mc, mouseX, mouseY);
		cancelButton.drawButton(mc, mouseX, mouseY);

		if (entry.action != null) {
			String cmdTitle = "";
			switch (entry.action) {
				case OPEN_URL:
					cmdTitle = "URL";
					nameInput.setPlaceHolder("§8NameMC öffnen");
					commandInput.setPlaceHolder("§8https://namemc.com/search?q=%name%");
					break;
				case RUN_CMD:
					cmdTitle = "Befehl";
					nameInput.setPlaceHolder("§8Spieler kicken");
					commandInput.setPlaceHolder("§8/startkick %name%");
					break;
				case SUGGEST_CMD:
					cmdTitle = "Befehl";
					nameInput.setPlaceHolder("§8MSG an Spieler");
					commandInput.setPlaceHolder("§8/msg %name% ");
					break;
			}
			drawUtils().drawString(cmdTitle, x, commandInput.yPosition - fontRendererObj.FONT_HEIGHT - 8, 1.2);
			commandInput.drawTextBox();

			drawUtils().drawString("Name", x, nameInput.yPosition - fontRendererObj.FONT_HEIGHT - 8, 1.2);
			nameInput.drawTextBox();

			for (IconTypeButton iconTypeButton : iconTypeButtons) {
				GuiButton button = iconTypeButton.wrappedButton;
				x = button.xPosition + 10;
				int y = button.yPosition;
				drawUtils().drawRectangle(x - 10, y, x - 10 + button.width, y + button.height, ModColor.toRGB(80, 80, 80, 60));
				if (iconTypeButton.type == IconType.DEFAULT)
					mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/" + entry.action.defaultIcon + ".png"));
				else
					mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/" + iconTypeButton.type.defaultIcon + ".png"));
				boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + button.width && mouseY < y + button.height;

				if (hovered || (entry.iconType == iconTypeButton.type)) {
					drawUtils().drawTexture(x + 2, y + 2, 256.0, 256.0, 18, 18);
					drawUtils().drawString(button.displayString, x + 25, (double) y + 7);
				} else {
					drawUtils().drawTexture(x + 3, y + 3, 256.0, 256.0, 16.0, 16.0);
					int r = 180;
					drawUtils().drawString(fontRendererObj, button.displayString, x + 24, y + 7, ModColor.toRGB(r, r, r, 0));
				}
			}
			x = width / 2 - 120;
			drawUtils().drawString("Icon", x, (iconTypeButtons.get(0).wrappedButton.yPosition) - fontRendererObj.FONT_HEIGHT - 8, 1.2);


			if (entry.iconType == IconType.IMAGE_FILE) {
				int y = fileInput.yPosition;
				drawUtils().drawString("Datei", x, y - fontRendererObj.FONT_HEIGHT - 8, 1.2);
				if (entry.iconType == IconType.IMAGE_FILE) {
					if (entry.icon == null)
						fileInput.setText("§8Wähle eine Datei aus");
					else
						fileInput.setText(((File) entry.icon).getName());
				}
				fileInput.drawTextBox();
				fileButton.drawButton(mc, mouseX, mouseY);
				drawUtils().bindTexture(entry.icon == null ? MISC_HEAD_QUESTION : new ResourceLocation("griefer_utils/user_content/" + entry.icon.hashCode()));
				drawUtils().drawTexture(x, y, 256.0, 256.0, 20, 20);
			} else if (entry.iconType == IconType.ITEM) {
				x = (width / 2 - 120);
				int y = 50 + 80 + 183 + 88;
				itemSetting.draw(x, y, x + 240, y + 23, mouseX, mouseY);
			}
		}

		GL11.glTranslated(0, -scrollbar.getScrollY(), 0);

		draw.drawOverlayBackground(0, 45);
		draw.drawGradientShadowTop(45, 0.0, this.width);
		draw.drawOverlayBackground(this.height - 10, this.height);
		draw.drawGradientShadowBottom((double) this.height - 10, 0.0, this.width);
		buttonBack.drawButton(mc, mouseX, mouseY + (int) scrollbar.getScrollY());

		if (AddonInfoManager.getInstance().isLoaded()) {
			AddonElement openedAddonSettings = Reflection.get(addonsGui, "openedAddonSettings");
			draw.drawString(openedAddonSettings.getAddonInfo().getName(), this.width / 2f - 100 + 30, 25.0);
			openedAddonSettings.drawIcon(this.width / 2 + 100 - 20, 20, 20, 20);
		}

	}

	private int getLowerSpacing(int scale) {
		switch (scale) {
			case 1:
			case 2: return 0;
			case 3: return 75;
			default: return 85;
		}
	}

	public void updateScreen() {
		backgroundScreen.updateScreen();
		commandInput.updateCursorCounter();
		nameInput.updateCursorCounter();
		itemSetting.updateScreen();
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id / 100 == 1) {
			entry.action = actionButtons.get(button.id - 101).action;
			return;
		}
		else if (button.id / 100 == 2) {
			IconType oldType = entry.iconType;
			entry.iconType = iconTypeButtons.get(button.id - 201).type;
			if (entry.iconType != oldType) {
				entry.icon = null;
				itemSetting.reset();
			}
			fileButton.enabled = entry.iconType == IconType.IMAGE_FILE;
			return;
		}

		switch (button.id) {
			case 2:
				entry.iconType = IconType.DEFAULT;
				break;
			case 3:
				entry.iconType = IconType.ITEM;
				break;
			case 4:
				FileSelection.chooseFile(file -> {
					if (file == null)
						return;

					ResourceLocation location = new ResourceLocation("griefer_utils/user_content/" + file.hashCode());

					try {
						BufferedImage img = ImageIO.read(file);
						mc().getTextureManager().loadTexture(location, new DynamicTexture(img));
					} catch (IOException | NullPointerException e) {
						labyMod().getGuiCustomAchievement().displayAchievement("§e§l§nFehlerhafte Datei", "§eDie Datei konnte nicht als Bild geladen werden.");
						return;
					}

					entry.icon = file;
					fileInput.setText(file.getName());
				});
				break;
			case 7:
				if (ogEntry.completed) { // Save original entry if making entry invalid and going back
					new EntryDisplaySetting(ogEntry, FileProvider.getSingleton(ChatMenu.class).getMainElement());
					ChatMenu.saveEntries();
				}
				Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);
				backgroundScreen.initGui(); // Update settings
				break;
			case 1:
				entry.name = nameInput.getText();
				entry.command = commandInput.getText();
				entry.completed = true;
				new EntryDisplaySetting(entry, FileProvider.getSingleton(ChatMenu.class).getMainElement());
				ChatMenu.saveEntries();
				// Fall-through
			case 0:
				Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);
				backgroundScreen.initGui(); // Update settings
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (buttonBack.mousePressed(this.mc, mouseX, mouseY)) {
			buttonBack.playPressSound(this.mc.getSoundHandler());
			this.actionPerformed(buttonBack);
			return;
		}

		super.mouseClicked(mouseX, mouseY -= (int) scrollbar.getScrollY(), mouseButton);

		textCompareDropDown.onClick(mouseX, mouseY, mouseButton);
		commandInput.mouseClicked(mouseX, mouseY, mouseButton);
		nameInput.mouseClicked(mouseX, mouseY, mouseButton);
		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.CLICKED);
		if (entry.iconType == IconType.ITEM) {
			itemSetting.mouseClicked(mouseX, mouseY, mouseButton);
			itemSetting.onClickDropDown(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY -= scrollbar.getScrollY(), state);
		textCompareDropDown.onRelease(mouseX, mouseY, state);
		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.RELEASED);
		if (entry.iconType == IconType.ITEM)
			itemSetting.mouseRelease(mouseX, mouseY, state);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY -= scrollbar.getScrollY(), mouseButton, timeSinceLastClick);
		textCompareDropDown.onDrag(mouseX, mouseY, mouseButton);
		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.DRAGGING);
		if (entry.iconType == IconType.ITEM)
			itemSetting.mouseClickMove(mouseX, mouseY, mouseButton);
	}

	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == 1) // ESC
			Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);

		commandInput.textboxKeyTyped(typedChar, keyCode);
		nameInput.textboxKeyTyped(typedChar, keyCode);
		if (entry.iconType == IconType.ITEM)
			itemSetting.keyTyped(typedChar, keyCode);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		scrollbar.mouseInput();
		itemSetting.onScrollDropDown();
	}

	private static class IconTypeButton {
		private final GuiButton wrappedButton;
		private final IconType type;

		public IconTypeButton(int buttonId, int x, int y, int widthIn, int heightIn, IconType type) {
			this.wrappedButton = new GuiButton(buttonId, x, y, widthIn, heightIn, type.name);
			this.type = type;
		}
	}

	private static class ActionButton {
		private final GuiButton wrappedButton;
		private final Action action;
		private final ControlElement.IconData image;

		public ActionButton(int buttonId, int x, int y, int widthIn, int heightIn, Action action) {
			this.wrappedButton = new GuiButton(buttonId, x, y, widthIn, heightIn, action.name);
			this.image = new ControlElement.IconData("griefer_utils/icons/" + action.defaultIcon + ".png");
			this.action = action;
		}
	}

}