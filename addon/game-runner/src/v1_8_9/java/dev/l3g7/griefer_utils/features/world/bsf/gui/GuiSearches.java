/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.bsf.gui;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.bsf.BSFSearchRequest;
import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.world.bsf.BSF;
import dev.l3g7.griefer_utils.features.world.bsf.data.BSFSearchable;
import dev.l3g7.griefer_utils.features.world.bsf.data.Biome;
import dev.l3g7.griefer_utils.features.world.bsf.data.Category;
import dev.l3g7.griefer_utils.features.world.bsf.data.Structure;
import dev.l3g7.griefer_utils.features.world.bsf.waypoint.Waypoint;
import net.minecraft.init.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.distanceToPlayer;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class GuiSearches extends GuiBigChest {

	private final BSFSearchable searchable;
	private final List<BSF.SearchData> searchData;

	public GuiSearches(BSFSearchable searchable, String title, TextureItem icon, GuiBigChest previousGui) {
		this(searchable, title, icon, previousGui, !BSF.isInFarmwelt()
			? ImmutableList.of()
			: BSF.SEARCH_DATA.computeIfAbsent(MinecraftUtil.getCurrentCitybuild(), k -> new HashMap<>())
			.computeIfAbsent(searchable, k -> new ArrayList<>()));
	}

	private GuiSearches(BSFSearchable searchable, String title, TextureItem icon, GuiBigChest previousGui, List<BSF.SearchData> searchData) {
		super(title, Math.max(4, (searchData.size() % 5 == 0 ? 2 : 3 /* fix rounding */) + (searchData.size() / 5)), previousGui);
		this.searchable = searchable;
		this.searchData = searchData;

		String searchNewName = "Neue" + searchable.getName().pronomialSuffix + " " + searchable.getName().singular + " suchen";

		if (!BSF.hasData()) {
			addItem(10, ItemUtil.setLore(ItemUtil.createItem(Blocks.barrier, 0, "§7" + searchNewName), "", "§cNicht genügend Daten verfügbar!"), null);
			return;
		}

		if (!BSF.isInFarmwelt()) {
			addItem(10, ItemUtil.setLore(ItemUtil.createItem(Blocks.barrier, 0, "§7" + searchNewName), "", "§cBitte betrete eine Farmwelt!"), null);
		} else {
			addTextureItem(10, new TextureItem("lens", "§f" + searchNewName), () -> {
				mc().displayGuiScreen(null);
				List<Integer> exclude = searchData.stream().map(sd -> sd.index).collect(Collectors.toList());

				if (searchable instanceof Structure s) {
					GUServer.searchStructure(s.ordinal(), exclude).thenAccept(this::onSearchResponse);
					return;
				}

				List<Integer> ids;
				if (searchable instanceof Biome b) {
					ids = b.getIds();
				} else {
					ids = ((Category) searchable).biomeIds;
				}

				GUServer.searchBiome(ids, exclude).thenAccept(this::onSearchResponse);
			});
		}

		int counter = 12;

		for (BSF.SearchData coordinates : searchData) {
			TextureItem searchItem = icon.copy();
			searchItem.toolTipStack.setStackDisplayName("§fKoordinaten: " + coordinates.x + ", " + coordinates.z + " (" + distanceToPlayer(coordinates.x, coordinates.z) + "m)");
			addTextureItem(counter++, searchItem, () -> {
				Waypoint.setWaypoint(coordinates.x, coordinates.z, searchable);
				labyBridge.notify("§aWegpunkt gesetzt", "§aWegpunkt wurde auf " + coordinates.x + " " + coordinates.z + " gesetzt.");
				mc().displayGuiScreen(null);
			});

			if (counter % 9 == 8)
				counter += 4;
		}
	}

	private void onSearchResponse(BSFSearchRequest.SearchResponse r) {
		if (r == BSFSearchRequest.SearchResponse.ALL_FOUND) {
			labyBridge.notify("§cAlles gefunden \u26A0", "§cDu hast schon alle " + searchable.getName().plural + " gefunden!");
			return;
		}

		if (r == BSFSearchRequest.SearchResponse.WORLD_NOT_READY) {
			labyBridge.notify("§cWelt gelöscht \u26A0", "§cBitte erkunde die Farmwelt erneut.");
			BSF.readyCbs.remove(MinecraftUtil.getCurrentCitybuild().getInternalName());
			return;
		}

		int mc_x = r.pos.x;
		int mc_z = r.pos.z;
		if (searchable != Structure.STRONGHOLD) {
			mc_x = r.pos.x * 16 + 8 * Integer.signum(r.pos.x);
			mc_z = r.pos.z * 16 + 8 * Integer.signum(r.pos.z);
		}

		searchData.add(new BSF.SearchData(mc_x, mc_z, r.index));
		labyBridge.notify("§a" + mc_x + " " + mc_z, "§a(Folge dem Beacon " + distanceToPlayer(mc_x, mc_z) + "m)");
		Waypoint.setWaypoint(mc_x, mc_z, searchable);
	}

}
