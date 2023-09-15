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

package dev.l3g7.griefer_utils.features.item;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.event.events.MouseClickEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.lwjgl.input.Mouse;

import java.io.IOException;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static org.lwjgl.input.Keyboard.KEY_ESCAPE;

/**
 * Suppresses left-clicks on books and opens a preview when right-clicking.
 */
@Singleton
public class BookFix extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Bücher fixen")
		.description("Unterbindet Linksklick auf Bücher und öffnet eine Vorschau bei Rechtsklick.")
		.icon(Material.BOOK);

	/**
	 * Fixes direct book interactions.
	 */
	@EventListener
	public void onMouse(MouseClickEvent.RightClickEvent event) {
		if (player() == null || mc().currentScreen != null)
			return;

		if (processClick(player().getHeldItem(), true))
			event.cancel();
	}

	/**
	 * Fixes book interactions in guis.
	 */
	@EventListener
	public void onMouseGui(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (!Mouse.getEventButtonState() || !(mc().currentScreen instanceof GuiContainer))
			return;

		Slot slot = MinecraftUtil.getSlotUnderMouse(mc().currentScreen);
		if (slot != null && processClick(slot.getStack(), Mouse.getEventButton() == 1))
			event.cancel();
	}

	/**
	 * @return {@code true} if the click should be canceled.
	 */
	private boolean processClick(ItemStack item, boolean openBook) {
		if (!ServerCheck.isOnGrieferGames())
			return false;

		if (item == null || (item.getItem() != Items.writable_book && item.getItem() != Items.written_book))
			return false;

		if (openBook) {
			// Fix missing tags
			NBTTagCompound tag = item.getTagCompound();
			if (tag == null)
				tag = new NBTTagCompound();

			if (!tag.hasKey("title"))
				tag.setString("title", "A book");
			if (!tag.hasKey("author"))
				tag.setString("author", "Me");
			if (!tag.hasKey("pages"))
				tag.setTag("pages", new NBTTagList());

			// Open preview
			mc().displayGuiScreen(new GuiBookPreview(player(), item));
		}

		return true;
	}

	/**
	 * A {@link GuiScreenBook} opening the last screen when closed.
	 */
	private static class GuiBookPreview extends GuiScreenBook {

		private final GuiScreen previousScreen = Minecraft.getMinecraft().currentScreen;

		private GuiBookPreview(EntityPlayer player, ItemStack book) {
			super(player, book, false);
		}

		public void keyTyped(char typedChar, int keyCode) {
			if (keyCode == KEY_ESCAPE)
				Minecraft.getMinecraft().displayGuiScreen(previousScreen);
		}

		public void actionPerformed(GuiButton button) throws IOException {
			if (button.enabled && button.id == 0)
				Minecraft.getMinecraft().displayGuiScreen(previousScreen);
			else
				super.actionPerformed(button);
		}

	}

}
