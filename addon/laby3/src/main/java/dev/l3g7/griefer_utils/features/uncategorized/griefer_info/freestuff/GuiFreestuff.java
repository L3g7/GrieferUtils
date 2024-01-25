/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info.freestuff;

import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.BigChestUtil;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiBigChest;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiGrieferInfo;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiSearchable;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

public class GuiFreestuff extends GuiSearchable<FreeStuff> {

	private final GuiBigChest CB_SELECT_GUI = new GuiBigChest("§8§lFreestuff - Citybuild", 7, this);
	private final GuiFilterItem ITEM_SELECT_GUI = new GuiFilterItem(this);

	ItemFilter itemFilter = null;
	private String cbFilter = null;
	private List<FreeStuff> allCurrentFreeStuffs = new ArrayList<>();

	public GuiFreestuff() {
		super("§8§lFreestuff", 7, GuiGrieferInfo.GUI, "Suchen...", Comparator.comparing(FreeStuff::toString));

		for (Map.Entry<Integer, ItemStack> cb : BigChestUtil.getCBs().entrySet()) {
			String asFilter = cb.getValue().getDisplayName().replaceAll("§.", "");

			CB_SELECT_GUI.addItem(cb.getKey(), cb.getValue(), () -> {
				cbFilter = asFilter;
				this.open();
			});
		}
	}

	@Override
	public void open() {
		super.open();
		addFilters(this);

		addFilters(ITEM_SELECT_GUI);
		ItemStack removeItemFilter = ItemUtil.createItem(new ItemStack(Blocks.barrier), itemFilter != null, "§fKein Item auswählen");
		if (itemFilter != null)
			ItemUtil.setLore(removeItemFilter, "§7Derzeitiges Item: §6§n" + itemFilter.germanName);

		ITEM_SELECT_GUI.addItem(28, removeItemFilter, () -> {
			itemFilter = null;
			open();
		});

		// Init cb selection
		addFilters(CB_SELECT_GUI);
		ItemStack removeCBFilter = ItemUtil.createItem(new ItemStack(Blocks.barrier), cbFilter != null, "§fKeinen Citybuild auswählen");
		if (cbFilter != null)
			ItemUtil.setLore(removeCBFilter, "§7Derzeitiger Citybuild: §6§n" + cbFilter);

		CB_SELECT_GUI.addItem(10, removeCBFilter, () -> {
			cbFilter = null;
			open();
		});
	}

	public void addFilters(GuiBigChest chest) {
		ItemStack cbStack = ItemUtil.createItem(Items.dark_oak_door, 0, "§fNach Citybuild filtern");
		if (cbFilter != null)
			for (ItemStack value : BigChestUtil.getCBs().values())
				if (cbFilter.equals(value.getDisplayName().replaceAll("§.", "")))
					cbStack = ItemUtil.createItem(value.copy(), true, "§fNach Citybuild filtern");

		ItemStack itemStack = itemFilter == null ? ItemUtil.createItem(Items.gold_ingot, 0, "§fNach Item filtern") : ItemUtil.createItem(itemFilter.stack.copy(), true, "§fNach Item filtern");

		if (cbFilter != null)
			ItemUtil.setLore(cbStack, "§7Derzeitiger Citybuild: §6§n" + cbFilter);

		if (itemFilter != null)
			ItemUtil.setLore(itemStack, "§7Derzeitiges Item: §6§n" + itemFilter.germanName);

		chest.addItem(10, cbStack, CB_SELECT_GUI::open);
		chest.addItem(28, itemStack, ITEM_SELECT_GUI::open);
	}

	@Override
	protected void renderToolTip(ItemStack stack, int x, int y) {
		super.renderToolTip(stack, x, y);
		if (stack.getItem() != Item.getItemFromBlock(Blocks.grass) && stack.getItem() != Item.getItemFromBlock(Blocks.dirt))
			return;

		List<String> list = stack.getTooltip(player(), false);
		if (list.isEmpty())
			return;

		List<ItemStack> items = new ArrayList<>();
		NBTTagList itemsNbt = stack.getTagCompound().getTagList("items_nbt", 10);
		for (int i = 0; i < itemsNbt.tagCount(); i++)
			items.add(ItemStack.loadItemStackFromNBT(itemsNbt.getCompoundTagAt(i)));

		// Render
		Pair<Integer, Integer> tooltipTranslation = RenderUtil.getTooltipTranslation(list, x, y, width, height);
		int tooltipY = tooltipTranslation.getRight() + 12;

		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.scale(0.5, 0.5, 1);
		GlStateManager.translate(0, 0, 500);
		for (ItemStack itemStack : items) {
			drawUtils().renderItemIntoGUI(itemStack, tooltipTranslation.getLeft() * 2 - 3, tooltipY * 2 - 1);
			tooltipY += 10;
		}
		GlStateManager.popMatrix();
	}

	@Override
	protected int fillEntriesOnEmptySearch(TreeSet<FreeStuff> entries) {
		for (FreeStuff freeStuff : new ArrayList<>(FreeStuff.FREE_STUFF))
			if (freeStuff.matchesFilter(cbFilter, itemFilter))
				entries.add(freeStuff);

		allCurrentFreeStuffs = new ArrayList<>(entries);
		updatePage();
		return allCurrentFreeStuffs.size();
	}

	@Override
	protected Iterable<FreeStuff> getAllEntries() {
		return allCurrentFreeStuffs;
	}

	@Override
	protected void addItem(FreeStuff entry, int slot) {
		entry.addItemStack(this, slot, cbFilter != null, scrollStartRow % 2 == slot / 9 % 2);
	}

	@Override
	protected boolean matchesSearch(FreeStuff entry, String search) {
		return entry.name.toLowerCase().contains(search);
	}
}
