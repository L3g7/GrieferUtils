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

package dev.l3g7.griefer_utils.misc.gui;


import net.labymod.main.LabyMod;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MissingForgeErrorGui extends GuiScreen {

	public static void open() {
		GuiScreen gui = new MissingForgeErrorGui(Minecraft.getMinecraft().currentScreen);
		// Ensure it can't be closed
		// There are no events that can be used, so a timer is used instead (I can't be bothered to transform)
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (!(Minecraft.getMinecraft().currentScreen instanceof MissingForgeErrorGui))
					Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(gui));
			}
		}, 0, 50);
	}

	private final GuiScreen previousScreen;

	public MissingForgeErrorGui(GuiScreen previousScreen) {
		this.previousScreen = previousScreen;
	}

	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height / 2 + 24, 125, 20, "Minecraft schließen"));
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode != 1)
			super.keyTyped(typedChar, keyCode);
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 1)
			Minecraft.getMinecraft().shutdown();
		else
			super.actionPerformed(button);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawBackground(0);

		DrawUtils draw = LabyMod.getInstance().getDrawUtils();
		draw.drawString("§c§nGrieferUtils benötigt Forge!", (double) this.width / 2 - 150, (double) this.height / 2 - 80, 1.5);
		String message = "GrieferUtils benötigt Minecraft Forge, um aktiviert werden zu können.\n\nBitte installiere Forge, um fortzufahren.";
		List<String> list = draw.listFormattedStringToWidth(message, 203);

		double y = 0;
		for (String text : list) {
			draw.drawString(text, (double) this.width / 2 - 150, (double) this.height / 2 - 50 + y, 1.2);
			y += 10*1.2;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/icon.png"));
		draw.drawTexture((double) this.width / 2 + 150 - 60, (double) this.height / 2 - 95, 256.0, 256.0, 60.0, 60.0);
		boolean mouseOver = mouseX > this.width / 2 - 150 && mouseX < this.width / 2 && mouseY > this.height / 2 + 24 && mouseY < this.height / 2 + 44;
		draw.drawString(ModColor.cl(mouseOver ? 'c' : '7') + "Ohne GrieferUtils spielen", (double) this.width / 2 - 150, (double) this.height / 2 + 30);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseX > this.width / 2 - 150 && mouseX < this.width / 2 && mouseY > this.height / 2 + 24 && mouseY < this.height / 2 + 44)
			Minecraft.getMinecraft().displayGuiScreen(this.previousScreen);
		else
			super.mouseClicked(mouseX, mouseY, mouseButton);
	}
}