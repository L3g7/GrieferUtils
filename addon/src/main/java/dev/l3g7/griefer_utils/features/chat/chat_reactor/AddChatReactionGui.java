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

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.settings.elements.ItemSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
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
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
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

	private ItemSetting cityBuildSetting;

	private final Scrollbar scrollbar = new Scrollbar(1);

	public AddChatReactionGui(ChatReaction reaction, GuiScreen backgroundScreen) {
		this.reaction = reaction == null ? new ChatReaction() : reaction;
		if (reaction != null)
			regEx = reaction.regEx;
		if (reaction == null)
			this.reaction.enabled = true;
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
		if (backgroundScreen instanceof LabyModModuleEditorGui)
			PreviewRenderer.getInstance().init(AddChatReactionGui.class);

		triggerInput = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 120, y + 95, 240, 20);
		triggerInput.setPlaceHolder("§8[GrieferUtils] [+] 1Plugin");
		triggerInput.setMaxStringLength(Integer.MAX_VALUE);
		triggerInput.setText(reaction.trigger);


		commandInput = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 120, y + 85 + 78, 240, 20);
		commandInput.setPlaceHolder("§8/startkick 1Plugin Scammer >:(");
		commandInput.setMaxStringLength(Integer.MAX_VALUE);
		commandInput.setText(reaction.command);

		buttonList.add(cancelButton = new GuiButton(0, width / 2 - 105, y + 85, 100, 20, reaction.completed ? "Löschen" : "Abbrechen"));
		buttonList.add(doneButton = new GuiButton(1, width / 2 + 5, y + 85, 100, 20, reaction.completed ? "Speichern" : "Hinzufügen"));

		int bgn = (width - 240) / 2;
		buttonList.add((parseModeText = new ImageButton(2, bgn, y + 25, 99, 23, "normaler Text", "yellow_t")).wrappedButton);
		buttonList.add((parseModeRegEx = new ImageButton(3, bgn + 110, y + 25, 130, 23, "regulärer Ausdruck", "regex")).wrappedButton);
		buttonList.add(buttonBack = new GuiButton(1, this.width / 2 - 100, 20, 22, 20, "<"));

		textCompareDropDown = new DropDownMenu<>("", 0, 0, 0, 0);
		textCompareDropDown.fill(TextCompareMode.values());
		textCompareDropDown.setSelected(reaction.matchAll ? TextCompareMode.EQUALS : TextCompareMode.CONTAINS);
		textCompareDropDown.setEntryDrawer((o, ex, ey, trimmedEntry) -> drawUtils().drawString(((TextCompareMode) o).name, ex, ey));
		textCompareDropDown.setY(y + 183 + 45);
		textCompareDropDown.setWidth(240);
		textCompareDropDown.setHeight(17);

		// 50 + 80 + 183 + 45
		cityBuildSetting = new ItemSetting(ItemUtil.CB_ITEMS, false)
			.name("CityBuild")
			.description("Die ID / der Namespace des Items / Blocks, das als Icon angezeigt werden soll");

		for (ItemStack cb : ItemUtil.CB_ITEMS) {
			if (cb.getDisplayName().equals(reaction.cityBuild)) {
				cityBuildSetting.defaultValue(cb);
				break;
			}
		}

		cityBuildSetting.init();
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GL11.glColorMask(false, false, false, false);
		for (GuiButton guiButton : this.buttonList) guiButton.drawButton(this.mc, mouseX, mouseY);
		GL11.glColorMask(true, true, true, true);
		int height = regEx == null ? 205 : regEx ? 50 + 183 + 20 + 8 + 80 + 42 : 50 + 183 + 65 + 60 + 17 + 28 + 42;
		int guiScale = new ScaledResolution(mc).getScaleFactor();
		scrollbar.update(height - (int) scrollbar.getTop() + (regEx == null ? 0 : regEx ? -91 + (80 * guiScale) : (17 + (50 * guiScale))));


		LabyModAddonsGui addonsGui = (LabyModAddonsGui) backgroundScreen;
		DrawUtils draw = LabyMod.getInstance().getDrawUtils();
		draw.drawAutoDimmedBackground(scrollbar.getScrollY());

		GL11.glTranslated(0, scrollbar.getScrollY(), 0);
		mouseY -= scrollbar.getScrollY();
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



		doneButton.yPosition = cancelButton.yPosition = height - 8;
		doneButton.enabled = regEx != null && !triggerInput.getText().isEmpty() && !commandInput.getText().isEmpty() && (!regEx || validRegEx);
		buttonBack.id = doneButton.enabled ? 1 : 0;
		doneButton.drawButton(mc, mouseX, mouseY);
		cancelButton.drawButton(mc, mouseX, mouseY);

		if (regEx != null) {
			drawUtils().drawString(regEx ? "Regulärer Ausdruck" : "Text", x, triggerInput.yPosition - fontRendererObj.FONT_HEIGHT - 8, 1.2);
			triggerInput.setPlaceHolder(regEx ? "§8^\\[[^ ]+ ┃ ([^ ]+) -> mir] (.*)$" : "§8[GrieferUtils] [+] 1Plugin");
			triggerInput.drawTextBox();

			commandInput.setPlaceHolder(regEx ? "§8/msg MainAcc \\1: \\2" : "§8/startkick 1Plugin Scammer >:(");
			drawUtils().drawString("Befehl", x, commandInput.yPosition - fontRendererObj.FONT_HEIGHT - 8, 1.2);
			commandInput.drawTextBox();

			int y = regEx ? 50 + 80 + 183 + 20 : 50 + 80 + 183 + 82;
			cityBuildSetting.draw(x, y, x + 240, y + 23, mouseX, mouseY);

			if (!regEx) {
				textCompareDropDown.setX(x);
				textCompareDropDown.draw(mouseX, mouseY);
				drawUtils().drawString("Auslösen", x, textCompareDropDown.getY() - fontRendererObj.FONT_HEIGHT - 8, 1.2);

				// Draw dropdown with fixed width
				if (textCompareDropDown.isOpen())
					textCompareDropDown.drawMenuDirect(textCompareDropDown.getX(), textCompareDropDown.getY(), mouseX, mouseY);
			}
		}
		GL11.glTranslated(0, -scrollbar.getScrollY(), 0);

		draw.drawOverlayBackground(0, 45);
		draw.drawGradientShadowTop(45, 0.0, this.width);
		draw.drawOverlayBackground(this.height - 10, this.height);
		draw.drawGradientShadowBottom((double) this.height - 10, 0.0, this.width);

		scrollbar.draw(mouseX, mouseY);

		AddonElement openedAddonSettings = Reflection.get(addonsGui, "openedAddonSettings");
		draw.drawString(openedAddonSettings.getAddonInfo().getName(), this.width / 2f - 100 + 30, 25.0);
		openedAddonSettings.drawIcon(this.width / 2 + 100 - 20, 20, 20, 20);
		buttonBack.drawButton(mc, mouseX, mouseY + (int) scrollbar.getScrollY());
	}

	public void updateScreen() {
		backgroundScreen.updateScreen();
		triggerInput.updateCursorCounter();
		commandInput.updateCursorCounter();
		cityBuildSetting.updateScreen();
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
				if (cityBuildSetting.isOpen())
					return;
				reaction.regEx = regEx;
				reaction.matchAll = textCompareDropDown.getSelected() == TextCompareMode.EQUALS;
				reaction.trigger = triggerInput.getText();
				reaction.command = commandInput.getText();
				reaction.cityBuild = cityBuildSetting.get().getDisplayName();
				reaction.completed = true;
				new ReactionDisplaySetting(reaction, FileProvider.getSingleton(ChatReactor.class).getMainElement())
					.icon(regEx ? parseModeRegEx.image.getTextureIcon() : parseModeText.image.getTextureIcon());
				ChatReactor.saveEntries();
				// Fall-through
			case 0:
				if (cityBuildSetting.isOpen())
					return;
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
		triggerInput.mouseClicked(mouseX, mouseY, mouseButton);
		commandInput.mouseClicked(mouseX, mouseY, mouseButton);
		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.CLICKED);
		cityBuildSetting.mouseClicked(mouseX, mouseY, mouseButton);
		cityBuildSetting.onClickDropDown(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY -= scrollbar.getScrollY(), state);
		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.RELEASED);
		cityBuildSetting.mouseRelease(mouseX, mouseY, state);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY -= scrollbar.getScrollY(), mouseButton, timeSinceLastClick);
		textCompareDropDown.onDrag(mouseX, mouseY, mouseButton);
		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.DRAGGING);
		cityBuildSetting.mouseClickMove(mouseX, mouseY, mouseButton);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		cityBuildSetting.onScrollDropDown();
		if (!cityBuildSetting.isOpen())
			scrollbar.mouseInput();
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
		cityBuildSetting.keyTyped(typedChar, keyCode);
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