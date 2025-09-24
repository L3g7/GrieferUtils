/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.bsf.gui;

import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.world.bsf.BSF;
import dev.l3g7.griefer_utils.features.world.bsf.BSFCollector;
import dev.l3g7.griefer_utils.features.world.bsf.Waypoint;
import dev.l3g7.griefer_utils.features.world.bsf.data.Category;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

public class GuiBSF extends GuiBigChest {

	public static GuiBSF GUI = new GuiBSF();

	public static long lastUpdate = 0;

	private GuiBSF() {
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
			List<String> lore = new ArrayList<>(Arrays.asList("§fBitte erkunde die Farmwelt."));

			if (!BSF.notify.contains(getCurrentCitybuild())) {
				lore.addAll(Arrays.asList("",
					"§7Wenn du eine Benachrichtigung bekommen willst,",
					"§7sobald die Suche bereit ist, klicke auf das Item."));
			}

			TextureItem item = new TextureItem("hourglass", "§fStatus: §cNicht bereit", lore.toArray(new String[0]));

			addTextureItem(11, null, null);
			addTextureItem(13, item, () -> {
				if (BSF.notify.add(getCurrentCitybuild())) {
					labyBridge.notify("§aBenachrichtigung", "§aDu bekommst nun eine Benachrichtigung,\nwenn die Suche bereit ist!");
					new GuiBSF().open(); // Rebuild GUI
				}
			});
			addTextureItem(15, null, null);
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
