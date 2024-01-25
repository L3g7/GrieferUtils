/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info.freestuff;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.chat.BetterSwitchCommand;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.BigChestUtil;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.farms.Farm;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiBigChest;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiList;
import dev.l3g7.griefer_utils.misc.Citybuild;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.*;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class FreeStuff {

	public static final List<FreeStuff> FREE_STUFF = new ArrayList<>();

	public static FreeStuff fromJson(JsonObject object) {
		String name = object.get("name").getAsString();
		Citybuild cb = Citybuild.getCitybuild(object.get("cb").getAsString());
		String id = object.get("id").getAsString();
		String farm = object.get("farm").getAsString();
		farm = farm.isEmpty() ? null : farm.substring("/farm/".length());

		TreeMap<ItemFilter, String> items = new TreeMap<>(Comparator.comparing(iF -> iF.germanName));
		for (JsonElement entry : object.get("itemlist").getAsJsonArray()) {
			JsonObject item = entry.getAsJsonObject();
			ItemFilter filter = ItemFilter.TYPES_BY_ID.get(item.get("item").getAsString());
			if (filter == null)
				System.err.println("Missing item: " + item.get("item").getAsString());
			else
				items.put(filter, item.get("info").getAsString());

		}

		return new FreeStuff(name, cb, items, id, farm);
	}

	public final String name;
	public final Citybuild cb;
	public final TreeMap<ItemFilter, String> items;
	public final String id;
	public final String farm;

	private FreeStuff(String name, Citybuild cb, TreeMap<ItemFilter, String> items, String id, String farm) {
		this.name = name;
		this.cb = cb;
		this.items = items;
		this.id = id;
		this.farm = farm;
	}

	public boolean matchesFilter(String cb, ItemFilter itemFilter) {
		if (cb != null && !this.cb.matches(cb))
			return false;

		return itemFilter == null || items.containsKey(itemFilter);
	}

	public void addItemStack(GuiBigChest chest, int id, boolean isCbFiltered, boolean secondRow) {
		ItemStack stack = secondRow ? new ItemStack(Blocks.dirt, 1, 2) : new ItemStack(Blocks.grass);

		stack.setStackDisplayName("§6§n" + name);
		if (!isCbFiltered)
			stack.setStackDisplayName(String.format("§e[%s] %s", MinecraftUtil.getCitybuildAbbreviation(cb.getDisplayName()), stack.getDisplayName()));

		List<String> lines = items.keySet().stream().map(it -> "  §f" + it.germanName).collect(Collectors.toList());

		if (lines.size() > 10) {
			int extraLines = lines.size() - 10;
			while (lines.size() > 10)
				lines.remove(lines.size() - 1);
			lines.add("§8+" + extraLines + " Weitere");
		}

		NBTTagList itemsList = new NBTTagList();
		items.keySet().stream().limit(10).map(iF -> iF.stack.writeToNBT(new NBTTagCompound())).forEachOrdered(itemsList::appendTag);
		stack.getTagCompound().setTag("items_nbt", itemsList);

		lines.add("");
		lines.add("§7Linksklick: Teleportieren");
		if (farm != null)
			lines.add("§7Mittelklick: Zur Farm");
		lines.add("§7Rechtsklick: Mehr Infos");
		ItemUtil.setLore(stack, lines);

		chest.addItem(id, stack, () -> {
			BetterSwitchCommand.sendOnCitybuild("/p h " + name, cb);
			mc().displayGuiScreen(null);
		}, () -> openGui(chest), () -> {
			if (farm == null)
				return;

			for (Farm f : Farm.FARMS) {
				if (f.id.equals(farm)) {
					f.openGui(chest);
					return;
				}
			}

			throw new IllegalStateException("Freestuff entry " + this.id + " references missing farm " + farm);
		});
	}

	public void openGui(GuiBigChest previousGui) {
		GuiList gui = new GuiList("§8§lFreeStuff - " + name, 7, previousGui);
		for (Map.Entry<ItemFilter, String> entry : items.entrySet()) {
			ItemStack freeStuffStack = entry.getKey().stack.copy();
			if (!entry.getValue().isEmpty())
				ItemUtil.setLore(freeStuffStack, "§7" + entry.getValue());
			gui.addEntry(freeStuffStack, () -> {
				BetterSwitchCommand.sendOnCitybuild("/p h " + name, cb);
				mc().displayGuiScreen(null);
			});
		}
		gui.open();
	}

	@Override
	public String toString() {
		return ((char) Short.MAX_VALUE - items.size()) + " " + name + "[" + BigChestUtil.toAbbreviation(cb) + "]" + id;
	}

}
