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

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

public class GuiBigChest extends GuiContainer {

	protected static final ItemStack BACK_BUTTON = ItemUtil.fromNBT("{id:\"minecraft:skull\",Count:1b,tag:{display:{Name:\"§f" + "Zurück" + "\"},SkullOwner:{Id:\"00000000-0000-0000-0000-000000000000\",Properties:{textures:[0:{Value:\"" + "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y3YWFjYWQxOTNlMjIyNjk3MWVkOTUzMDJkYmE0MzM0MzhiZTQ2NDRmYmFiNWViZjgxODA1NDA2MTY2N2ZiZTIifX19" + "\"}]}}},Damage:3s}");
	private static final ItemStack FILLER = ItemUtil.createItem(Blocks.stained_glass_pane, 7, null);

	private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	private final List<Runnable> leftClickEvents = new ArrayList<>();
	private final List<Runnable> rightClickEvents = new ArrayList<>();
	private final List<Runnable> midClickEvents = new ArrayList<>();
	private final List<TextureItem> textureItems = new ArrayList<>();
	private final IInventory inventory;
	protected final int rows;

	public GuiBigChest(String title, int rows) {
		this(new InventoryBasic(title, true, rows * 9) {
			public int getInventoryStackLimit() {
				return Integer.MAX_VALUE;
			}
		});
	}

	public GuiBigChest(String title, int rows, GuiBigChest previousGui) {
		this(title, rows);
		addItem((rows - 1) * 9, BACK_BUTTON, previousGui::open);
	}

	private GuiBigChest(IInventory inv) {
		super(new ContainerBigChest(inv));

		inventory = inv;
		allowUserInput = false;
		rows = inv.getSizeInventory() / 9;
		ySize = 24 + rows * 18;
		for (int i = 0; i < rows * 9; i++) {
			leftClickEvents.add(null);
			rightClickEvents.add(null);
			midClickEvents.add(null);
			textureItems.add(null);
		}
	}

	/**
	 * Opens the gui.
	 */
	public void open() {
		mc().displayGuiScreen(this);
	}

	/**
	 * Adds an item to the inventory and registers a callback when clicking it.
	 */
	public void addItem(int slot, ItemStack stack, Runnable onClick) {
		addItem(slot, stack, onClick, onClick, onClick);
	}

	public void addItem(int slot, ItemStack stack, Runnable onLeftClick, Runnable onRightClick, Runnable onMidClick) {
		inventory.setInventorySlotContents(slot, stack);
		textureItems.set(slot, null);

		leftClickEvents.set(slot, onLeftClick);
		rightClickEvents.set(slot, onRightClick);
		midClickEvents.set(slot, onMidClick);
	}

	/**
	 * Adds a TextureItem to the inventory and registers a callback when clicking it.
	 */
	public void addTextureItem(int slot, TextureItem textureItem, Runnable onClick) {
		addTextureItem(slot, textureItem, onClick, onClick);
	}

	public void addTextureItem(int slot, TextureItem textureItem, Runnable onLeftClick, Runnable onRightClick) {
		inventory.setInventorySlotContents(slot, null);
		textureItems.set(slot, textureItem);

		leftClickEvents.set(slot, onLeftClick);
		rightClickEvents.set(slot, onRightClick);
		midClickEvents.set(slot, onRightClick);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (getSlotUnderMouse() == null
			|| getSlotUnderMouse().inventory != inventory)
			return;

		if (mouseButton > 2)
			return;

		List<Runnable> runnables;
		if (mouseButton == 0)
			runnables = leftClickEvents;
		else if (mouseButton == 1)
			runnables = rightClickEvents;
		else
			runnables = midClickEvents;

		Runnable clickEvent = runnables.get(getSlotUnderMouse().getSlotIndex());
		if (clickEvent != null)
			clickEvent.run();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1 || keyCode == settings().keyBindInventory.getKeyCode())
			super.keyTyped(typedChar, keyCode);
	}

	/**
	 * Draws the title and the tooltips of texture-items
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(inventory.getDisplayName().getUnformattedText(), 8, 6, 0x404040);
		Slot slot = getSlotUnderMouse();
		if (slot == null)
			return;

		TextureItem textureItem = textureItems.get(slot.getSlotIndex());
		if (textureItem == null)
			return;

		renderToolTip(textureItem.toolTipStack, mouseX - guiLeft, mouseY - guiTop);
		GlStateManager.disableLighting();
	}

	@Override
	protected void renderToolTip(ItemStack stack, int x, int y) {
		boolean advancedTooltips = settings().advancedItemTooltips;
		settings().advancedItemTooltips = false;

		super.renderToolTip(stack, x, y);
		settings().advancedItemTooltips = advancedTooltips;
	}

	/**
	 * Draws the background and the texture-items
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;

		GlStateManager.color(1, 1, 1);
		drawTexturedModalRect(x, y, 0, 0, xSize, 17);
		drawTexturedModalRect(x, y + rows * 18 + 17, 0, 215, xSize, 7);
		y--;
		for (int i = 1; i <= rows; i++)
			drawTexturedModalRect(x, y + i * 18, 0, 17, xSize, 18);

		// Has to be in a separate loop, otherwise some wierd stuff is rendered
		for (int i = 1; i <= rows; i++) {
			for (int dX = 0; dX < 9; dX++) {
				Slot slot = inventorySlots.getSlot((i - 1) * 9 + dX);
				if (!slot.getHasStack() || slot.getStack().getItem() != FILLER.getItem())
					drawUtils().drawItem(FILLER, x + dX * 18 + 8, y + i * 18 + 1, null);
			}
		}

		for (int i = 0; i < textureItems.size(); i++) {
			TextureItem textureItem = textureItems.get(i);
			if (textureItem == null)
				continue;

			int dX = (i % 9) * 18 + 8;
			int dY = (i / 9) * 18 + 19;

			drawUtils().bindTexture(textureItem.texture);
			double dSize = (16 - textureItem.renderSize) / 2d;

			drawUtils().drawTexture(x + dX + dSize, y + dY + dSize, 0, 0, 256, 256, textureItem.renderSize, textureItem.renderSize);
			mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, textureItem.toolTipStack, x + dX, y + dY);
			GlStateManager.disableLighting();
		}

	}

	@EventListener
	private static void onPacketSend(PacketSendEvent<Packet<?>> event) {
		if ((event.packet instanceof C0EPacketClickWindow
			|| event.packet instanceof C0DPacketCloseWindow)
			&& mc().currentScreen instanceof GuiBigChest)
			event.cancel();
	}

	private static class ContainerBigChest extends Container {

		public ContainerBigChest(IInventory inventory) {
			for (int j = 0; j < inventory.getSizeInventory() / 9; ++j)
				for (int k = 0; k < 9; ++k)
					this.addSlotToContainer(new Slot(inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
		}

		@Override
		public boolean canInteractWith(EntityPlayer playerIn) {
			return false;
		}

	}

	/**
	 * Used for what to the user looks like items with custom textures
	 */
	public static class TextureItem {
		private final String texture;
		private final int renderSize;
		private final ItemStack toolTipStack;

		public TextureItem(String texture, String title, String... lore) {
			this(texture, 16, ItemUtil.setLore(ItemUtil.createItem(Blocks.command_block, 0, title), lore));
		}

		public TextureItem(String texture, int renderSize, ItemStack toolTipStack) {
			this.texture = "griefer_utils/icons/" + texture + ".png";
			this.renderSize = renderSize;
			this.toolTipStack = toolTipStack;
		}
	}

}