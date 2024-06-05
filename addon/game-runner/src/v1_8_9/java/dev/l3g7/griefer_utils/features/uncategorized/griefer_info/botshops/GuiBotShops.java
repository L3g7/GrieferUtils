/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.botshops;

import dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.BigChestUtil;
import dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.gui.GuiBigChest;
import dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.gui.GuiGrieferInfo;
import dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.gui.GuiSearchable;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.*;

public class GuiBotShops extends GuiSearchable<BotShop> {

	private boolean filteringForSelling = true;
	private boolean filteringForBuying = true;
	private boolean hasClicked = false;
	private String cbFilter = null;

	private List<BotShop> allCurrentBotShops = new ArrayList<>();

	private final GuiBigChest CB_SELECT_GUI = new GuiBigChest("§8§lBotshops - Citybuild", 7, this);

	public GuiBotShops() {
		super("§8§lBotshops", 7, GuiGrieferInfo.GUI, "Botshop suchen...", Comparator.comparing(BotShop::toString));

		for (Map.Entry<Integer, ItemStack> cb : BigChestUtil.getCBs().entrySet()) {
			String asFilter = cb.getValue().getDisplayName().replaceAll("§.", "");

			CB_SELECT_GUI.addItem(cb.getKey(), cb.getValue(), () -> {
				cbFilter = asFilter;
				this.open();
			});
		}
	}

	@Override
	protected int fillEntriesOnEmptySearch(TreeSet<BotShop> botShops) {
		for (BotShop botShop : new ArrayList<>(BotShop.BOT_SHOPS))
			if (botShop.matchesFilter(cbFilter, filteringForBuying, filteringForSelling))
				botShops.add(botShop);

		allCurrentBotShops = new ArrayList<>(botShops);
		updatePage();
		return allCurrentBotShops.size();
	}

	@Override
	protected Iterable<BotShop> getAllEntries() {
		return allCurrentBotShops;
	}

	@Override
	protected void addItem(BotShop entry, int slot) {
		entry.addItemStack(this, slot, cbFilter != null);
	}

	@Override
	protected boolean matchesSearch(BotShop entry, String search) {
		return entry.name.toLowerCase().contains(search);
	}

	@Override
	public void initGui() {
		super.initGui();

		addFilters(this);

		addFilters(CB_SELECT_GUI);
		ItemStack removeCBFilter = ItemUtil.createItem(new ItemStack(Blocks.barrier), cbFilter != null, "§fKeinen Citybuild auswählen");
		if (cbFilter != null)
			ItemUtil.setLore(removeCBFilter, "§7Derzeitiger Citybuild: §6§n" + cbFilter);

		CB_SELECT_GUI.addItem(10, removeCBFilter, () -> {
			cbFilter = null;
			open();
		});
	}

	private void addFilters(GuiBigChest chest) {
		ItemStack cbStack = ItemUtil.createItem(Items.dark_oak_door, 0, "§fNach Citybuild filtern");
		if (cbFilter != null) {
			for (ItemStack value : BigChestUtil.getCBs().values())
				if (cbFilter.equals(value.getDisplayName().replaceAll("§.", "")))
					cbStack = ItemUtil.createItem(value.copy(), true, "§fNach Citybuild filtern");

			ItemUtil.setLore(cbStack, "§7Derzeitiger Citybuild: §6§n" + cbFilter);
		}


		TextureItem textureItem = new TextureItem("wallets/ingoing" + (filteringForBuying ? "" : "_gray"), (filteringForBuying ? "§f" : "§7") + "Ankauf", "§8Klicke, um Umzuschalten");
		chest.addTextureItem(28, textureItem, () -> {
			filteringForBuying = !filteringForBuying;
			if (!hasClicked) {
				filteringForBuying = hasClicked = true;
				filteringForSelling = false;
			}

			if (!filteringForBuying)
				filteringForSelling = true;

			open();
		});

		textureItem = new TextureItem("wallets/outgoing" + (filteringForSelling ? "" : "_gray"), (filteringForSelling ? "§f" : "§7") + "Verkauf", "§8Klicke, um Umzuschalten");
		chest.addTextureItem(37, textureItem, () -> {
			filteringForSelling = !filteringForSelling;
			if (!hasClicked) {
				filteringForSelling = hasClicked = true;
				filteringForBuying = false;
			}

			if (!filteringForSelling)
				filteringForBuying = true;

			open();
		});

		chest.addItem(10, cbStack, CB_SELECT_GUI::open);
	}

}
