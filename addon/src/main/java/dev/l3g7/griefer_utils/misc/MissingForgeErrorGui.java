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

package dev.l3g7.griefer_utils.misc;


import dev.l3g7.griefer_utils.features.uncategorized.settings.auto_update.TextList;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MissingForgeErrorGui extends GuiScreen {

	private final String text;
	private TextList textList;

	public static void open() {
		new MissingForgeErrorGui("\nGrieferUtils benötigt Minecraft Forge, um aktiviert werden zu können.\nBitte installiere Forge, um fortzufahren.");
	}

	private MissingForgeErrorGui(String text) {
		this.text = text;

		// Ensure it can't be closed
		// There are no events that can be used, so a timer is used instead (I can't be bothered to transform)
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (!(Minecraft.getMinecraft().currentScreen instanceof MissingForgeErrorGui))
					Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(MissingForgeErrorGui.this));
			}
		}, 0, 50);
	}

	public void initGui() {
		super.initGui();

		textList = new TextList(mc, width, height, 64, height - 42, fontRendererObj);
		textList.addEntries(text);
		textList.addEntry("");

		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 + 4 + 75, height - 28, 150, 20, "Minecraft schließen"));
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		textList.drawScreen(mouseX, mouseY, partialTicks);

		String text = "§nGrieferUtils benötigt Forge!";

		// Title
		GlStateManager.scale(1.5, 1.5, 1.5);
		drawCenteredString(fontRendererObj, text, width / 3, 15, 0xFF4444);
		GlStateManager.scale(1/1.5, 1/1.5, 1/1.5);

		// Icon
		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1);
		int textWidth = fontRendererObj.getStringWidth(text);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/icon.png"));
		LabyMod.getInstance().getDrawUtils().drawRawTexture(width / 2d - textWidth * 0.75 - 29, 18, 256, 256, 20, 20);
		GlStateManager.popMatrix();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode != 1)
			super.keyTyped(typedChar, keyCode);
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
		Minecraft.getMinecraft().shutdown();
	}

}