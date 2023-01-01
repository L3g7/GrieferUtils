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

package dev.l3g7.griefer_utils.features.uncategorized.settings.auto_update;

import dev.l3g7.griefer_utils.event.EventHandler;
import dev.l3g7.griefer_utils.event.EventListener;
import net.labymod.main.LabyMod;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChangelogScreen extends GuiScreen {

	private static boolean triggered = false;
	private static String version = null;
	private static String changelog = null;

	private TextList textList;
	private GuiScreen previousScreen;

	// Make sure the gui closes to the correct screen
	@EventListener(priority = EventPriority.LOWEST)
	public void onGuiOpen(GuiOpenEvent event) {
		if (event.isCanceled() || event.gui instanceof ChangelogScreen)
			return;

		previousScreen = event.gui;
		event.setCanceled(true);
	}

	public static void trigger() {
		triggered = true;

		if (version != null)
			Minecraft.getMinecraft().displayGuiScreen(new ChangelogScreen());
	}

	public static boolean hasData() {
		return version != null;
	}

	public static void setData(String version, String changelog) {
		ChangelogScreen.version = version;
		ChangelogScreen.changelog = changelog;

		if (triggered)
			Minecraft.getMinecraft().displayGuiScreen(new ChangelogScreen());
	}

	public ChangelogScreen() {
		EventHandler.register(this);
	}

	public void initGui() {
		super.initGui();

		textList = new TextList(mc, width, height, 64, height - 42, fontRendererObj);
		textList.addEntries(changelog);
		textList.addEntry("");

		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 + 4 + 75, height - 28, 150, 20, "Schließen"));
	}

	public void closeGui() {
		MinecraftForge.EVENT_BUS.unregister(this);
		mc.displayGuiScreen(previousScreen);
	}

	private boolean isLeftButtonHovered(int mouseX, int mouseY) {
		return mouseX > this.width / 2 - 205 && mouseX < this.width / 2 - 54 && mouseY > this.height - 28 && mouseY < this.height - 8;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		textList.drawScreen(mouseX, mouseY, partialTicks);

		String text = "§nGrieferUtils - Changelog - " + version;

		// Title
		GlStateManager.scale(1.5, 1.5, 1.5);
		drawCenteredString(fontRendererObj, text, width / 3, 15, 0xffffff);
		GlStateManager.scale(1/1.5, 1/1.5, 1/1.5);

		// Icon
		int textWidth = fontRendererObj.getStringWidth(text);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/icon.png"));
		LabyMod.getInstance().getDrawUtils().drawTexture(width / 2d - textWidth * 0.75 - 29, 18, 256, 256, 20, 20);

		// Left button
		text = ModColor.cl(isLeftButtonHovered(mouseX, mouseY) ? 'c' : '7') + "Nicht nochmal anzeigen";
		LabyMod.getInstance().getDrawUtils().drawString(text, width / 2d - 186, height - 22);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode != 1)
			super.keyTyped(typedChar, keyCode);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (isLeftButtonHovered(mouseX, mouseY)) {
			AutoUpdate.showChangelog.set(false);
			closeGui();
			return;
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		textList.handleMouseInput();
	}

	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		textList.mouseReleased(mouseX, mouseY, state);
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		closeGui();
	}


	private static class TextList extends GuiListExtended {

		private static final int MAX_LENGTH = 393;

		private final List<Line> lines = new ArrayList<>();
		private final FontRenderer fontRenderer;
		private int textWidth = 0;

		public TextList(Minecraft mc, int width, int height, int top, int bottom, FontRenderer fontRenderer) {
			super(mc, width, height, top, bottom, fontRenderer.FONT_HEIGHT + 1);
			this.fontRenderer = fontRenderer;
		}

		public void addEntry(String text) {
			int stringWidth = fontRenderer.getStringWidth(text);

			if (stringWidth <= MAX_LENGTH) {
				lines.add(new Line(text));
				textWidth = Math.max(textWidth, stringWidth);
				return;
			}

			while (stringWidth > MAX_LENGTH) {

				String partString;

				// Limit the string to a width of 393
				for (int i = text.length(); true; i--) {
					partString = text.substring(0, i);

					if (fontRenderer.getStringWidth(partString) <= MAX_LENGTH)
						break;
				}

				// Chop up string at space
				int i = partString.length() - 1;
				while (true) {
					if (i < 0) {
						i = partString.length() - 1;
						break;
					}

					if (partString.charAt(i) == ' ')
						break;

					i--;
				}

				// Add the string and continue with the rest
				lines.add(new Line(text.substring(0, i)));

				text = text.substring(i + 1);

				stringWidth = fontRenderer.getStringWidth(text);
			}

			lines.add(new Line(text));
			textWidth = MAX_LENGTH;
		}

		@Override
		protected int getScrollBarX() {
			return width - 24;
		}

		@Override
		public int getListWidth() {
			return textWidth;
		}

		public void addEntries(String multiLineText) {
			for (String s : multiLineText.replace("\r", "").split("\n"))
				addEntry(s);
		}

		public IGuiListEntry getListEntry(int index) {
			return lines.get(index);
		}

		protected int getSize() {
			return lines.size();
		}

		private static class Line implements IGuiListEntry {

			private final String text;

			public Line(String text) {
				this.text = text;
			}

			public void setSelected(int a, int b, int c) {}
			public boolean mousePressed(int a, int b, int c, int d, int e, int f) {return false;}
			public void mouseReleased(int a, int b, int c, int d, int e, int f) {}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				LabyMod.getInstance().getDrawUtils().drawString(text, x, y);
			}

		}

	}
}