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


import net.labymod.main.LabyMod;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class UpdateScreen extends GuiScreen {

	private static boolean triggered = false;
	private static String version = null;
	private static String changelog = null;

	private TextList textList;
	private GuiScreen previousScreen;

	// Make sure the gui closes to the correct screen
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onGuiOpen(GuiOpenEvent event) {
		if (event.isCanceled() || event.gui instanceof UpdateScreen)
			return;

		previousScreen = event.gui;
		event.setCanceled(true);
	}

	public static void trigger() {
		triggered = true;

		if (version == null)
			return;

		mc().displayGuiScreen(new UpdateScreen());
	}

	public static boolean hasData() {
		return version != null;
	}

	public static void setData(String version, String changelog) {
		UpdateScreen.version = version;
		UpdateScreen.changelog = changelog;

		if (!triggered)
			return;

		mc().displayGuiScreen(new UpdateScreen());
	}

	public UpdateScreen() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void initGui() {
		super.initGui();

		textList = new TextList(mc, width, height, 64, height - 42, fontRendererObj);
		textList.addEntry("");
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
			AutoUpdate.showUpdateScreen.set(false);
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

}
