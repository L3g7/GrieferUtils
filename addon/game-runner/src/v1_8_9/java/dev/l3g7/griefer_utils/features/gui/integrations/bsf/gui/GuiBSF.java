/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.gui.integrations.bsf.gui;

import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.gui.integrations.bsf.BSF;
import dev.l3g7.griefer_utils.features.gui.integrations.bsf.BSFCollector;
import dev.l3g7.griefer_utils.features.gui.integrations.bsf.Waypoint;
import dev.l3g7.griefer_utils.features.gui.integrations.bsf.data.Category;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.misc.griefer_games.Citybuild.ANY;
import static dev.l3g7.griefer_utils.core.misc.griefer_games.Citybuild.MAGIC_FOREST;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

public class GuiBSF extends GuiBigChest {

	public static long lastUpdate = 0;

	public GuiBSF() {
		super("Biom- und Strukturen-Suche", 3);
	}

	@Override
	public void open() {
		if (!BSF.hasData() && !BSFCollector.processing && lastUpdate + 10_000 <= System.currentTimeMillis()) {
			GUServer.getBSFReady().thenAccept(cbs -> {
				BSF.updateCBs(cbs);
				if (mc().currentScreen instanceof GuiBSF)
					new GuiBSF().open(); // Rebuild GUI
			});
			lastUpdate = System.currentTimeMillis();
		}

		super.open();
		if (!BSF.hasData()) {
			if (getCurrentCitybuild() == ANY || getCurrentCitybuild() == MAGIC_FOREST) {
				TextureItem item = new TextureItem("hourglass", "§fStatus: §cNicht bereit", "§fBitte betrete einen Citybuild.");
				addTextureItem(13, item, null);
				return;
			}

			List<String> lore = new ArrayList<>(Arrays.asList("§fBitte erkunde die " + (BSF.isInGlitchwelt() ? "Glitchwelt." : "Farmwelt.")));

			if (!BSF.notify.contains(BSF.getCurrentCBString())) {
				lore.addAll(Arrays.asList("",
					"§7Wenn du eine Benachrichtigung bekommen willst,",
					"§7sobald die Suche bereit ist, klicke auf das Item."));
			}

			TextureItem item = new TextureItem("hourglass", "§fStatus: §cNicht bereit", lore.toArray(new String[0]));

			addTextureItem(13, item, () -> {
				if (BSF.notify.add(BSF.getCurrentCBString())) {
					labyBridge.notify("§aBenachrichtigung", "§aDu bekommst nun " + (LABY_4.isActive() ? "eine Benachrichtigung" : "ein Popup") + ",\nwenn die Suche bereit ist!");
					new GuiBSF().open(); // Rebuild GUI
				}
			});
			return;
		}

		addTextureItem(11, new TextureItem("earth", "§fBiome"), new GuiSelect("Biom-Suche", Category.ALL_BIOMES, this)::open);
		addTextureItem(13, new TextureItem("structures/desert_pyramid", "§fStrukturen"), new GuiSelect("Strukturen-Suche", Category.ALL_STRUCTURES, this)::open);

		if (Waypoint.isEnabled()) {
			ItemStack item = ItemUtil.createItem(Blocks.beacon, 0, "§fAktiver Wegpunkt: " + Waypoint.target.getName().singular() + " (" + distanceToPlayer(Waypoint.x, Waypoint.z) + "m)");
			ItemUtil.setLore(item, "§fPosition: " + Waypoint.x + ", " + Waypoint.z,
				"",
				"§7Klicke auf das Item, um den Wegpunkt zu deaktivieren.");
			addItem(15, item, () -> {
				Waypoint.disable();
				new GuiBSF().open();
			});
		} else {
			addItem(15, ItemUtil.setLore(ItemUtil.createItem(Blocks.glass, 0, "§cKein Wegpunkt aktiv.")), null);
		}
	}
}
