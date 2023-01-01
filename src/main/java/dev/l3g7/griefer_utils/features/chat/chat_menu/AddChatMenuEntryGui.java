/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

import dev.l3g7.griefer_utils.features.chat.chat_menu._ChatMenuEntry.Action;
import dev.l3g7.griefer_utils.features.chat.chat_menu._ChatMenuEntry.IconType;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.gui.elements.ModTextField;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.main.LabyMod;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.labymod.settings.elements.AddonElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class AddChatMenuEntryGui extends GuiScreen {

	private final GuiScreen backgroundScreen;
	private ModTextField commandInput;
	private ModTextField nameInput;
	private GuiButton cancelButton;
	private GuiButton doneButton;
	private final _ChatMenuEntry entry;

	private final List<ActionButton> actionButtons = new ArrayList<>();
	private final List<IconTypeButton> iconTypeButtons = new ArrayList<>();
	private GuiButton buttonBack;
	private DropDownMenu<Action> textCompareDropDown;

	private ModTextField fileInput;
	private GuiButton fileButton;

	private final Scrollbar scrollbar = new Scrollbar(1);

	public AddChatMenuEntryGui(_ChatMenuEntry entry, GuiScreen backgroundScreen) {
		this.entry = entry == null ? new _ChatMenuEntry() : entry;
		this.backgroundScreen = backgroundScreen;
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void initGui() {
		super.initGui();
		this.scrollbar.setPosition(this.width / 2 + 172 , 135, this.width / 2 + 172 + 4, this.height - 15);
		this.scrollbar.setSpeed(20);
		this.scrollbar.init();

		entry.action = Action.RUN_CMD;
		entry.iconType = IconType.IMAGE;
		entry.icon = new File("");
		backgroundScreen.width = width;
		backgroundScreen.height = height;
		int y = 50 + 80;

		commandInput = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 120, y + 115, 240, 20);
		commandInput.setPlaceHolder("");
		commandInput.setText(entry.command);
		commandInput.setMaxStringLength(Integer.MAX_VALUE);


		nameInput = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 120, y + 85 + 98, 240, 20);
		nameInput.setPlaceHolder("§8/startkick Zwiebackgesicht Scammer >:(");
		nameInput.setText(entry.command);
		nameInput.setMaxStringLength(Integer.MAX_VALUE);

		buttonList.add(cancelButton = new GuiButton(0, width / 2 - 105, y + 115, 100, 20, entry.completed ? "Löschen" : "Abbrechen"));
		buttonList.add(doneButton = new GuiButton(1, width / 2 + 5, y + 115, 100, 20, entry.completed ? "Speichern" : "Hinzufügen"));

		int bgn = (width - 336) / 2;

		int x = 0;
		for (int i = 0; i < Action.values().length; i++) {
			Action value = Action.values()[i];
			int sLen = drawUtils().getStringWidth(value.name) + 29;
			ActionButton btn = new ActionButton(100 + i, bgn + x, y + 45, sLen, 23, value);
			buttonList.add(btn.wrappedButton);
			actionButtons.add(btn);
			x += sLen + 4;
		}
		buttonList.add(buttonBack = new GuiButton(1, this.width / 2 - 100, 20, 22, 20, "<"));

		bgn = (width - 240) / 2;


		x = 0;
		for (int i = 0; i < IconType.values().length; i++) {
			IconType value = IconType.values()[i];
			int sLen = drawUtils().getStringWidth(value.name) + 29 + 20;
			IconTypeButton btn = new IconTypeButton(200 + i, bgn + x, y + 183 + 65 + 3, sLen, 23, value);
			buttonList.add(btn.wrappedButton);
			iconTypeButtons.add(btn);
			x += sLen + 4;
		}
		buttonList.add(buttonBack = new GuiButton(1, this.width / 2 - 100, 20, 22, 20, "<"));

		textCompareDropDown = new DropDownMenu<>("", 0, 0, 0, 0);
		textCompareDropDown.fill(Action.values());
		textCompareDropDown.setSelected(entry.action);
		textCompareDropDown.setEntryDrawer((o, ex, ey, trimmedEntry) -> drawUtils().drawString(((Action) o).name, ex, ey));
		textCompareDropDown.setY(y + 183 + 65);
		textCompareDropDown.setWidth(240);
		textCompareDropDown.setHeight(17);



		fileInput = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), bgn, y + 183 + 108 + 30, 240 - 60 - 6, 20);
		fileInput.setPlaceHolder("");
		if (entry.iconType == IconType.IMAGE)
			fileInput.setText(((File) entry.icon).getName()); // 423
		fileInput.setMaxStringLength(Integer.MAX_VALUE);
		buttonList.add(fileButton = new GuiButton(4, this.width / 2 + 60, y + 183 + 108 + 30, 60, 20, "Auswählen"));
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int height = (entry.action == null ? 228 : (entry.iconType != null && entry.iconType != IconType.DEFAULT ? 423 + 89 : 423)) + 20;
		scrollbar.update(height - (int) scrollbar.getTop());


		GL11.glColorMask(false, false, false, false);
		for (GuiButton guiButton : this.buttonList) guiButton.drawButton(this.mc, mouseX, mouseY);
		GL11.glColorMask(true, true, true, true);

		LabyModAddonsGui addonsGui = (LabyModAddonsGui) backgroundScreen;
		DrawUtils draw = LabyMod.getInstance().getDrawUtils();
		draw.drawAutoDimmedBackground(0); //         draw.drawAutoDimmedBackground(this.scrollbar.getScrollY());

		draw.drawOverlayBackground(0, 45);
		draw.drawGradientShadowTop(45, 0.0, this.width);
		draw.drawOverlayBackground(this.height - 10, this.height);
		draw.drawGradientShadowBottom((double) this.height - 10, 0.0, this.width);

		scrollbar.draw(mouseX, mouseY);

		if (AddonInfoManager.getInstance().isLoaded()) {
			AddonElement openedAddonSettings = Reflection.get(addonsGui, "openedAddonSettings");
			draw.drawString(openedAddonSettings.getAddonInfo().getName(), this.width / 2f - 100 + 30, 25.0);
			openedAddonSettings.drawIcon(this.width / 2 + 100 - 20, 20, 20, 20);
		}
		buttonBack.drawButton(mc, mouseX, mouseY);

		drawUtils().drawCenteredString("§e§l" + Constants.ADDON_NAME, width / 2f, 81, 1.3);
		drawUtils().drawCenteredString("§e§lChatmenü", width / 2f, 105, .7);

		GL11.glTranslated(0, scrollbar.getScrollY(), 0);

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

		doneButton.yPosition = cancelButton.yPosition = height - 20;
		doneButton.enabled = !commandInput.getText().isEmpty() && !nameInput.getText().isEmpty();
		buttonBack.id = doneButton.enabled ? 1 : 0;
		doneButton.drawButton(mc, mouseX, mouseY);
		cancelButton.drawButton(mc, mouseX, mouseY);

		if (entry.action == null)
			return;

		String cmdTitle = "";
		switch (entry.action) {
			case OPEN_URL:
				cmdTitle = "URL";
				nameInput.setPlaceHolder("§8NameMC öffnen");
				commandInput.setPlaceHolder("§8https://namemc.com/search?q=%player%");
				break;
			case RUN_CMD:
				cmdTitle = "Befehl";
				nameInput.setPlaceHolder("§8Spieler kicken");
				commandInput.setPlaceHolder("§8/startkick %player%");
				break;
			case SUGGEST_CMD:
				cmdTitle = "Befehl";
				nameInput.setPlaceHolder("§8MSG an Spieler");
				commandInput.setPlaceHolder("§8/msg %player% ");
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


		if (entry.iconType == IconType.IMAGE) {
			drawUtils().drawString("Datei", x, (fileInput.yPosition) - fontRendererObj.FONT_HEIGHT - 8, 1.2);
			fileInput.drawTextBox();
			fileButton.drawButton(mc, mouseX, mouseY);
		}
/*		if (entry.iconType != null && entry.iconType != IconType.DEFAULT) {
			textCompareDropDown.setX(x);
			textCompareDropDown.draw(mouseX, mouseY);
			drawUtils().drawString("Auslösen", x, textCompareDropDown.getY() - fontRendererObj.FONT_HEIGHT - 8, 1.2);

			// Draw dropdown with fixed width
			if (textCompareDropDown.isOpen())
				textCompareDropDown.drawMenuDirect(textCompareDropDown.getX(), textCompareDropDown.getY(), mouseX, mouseY);
		}*/
	}

	public void updateScreen() {
		backgroundScreen.updateScreen();
		commandInput.updateCursorCounter();
		nameInput.updateCursorCounter();
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.id / 100 == 1) {
			entry.action = actionButtons.get(button.id - 100).action;
			return;
		}
		else if (button.id / 100 == 2) {
			entry.iconType = iconTypeButtons.get(button.id - 200).type;
			return;
		}
		switch (button.id) {
			case 2:
				entry.iconType = IconType.DEFAULT;
				break;
			case 3:
				entry.iconType = IconType.ITEM;
				break;
			case 1:
				entry.command = nameInput.getText();
				entry.completed = true;
//				new ReactionDisplaySetting(entry, FileProvider.getSingleton(ChatReactor.class).getMainElement()).icon(icon ? parseModeRegEx.image.getTextureIcon() : parseModeText.image.getTextureIcon());
//				ChatReactor.saveEntries();
				// Fall-through
			case 0:
				Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);
				backgroundScreen.initGui(); // Update settings
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		textCompareDropDown.onClick(mouseX, mouseY, mouseButton);
		commandInput.mouseClicked(mouseX, mouseY, mouseButton);
		nameInput.mouseClicked(mouseX, mouseY, mouseButton);
		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.CLICKED);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		textCompareDropDown.onRelease(mouseX, mouseY, state);
		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.RELEASED);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
		textCompareDropDown.onDrag(mouseX, mouseY, mouseButton);
		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.DRAGGING);
	}

	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == 1) // ESC
			Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);

		commandInput.textboxKeyTyped(typedChar, keyCode);
		nameInput.textboxKeyTyped(typedChar, keyCode);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		scrollbar.mouseInput();
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