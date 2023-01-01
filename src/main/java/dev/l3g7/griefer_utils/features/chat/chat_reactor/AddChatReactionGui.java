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

package dev.l3g7.griefer_utils.features.chat.chat_reactor;

import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.gui.elements.ModTextField;
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
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class AddChatReactionGui extends GuiScreen {

	private final GuiScreen backgroundScreen;
	private ModTextField triggerInput;
	private ModTextField commandInput;
	private GuiButton cancelButton;
	private GuiButton doneButton;
	private Boolean regEx;
	private boolean validRegEx = true;
	private final ChatReaction reaction;

	private ImageButton parseModeText;
	private ImageButton parseModeRegEx;
	private GuiButton buttonBack;
	private DropDownMenu<TextCompareMode> textCompareDropDown;

	public AddChatReactionGui(ChatReaction reaction, GuiScreen backgroundScreen) {
		this.reaction = reaction == null ? new ChatReaction() : reaction;
		if (reaction != null)
			regEx = reaction.regEx;
		this.backgroundScreen = backgroundScreen;
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void initGui() {
		super.initGui();
		backgroundScreen.width = width;
		backgroundScreen.height = height;
		int y = 50 + 80;
		if (backgroundScreen instanceof LabyModModuleEditorGui)
			PreviewRenderer.getInstance().init(AddChatReactionGui.class);

		triggerInput = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 120, y + 115, 240, 20);
		triggerInput.setPlaceHolder("§8[GrieferUtils] [+] Zwiebackgesicht");
		triggerInput.setText(reaction.trigger);
		triggerInput.setMaxStringLength(Integer.MAX_VALUE);


		commandInput = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 120, y + 85 + 98, 240, 20);
		commandInput.setPlaceHolder("§8/startkick Zwiebackgesicht Scammer >:(");
		commandInput.setText(reaction.command);
		commandInput.setMaxStringLength(Integer.MAX_VALUE);

		buttonList.add(cancelButton = new GuiButton(0, width / 2 - 105, y + 85, 100, 20, reaction.completed ? "Löschen" : "Abbrechen"));
		buttonList.add(doneButton = new GuiButton(1, width / 2 + 5, y + 85, 100, 20, reaction.completed ? "Speichern" : "Hinzufügen"));

		int bgn = (width - 240) / 2;
		buttonList.add((parseModeText = new ImageButton(2, bgn, y + 45, 99, 23, "normaler Text", "yellow_t")).wrappedButton);
		buttonList.add((parseModeRegEx = new ImageButton(3, bgn + 110, y + 45, 130, 23, "regulärer Ausdruck", "regex")).wrappedButton);
		buttonList.add(buttonBack = new GuiButton(1, this.width / 2 - 100, 20, 22, 20, "<"));

		textCompareDropDown = new DropDownMenu<>("", 0, 0, 0, 0);
		textCompareDropDown.fill(TextCompareMode.values());
		textCompareDropDown.setSelected(reaction.matchAll ? TextCompareMode.EQUALS : TextCompareMode.CONTAINS);
		textCompareDropDown.setEntryDrawer((o, ex, ey, trimmedEntry) -> drawUtils().drawString(((TextCompareMode) o).name, ex, ey));
		textCompareDropDown.setY(y + 183 + 65);
		textCompareDropDown.setWidth(240);
		textCompareDropDown.setHeight(17);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GL11.glColorMask(false, false, false, false);
		for (GuiButton guiButton : this.buttonList) guiButton.drawButton(this.mc, mouseX, mouseY);
		GL11.glColorMask(true, true, true, true);


		LabyModAddonsGui addonsGui = (LabyModAddonsGui) backgroundScreen;
		DrawUtils draw = LabyMod.getInstance().getDrawUtils();
		draw.drawAutoDimmedBackground(0);

		draw.drawOverlayBackground(0, 45);
		draw.drawGradientShadowTop(45, 0.0, this.width);
		draw.drawOverlayBackground(this.height - 10, this.height);
		draw.drawGradientShadowBottom((double) this.height - 10, 0.0, this.width);

		if (AddonInfoManager.getInstance().isLoaded()) {
			AddonElement openedAddonSettings = Reflection.get(addonsGui, "openedAddonSettings");
			draw.drawString(openedAddonSettings.getAddonInfo().getName(), this.width / 2f - 100 + 30, 25.0);
			openedAddonSettings.drawIcon(this.width / 2 + 100 - 20, 20, 20, 20);
		}
		buttonBack.drawButton(mc, mouseX, mouseY);

		drawUtils().drawCenteredString("§e§l" + Constants.ADDON_NAME, width / 2f, 81, 1.3);
		drawUtils().drawCenteredString("§e§lChatReactor", width / 2f, 105, .7);

		for (ImageButton imgButton : new ImageButton[] {parseModeText, parseModeRegEx}) {
			GuiButton button = imgButton.wrappedButton;
			int x = button.xPosition;
			int y = button.yPosition;
			drawUtils().drawRectangle(x, y, x + button.width, y + button.height, ModColor.toRGB(80, 80, 80, 60));
			mc.getTextureManager().bindTexture(imgButton.image.getTextureIcon());
			boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + button.width && mouseY < y + button.height;

			if (hovered || (regEx != null && regEx == (imgButton == parseModeRegEx))) {
				drawUtils().drawTexture(x + 2, y + 2, 256.0, 256.0, 18, 18);
				drawUtils().drawString(button.displayString, x + 25, (double) y + 7);
			} else {
				drawUtils().drawTexture(x + 3, y + 3, 256.0, 256.0, 16.0, 16.0);
				int r = 180;
				drawUtils().drawString(fontRendererObj, button.displayString, x + 24, y + 7, ModColor.toRGB(r, r, r, 0));
			}
		}
		int x = width / 2 - 120;
		drawUtils().drawString("Text-Form", x, (parseModeText.wrappedButton.yPosition) - fontRendererObj.FONT_HEIGHT - 8, 1.2);


		int y = regEx == null ? 22 + 80 + 115 + 8 : regEx ? 50 + 183 + 40 + 8 + 80 : 50 + 183 + 65 + 80 + 17 + 28;

		doneButton.yPosition = cancelButton.yPosition = y;
		doneButton.enabled = regEx != null && !triggerInput.getText().isEmpty() && !commandInput.getText().isEmpty() && (!regEx || validRegEx);
		buttonBack.id = doneButton.enabled ? 1 : 0;
		doneButton.drawButton(mc, mouseX, mouseY);
		cancelButton.drawButton(mc, mouseX, mouseY);

		if (regEx != null) {
			drawUtils().drawString(regEx ? "Regulärer Ausdruck" : "Text", x, triggerInput.yPosition - fontRendererObj.FONT_HEIGHT - 8, 1.2);
			triggerInput.setPlaceHolder(regEx ? "§8^\\[[^ ]+ \\┃ ([^ ]+) -> mir] (.*)$" : "§8[GrieferUtils] [+] Zwiebackgesicht");
			triggerInput.drawTextBox();

			commandInput.setPlaceHolder(regEx ? "§8/msg MainAcc \\1: \\2" : "§8/startkick Zwiebackgesicht Scammer");
			drawUtils().drawString("Befehl", x, commandInput.yPosition - fontRendererObj.FONT_HEIGHT - 8, 1.2);
			commandInput.drawTextBox();

			if (!regEx) {
				textCompareDropDown.setX(x);
				textCompareDropDown.draw(mouseX, mouseY);
				drawUtils().drawString("Auslösen", x, textCompareDropDown.getY() - fontRendererObj.FONT_HEIGHT - 8, 1.2);

				// Draw dropdown with fixed width
				if (textCompareDropDown.isOpen())
					textCompareDropDown.drawMenuDirect(textCompareDropDown.getX(), textCompareDropDown.getY(), mouseX, mouseY);
			}
		}
	}

	public void updateScreen() {
		backgroundScreen.updateScreen();
		triggerInput.updateCursorCounter();
		commandInput.updateCursorCounter();
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		switch (button.id) {
			case 2:
				regEx = false;
				break;
			case 3:
				regEx = true;
				break;
			case 1:
				reaction.regEx = regEx;
				reaction.matchAll = textCompareDropDown.getSelected() == TextCompareMode.EQUALS;
				reaction.trigger = triggerInput.getText();
				reaction.command = commandInput.getText();
				reaction.completed = true;
				new ReactionDisplaySetting(reaction, FileProvider.getSingleton(ChatReactor.class).getMainElement())
					.icon(regEx ? parseModeRegEx.image.getTextureIcon() : parseModeText.image.getTextureIcon());
				ChatReactor.saveEntries();
				// Fall-through
			case 0:
				Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);
				backgroundScreen.initGui(); // Update settings
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		textCompareDropDown.onClick(mouseX, mouseY, mouseButton);
		triggerInput.mouseClicked(mouseX, mouseY, mouseButton);
		commandInput.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		textCompareDropDown.onRelease(mouseX, mouseY, state);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
		textCompareDropDown.onDrag(mouseX, mouseY, mouseButton);
	}

	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == 1) // ESC
			Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);

		if (triggerInput.textboxKeyTyped(typedChar, keyCode) && regEx != null && regEx) {
			doneButton.enabled = !commandInput.getText().isEmpty();
			try {
				Pattern.compile(triggerInput.getText());
				validRegEx = true;
			} catch (PatternSyntaxException e) {
				validRegEx = false;
			}
		}
		commandInput.textboxKeyTyped(typedChar, keyCode);
	}

	private static class ImageButton {
		private final GuiButton wrappedButton;
		private final ControlElement.IconData image;

		public ImageButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, String image) {
			this.wrappedButton = new GuiButton(buttonId, x, y, widthIn, heightIn, buttonText);
			this.image = new ControlElement.IconData("griefer_utils/icons/" + image + ".png");
		}
	}

	private enum TextCompareMode {
		CONTAINS("Wenn die Nachricht den Text beinhaltet"), EQUALS("Wenn die Nachricht dem Text entspricht");
		private final String name;
		TextCompareMode(String name) {
			this.name = name;
		}
	}

}