/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.BigChestUtil.toSlotId;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

public class GuiList extends GuiBigChest {

	private static final ResourceLocation SEARCH_TAB_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tab_items.png");
	private static final ResourceLocation SCROLLBAR_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

	private final List<Entry> entries = new ArrayList<>();
	private float currentScroll = 0;
	private boolean wasClicking = false;
	private boolean isScrolling = false;

	public GuiList(String title, int rows, GuiBigChest previousGui) {
		super(title, rows, previousGui);
	}

	public void addEntry(ItemStack stack, Runnable onClick) {
		entries.add(new Entry(null, stack, onClick));
		onScroll(currentScroll);
	}

	public void addEntry(TextureItem textureItem, Runnable onClick) {
		entries.add(new Entry(textureItem, null, onClick));
		onScroll(currentScroll);
	}

	private boolean isScrollbarRequired() {
		return entries.size() > 7 * (rows - 2);
	}

	private void onScroll(float scroll) {
		int i = (entries.size() + 7 - 1) / 7 - (rows - 2);
		int startRow = (int) (scroll * i + 0.5);

		if (startRow < 0)
			startRow = 0;

		for (int y = 0; y < rows - 2; ++y) {
			for (int x = 0; x < 7; ++x) {
				int index = x + (y + startRow) * 7;
				int slotId = x + y * 7;

				if (index < 0 || index >= entries.size()) {
					addItem(toSlotId(slotId, true), null, null);
					continue;
				}

				Entry entry = entries.get(index);
				if (entry.textureItem != null)
					addTextureItem(toSlotId(slotId, true), entry.textureItem, entry.onClick);
				else
					addItem(toSlotId(slotId, true), entry.stack, entry.onClick);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		boolean isLeftClicking = Mouse.isButtonDown(0);
		int left = guiLeft + 175;
		int top = guiTop + 18;
		int right = left + 14;
		int bottom = top + 112;

		if (!wasClicking && isLeftClicking && mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom)
			isScrolling = isScrollbarRequired();

		if (!isLeftClicking)
			isScrolling = false;

		wasClicking = isLeftClicking;

		if (isScrolling) {
			currentScroll = ((mouseY - top) - 7.5f) / ((bottom - top) - 15f);
			currentScroll = MathHelper.clamp_float(currentScroll, 0, 1);
			onScroll(currentScroll);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		mc().getTextureManager().bindTexture(SEARCH_TAB_TEXTURE);
		drawTexturedModalRect(guiLeft + 169, guiTop, 170, 0, 25, 35);
		drawTexturedModalRect(guiLeft + 169, guiTop + ySize - 25, 170, 111, 25, 25);

		for (int i = 0; i < rows - 2; i++)
			drawTexturedModalRect(guiLeft + 169, guiTop + 35 + i * 18, 170, 35, 25, 18);

		int sbStart = guiTop + 18;
		int sbEnd = guiTop + ySize - 6;
		mc.getTextureManager().bindTexture(SCROLLBAR_TEXTURE);
		drawTexturedModalRect(guiLeft + 174, sbStart + ((sbEnd - sbStart - 17) * currentScroll), isScrollbarRequired() ? 232 : 244, 0, 12, 15);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		int dWheel = Mouse.getEventDWheel();

		if (dWheel == 0 || !isScrollbarRequired())
			return;

		float invisibleRows = entries.size() / 7f - (rows - 2);

		if (dWheel > 0)
			dWheel = 1;

		if (dWheel < 0)
			dWheel = -1;

		currentScroll = currentScroll - dWheel / invisibleRows;
		currentScroll = MathHelper.clamp_float(currentScroll, 0.0F, 1.0F);
		onScroll(currentScroll);
	}

	private static class Entry {

		private final TextureItem textureItem;
		private final ItemStack stack;
		private final Runnable onClick;

		private Entry(TextureItem textureItem, ItemStack stack, Runnable onClick) {
			this.textureItem = textureItem;
			this.stack = stack;
			this.onClick = onClick;
		}

	}

}
