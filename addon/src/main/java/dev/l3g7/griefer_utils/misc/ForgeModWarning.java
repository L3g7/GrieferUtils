/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

import dev.l3g7.griefer_utils.core.reflection.Reflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.NetworkModHolder;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForgeModWarning {

	public static boolean loadedUsingLabyMod = false;

	@Mod(modid = "griefer_utils", name = "GrieferUtils", version = "0")
	public static class ForgeMod {

		private boolean warningShown = false;

		public ForgeMod() {
			MinecraftForge.EVENT_BUS.register(this);
		}

		@SubscribeEvent
		public void onInit(GuiOpenEvent event) {
			if (event.gui instanceof GuiMainMenu && !warningShown) {
				warningShown = true;

				// Hide GrieferUtils from mods
				List<ModContainer> mods = new ArrayList<>(Loader.instance().getModList());
				mods.removeIf(m -> "griefer_utils".equals(m.getModId()));
				Reflection.set(Loader.instance(), mods, "mods");

				Map<String, ModContainer> namedMods = new HashMap<>(Loader.instance().getIndexedModList());
				namedMods.remove("griefer_utils");
				Reflection.set(Loader.instance(), namedMods, "namedMods");

				Map<ModContainer, NetworkModHolder> registry = new HashMap<>(NetworkRegistry.INSTANCE.registry());
				registry.keySet().removeIf(m -> "griefer_utils".equals(m.getModId()));
				Reflection.set(NetworkRegistry.INSTANCE, registry, "registry");

				Loader.instance().getActiveModList().removeIf(m -> "griefer_utils".equals(m.getModId()));

				// Show warning
				if (!loadedUsingLabyMod)
					event.gui = new GuiWarning(event.gui);
			}
		}
	}

	private static class GuiWarning extends GuiScreen {

		private final GuiScreen previousScreen;

		public GuiWarning(GuiScreen previousScreen) {
			this.previousScreen = previousScreen;
		}

		@Override
		public void initGui() {
			super.initGui();
			this.buttonList.clear();
			this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height / 2 + 40, 125, 20, "Minecraft beenden"));
		}

		@Override
		protected void keyTyped(char typedChar, int keyCode) throws IOException {
			if (keyCode != 1) {
				super.keyTyped(typedChar, keyCode);
			}
		}

		@Override
		protected void actionPerformed(GuiButton button) throws IOException {
			super.actionPerformed(button);
			if (button.id == 1)
				Minecraft.getMinecraft().shutdown();
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			drawBackground(0);
			Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("§eGrieferUtils befindet sich im falschen Ordner!", this.width / 2f - 150, this.height / 2f - 60, 0xFFFFFF);

			// Draw explanation
			int y = 0;
			String jarName = getJarName();
			for (String line : Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth("§7GrieferUtils befindet sich im §fmods§7-Ordner, ist aber ein LabyMod-Addon.\nBitte schließe Minecraft und lege die " + (jarName == null ? "dazugehörige Datei" : "§f" + jarName) + "§7 in den §fLabyMod/addons-1.8§7-Ordner.", 300)) {
				Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(line, this.width / 2f - 150, this.height / 2f - 30 + y, 0xFFFFFF);
				y += 10;
			}

			// Draw icon
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/icon.png"));
			drawTexture((double) this.width / 2 + 150 - 40, (double) this.height / 2 - 95, 60, 60);

			// Draw action buttons
			boolean mouseOver = mouseX > this.width / 2 - 150 && mouseX < this.width / 2 && mouseY > this.height / 2 + 40 && mouseY < this.height / 2 + 60;
			drawString(fontRendererObj, "§" + (mouseOver ? 'c' : '7') + "Ohne GrieferUtils spielen", this.width / 2 - 150, this.height / 2 + 46, 0xFFFFFF);
			super.drawScreen(mouseX, mouseY, partialTicks);
		}

		private String getJarName() {
			String jarPath = ForgeModWarning.class.getProtectionDomain().getCodeSource().getLocation().getFile();
			if (!jarPath.contains(".jar"))
				return null;

			jarPath = jarPath.substring(5, jarPath.lastIndexOf("!")); // remove protocol and class
			try {
				jarPath = URLDecoder.decode(jarPath, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return null;
			}

			return new File(jarPath).getName();
		}

		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			super.mouseClicked(mouseX, mouseY, mouseButton);
			if (mouseX > this.width / 2 - 150 && mouseX < this.width / 2 && mouseY > this.height / 2 + 40 && mouseY < this.height / 2 + 60)
				Minecraft.getMinecraft().displayGuiScreen(previousScreen);
		}

		private void drawTexture(double x, double y, double maxWidth, double maxHeight) {
			GL11.glPushMatrix();
			double sizeWidth = maxWidth / (double) 256;
			double sizeHeight = maxHeight / (double) 256;
			GL11.glScaled(sizeWidth, sizeHeight, 0.0);
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GlStateManager.color(1, 1, 1, 1);

			double left = x / sizeWidth;
			double top = y / sizeHeight;

			float scale = 1 / 256f;

			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			worldrenderer.pos(left, top + 256, this.zLevel).tex(0, 256 * scale).endVertex();
			worldrenderer.pos(left + 256, top + 256, this.zLevel).tex(256 * scale, 256 * scale).endVertex();
			worldrenderer.pos(left + 256, top, this.zLevel).tex(256d * scale, 0).endVertex();
			worldrenderer.pos(left, top, this.zLevel).tex(0, 0).endVertex();
			tessellator.draw();

			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();

			GL11.glPopMatrix();
		}

	}

}
