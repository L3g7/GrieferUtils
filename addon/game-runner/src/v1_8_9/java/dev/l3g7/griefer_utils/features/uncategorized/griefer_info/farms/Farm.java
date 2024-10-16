/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info.farms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.chat.BetterSwitchCommand;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.freestuff.FreeStuff;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiBigChest;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiBigChest.TextureItem;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiList;
import net.labymod.api.Laby;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class Farm {

	public static final List<Farm> FARMS = new ArrayList<>();

	public static Farm fromJson(JsonObject object) {
		String name = object.get("name").getAsString();
		Citybuild cb = Citybuild.getCitybuild(object.get("cb").getAsString());
		String freeStuff = object.get("freestuff").getAsString();
		freeStuff = freeStuff.isEmpty() ? null : freeStuff.substring("/freestuff/view?id=".length());

		String id = object.get("id").getAsString();

		TreeSet<Spawner> spawners = new TreeSet<>(Comparator.comparing(Spawner::toString));
		spawnerLoop:
		for (JsonElement element : object.get("spawnerlist").getAsJsonArray()) {
			Spawner spawner = Spawner.fromJson(element.getAsJsonObject());
			for (Spawner s : spawners) {
				if (s.type == spawner.type && s.active == spawner.active && s.plot.equals(spawner.plot)) {
					s.amount += spawner.amount;
					continue spawnerLoop;
				}
			}
			spawners.add(spawner);
		}

		return new Farm(name, cb, freeStuff, id, spawners);
	}

	public final String name;
	public final Citybuild cb;
	public final String freeStuff;
	public final String id;
	public final int spawnerCount;
	public final TreeSet<Spawner> spawner;
	public final TreeMap<SpawnerType, Integer> spawnerTypes;

	private Farm(String name, Citybuild cb, String freeStuff, String id, TreeSet<Spawner> spawner) {
		this.name = name;
		this.cb = cb;
		this.freeStuff = freeStuff;
		this.id = id;
		this.spawner = spawner;
		this.spawnerCount = spawner.stream().mapToInt(s -> s.amount).sum();
		this.spawnerTypes = new TreeMap<>(Comparator.comparing(t -> t.germanName));
		for (Spawner s : spawner)
			spawnerTypes.put(s.type, spawnerTypes.getOrDefault(s.type, 0) + s.amount);
	}

	public boolean matchesFilter(String cb, SpawnerType type, boolean showActive, boolean showPassive) {
		if (cb != null && !this.cb.matches(cb))
			return false;

		if (type != null && spawner.stream().noneMatch(s -> s.type == type && (s.active ? showActive : showPassive)))
			return false;

		return (showActive && spawner.stream().anyMatch(s -> s.active))
			|| (showPassive && spawner.stream().anyMatch(s -> !s.active));
	}

	public void addItemStack(GuiBigChest chest, int id, SpawnerType type, boolean isCbFiltered, boolean secondRow) {
		chest.addItem(id, toStack(type, isCbFiltered, secondRow), () -> {
			BetterSwitchCommand.sendOnCitybuild("/p h " + name, cb);
			mc().displayGuiScreen(null);
		}, () -> openGui(chest), () -> {
			if (freeStuff == null)
				return;

			for (FreeStuff stuff : FreeStuff.FREE_STUFF) {
				if (stuff.id.equals(freeStuff)) {
					stuff.openGui(chest);
					return;
				}
			}

			throw new IllegalStateException("Farm " + this.id + " references missing freestuff entry " + freeStuff);
		});
	}

	public void openGui(GuiBigChest previousGui) {
		GuiList gui = new GuiList("§8§lFarmen - " + name, 7, previousGui);
		for (Spawner s : spawner) {
			Runnable onClick = () -> {
				BetterSwitchCommand.sendOnCitybuild("/p h " + (s.plot == null ? name : s.plot), cb);
				mc().displayGuiScreen(null);
			};

			if (s.type.isCobblestone()) {
				gui.addEntry(s.toStack(name), onClick);
				continue;
			}

			gui.addEntry(new TextureItem(s.type.texture, 12, s.toStack(name)), onClick);
		}
		gui.open();
	}

	private ItemStack toStack(SpawnerType typeFilter, boolean isCbFiltered, boolean secondRow) {
		ItemStack stack = secondRow ? new ItemStack(Blocks.dirt, 1, 2) : new ItemStack(Blocks.grass);

		stack.setStackDisplayName("§6§n" + name);
		if (!isCbFiltered)
			stack.setStackDisplayName(String.format("§e[%s] %s", MinecraftUtil.getCitybuildAbbreviation(cb.getName()), stack.getDisplayName()));

		TreeMap<SpawnerType, AtomicInteger> spawner = new TreeMap<>(Comparator.comparing(s -> s.germanName));
		NBTTagList textureList = new NBTTagList();
		List<String> lines = new ArrayList<>();

		for (Spawner spwnr : this.spawner)
			spawner.computeIfAbsent(spwnr.type, t -> new AtomicInteger()).getAndAdd(spwnr.amount);

		boolean fancy = LABY_4.isActive() && Laby.labyAPI().themeService().currentTheme().getId().equals("fancy");
		String format = (fancy ? "  " : "") + "  §f%s §8(%dx)";

		if (typeFilter != null && spawner.containsKey(typeFilter)) {
			int amount = spawner.get(typeFilter).get();
			spawner.remove(typeFilter);

			lines.add(String.format(format, typeFilter.germanName, amount));
			textureList.appendTag(new NBTTagString(typeFilter.texture));
		}

		lines.addAll(spawner.entrySet().stream().map(e -> String.format(format, e.getKey().germanName, e.getValue().get())).collect(Collectors.toList()));
		if (lines.size() > 10) {
			int extraLines = lines.size() - 10;
			while (lines.size() > 10)
				lines.remove(lines.size() - 1);

			lines.add("§8+" + extraLines + " Weitere");
		}

		spawner.keySet().stream().limit(typeFilter == null ? 10 : 9).map(s -> s.texture).forEach(t -> textureList.appendTag(new NBTTagString(t)));
		stack.getTagCompound().setTag("textures_nbt", textureList);

		lines.add("");
		lines.add("§7Linksklick: Teleportieren");
		if (freeStuff != null)
			lines.add("§7Mittelklick: Zum Freestuff-Plot");
		lines.add("§7Rechtsklick: Mehr Infos");
		ItemUtil.setLore(stack, lines);

		return stack;
	}

}
