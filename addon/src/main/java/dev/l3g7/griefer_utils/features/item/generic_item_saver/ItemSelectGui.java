/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package dev.l3g7.griefer_utils.features.item.generic_item_saver;

import dev.l3g7.griefer_utils.util.misc.functions.Consumer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.io.IOException;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static org.lwjgl.input.Keyboard.KEY_ESCAPE;

public class ItemSelectGui extends GuiInventory {

	private final GuiScreen previousScreen = mc().currentScreen;
	private final Consumer<ItemStack> stackConsumer;

	/**
	 * Required because otherwise a GuiOpenEvent gets pushed, causing LabyMod to override it to a GuiInventoryCustom
	 */
	public static void open(Consumer<ItemStack> stackConsumer) {
		GuiScreen screen = new ItemSelectGui(stackConsumer);

		mc().setIngameNotInFocus();
		ScaledResolution scaledresolution = new ScaledResolution(mc());
		int i = scaledresolution.getScaledWidth();
		int j = scaledresolution.getScaledHeight();
		screen.setWorldAndResolution(mc(), i, j);
		mc().currentScreen = screen;
		mc().skipRenderWorld = false;
	}

	private ItemSelectGui(Consumer<ItemStack> stackConsumer) {
		super(player());
		this.stackConsumer = stackConsumer;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		for (Slot slot : inventorySlots.inventorySlots) {
			if (isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY)) {
				if (!slot.getHasStack())
					continue;

				stackConsumer.accept(slot.getStack());
				mc().displayGuiScreen(previousScreen);
			}
		}
	}

	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == KEY_ESCAPE || keyCode == mc.gameSettings.keyBindInventory.getKeyCode())
			mc().displayGuiScreen(previousScreen);
	}

}
