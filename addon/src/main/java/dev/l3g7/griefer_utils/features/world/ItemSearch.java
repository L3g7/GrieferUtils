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
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.event.events.render.DrawGuiContainerForegroundLayerEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.gui.elements.ModTextField;
import net.labymod.utils.Material;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class ItemSearch extends Feature {

	/**
	 * An invisible marker to indicate guis where ItemSearch should be disabled
	 */
	public static final String marker = "§4§0§2§7§9§c§d§a§d§e§f§e§l§m§n§r";

	private final BooleanSetting dispenser = new BooleanSetting()
		.name("Spender / Werfer")
		.description("Ob die Item-Suche auch bei Spendern / Werfern hinzugefügt werden soll")
		.icon(Material.DISPENSER);

	private final BooleanSetting hopper = new BooleanSetting()
		.name("Tricher")
		.description("Ob die Item-Suche auch bei Trichtern hinzugefügt werden soll")
		.icon(Material.HOPPER);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Item-Suche")
		.description("Fügt eine Item-Suche innerhalb von Kisten hinzu.")
		.icon("chest")
		.subSettings(dispenser, hopper);

	public ModTextField searchField = null;
	private String previousSearch = "";

	@EventListener(triggerWhenDisabled = true)
	public void onGuiInit(GuiScreenEvent.InitGuiEvent event) {
		if (searchField != null)
			previousSearch = searchField.getText();

		searchField = null;
		if (!(event.gui instanceof GuiChest ||
			(dispenser.get() && event.gui instanceof GuiDispenser) ||
			(hopper.get() && event.gui instanceof GuiHopper)))
			return;

		if (event.gui instanceof GuiChest) {
			IInventory lowerChestInventory = Reflection.get(event.gui, "lowerChestInventory");
			String title = lowerChestInventory.getDisplayName().getFormattedText();
			if (title.startsWith(marker) || title.startsWith("§6Profil") || title.startsWith("§6Lottoschein "))
				return;
		}

		searchField = new ModTextField(0, mc().fontRendererObj, 82, 6, 83, mc().fontRendererObj.FONT_HEIGHT);
		searchField.setPlaceHolder("§oSuche...");
		searchField.setTextColor(0xffffff);
		searchField.setText(previousSearch);
		searchField.setEnableBackgroundDrawing(false);
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
			int guiLeft = Reflection.get(event.gui, "guiLeft");
			int guiTop = Reflection.get(event.gui, "guiTop");

			int scale = new ScaledResolution(mc()).getScaleFactor();
			int x = Mouse.getEventX() / scale;
			int y = (mc().displayHeight - Mouse.getEventY()) / scale;

			x -= guiLeft;
			y -= guiTop;

			searchField.mouseClicked(x, y, Mouse.getEventButton());
		}
	}

	@EventListener
	public void onScreenDraw(DrawGuiContainerForegroundLayerEvent event) {
		if (searchField == null)
			return;

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.translate(-1, -1, 300);

		// Draw search background
		mc().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png"));
		event.container.drawTexturedModalRect(80, 4, 80, 4, 90, 12);

		searchField.drawTextBox();

		// Draw search
		String text = searchField.getText().toLowerCase();

		// the maximum slot id before it is in the player's inventory
		if (!text.isEmpty()) {
			for (Slot slot : event.container.inventorySlots.inventorySlots) {
				if (slot.inventory == player().inventory)
					break;

				if (shouldHide(slot.getStack(), text))
					GuiScreen.drawRect(slot.xDisplayPosition, slot.yDisplayPosition, slot.xDisplayPosition + 18, slot.yDisplayPosition + 18, 0xAA000000);
			}
		}

		GlStateManager.translate(1, 1, -300);
	}

	private boolean shouldHide(ItemStack stack, String text) {
		if (stack == null)
			return true;

		if (stack.getDisplayName().toLowerCase().contains(text))
			return false;

		return !stack.getItem().getItemStackDisplayName(stack).toLowerCase().contains(text);
	}

}