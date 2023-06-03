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

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.render.DrawGuiContainerForegroundLayerEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.gui.elements.ModTextField;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class ChestSearch extends Feature {

	/**
	 * An invisible marker to indicate guis where ChestSearch should be disabled
	 */
	public static final String marker = "§4§0§2§7§9§c§d§a§d§e§f§e§l§m§n§r";

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Kisten-Suche")
		.description("Fügt eine Item-Suche innerhalb von Kisten hinzu.")
		.icon("chest");

	private ModTextField searchField = null;
	private String previousSearch = "";

	@EventListener(triggerWhenDisabled = true)
	public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
		if (searchField != null)
			previousSearch = searchField.getText();

		searchField = null;
		if (event.gui instanceof GuiChest) {
			int guiLeft = Reflection.get(event.gui, "guiLeft");
			int guiTop = Reflection.get(event.gui, "guiTop");

			IInventory lowerChestInventory = Reflection.get(event.gui, "lowerChestInventory");
			String title = lowerChestInventory.getDisplayName().getFormattedText();
			if (title.startsWith(marker) || title.startsWith("§6Profil") || title.equals("§6Lotterie§r"))
				return;

			searchField = new ModTextField(0, mc().fontRendererObj, guiLeft + 82, guiTop + 6, 83, mc().fontRendererObj.FONT_HEIGHT);
			searchField.setPlaceHolder("§oSuche...");
			searchField.setTextColor(0xffffff);
			searchField.setText(previousSearch);
			searchField.setEnableBackgroundDrawing(false);
		}
	}

	@EventListener
	public void onKeyPress(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (searchField != null && Keyboard.getEventKeyState()) {
			if (searchField.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey())) {
				// Suppress inventory closing when keyBindInventory is pressed
				if (Keyboard.getEventKey() == mc().gameSettings.keyBindInventory.getKeyCode())
					event.setCanceled(true);
			}
		}
	}

	@EventListener
	public void onMousePress(GuiScreenEvent.MouseInputEvent.Post event) {
		if (searchField != null && Mouse.getEventButton() != -1) {
			int x = Mouse.getEventX() * event.gui.width / mc().displayWidth;
			int y = event.gui.height - Mouse.getEventY() * event.gui.height / mc().displayHeight - 1;

			searchField.mouseClicked(x, y, Mouse.getEventButton());
		}
	}

	@EventListener
	public void onScreenDraw(DrawGuiContainerForegroundLayerEvent event) {
		if (searchField == null)
			return;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		int guiLeft = Reflection.get(event.chest, "guiLeft");
		int guiTop = Reflection.get(event.chest, "guiTop");
		GlStateManager.translate(-guiLeft, -guiTop, 300);

		// Draw search background
		mc().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png"));
		event.chest.drawTexturedModalRect(guiLeft + 80, guiTop + 4, 80, 4, 90, 12);

		searchField.drawTextBox();

		// Draw search
		String text = searchField.getText().toLowerCase();
		if (!text.isEmpty()) {
			List<ItemStack> items = event.chest.inventorySlots.getInventory();
			int x = guiLeft + 7;
			int y = guiTop + 17;
			for (int i = 0; i < items.size() - 36; i++) {
				int sX = x + (18 * (i % 9));
				int sY = y + (18 * (i / 9));

				if (shouldHide(items.get(i), text))
					GuiScreen.drawRect(sX, sY, sX + 18, sY + 18, 0xAA000000);
			}
		}

		GlStateManager.translate(guiLeft, guiTop, -300);
	}

	private boolean shouldHide(ItemStack stack, String text) {
		if (stack == null)
			return true;

		if (stack.getDisplayName().toLowerCase().contains(text))
			return false;

		return !stack.getItem().getItemStackDisplayName(stack).toLowerCase().contains(text);
	}

}