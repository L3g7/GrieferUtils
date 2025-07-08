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

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class GuiBSF extends GuiBigChest {

	public static long lastUpdate = 0;

	public GuiBSF() {
		super("Biom- und Strukturen-Suche", 3);
	}

	private void updateStatusItem() {
		if (BSF.hasData()) {
			addItem(16, ItemUtil.createItem(Blocks.wool, 5, "§fStatus: §aBereit"), null);
		} else {
			ItemStack stack = ItemUtil.createItem(Blocks.wool, 14, "§fStatus: §cNicht bereit");
			ItemUtil.setLore(stack, "",
				"§7Bitte erkunde die Farmwelt weiter.",
				"§7Wenn du eine Benachrichtigung bekommen willst,",
				"§7sobald die Suche bereit ist, klicke auf das Item.");
			addItem(16, stack, () -> {
				BSFCollector.notify = true;
				labyBridge.notify("§aBenachrichtigung", "§aDu bekommst nun eine Benachrichtigung,\nwenn die Suche bereit ist!");
				mc().displayGuiScreen(null);
			});
		}
	}

	@Override
	public void open() {
		if (lastUpdate - 10_000 <= System.currentTimeMillis()) {
			GUServer.getBSFReady().thenAccept(cbs -> {
				BSF.readyCbs.addAll(cbs);
				updateStatusItem();

			});
			lastUpdate = System.currentTimeMillis();
		}

		super.open();
		addItem(10, ItemUtil.createItem(Blocks.grass, 0, "§fBiome"), new GuiSelect("Biom-Suche", Category.ALL, this)::open);
		addItem(12, ItemUtil.createItem(Blocks.prismarine, 1, "§fStrukturen"), new GuiSelect("Strukturen-Suche", Category.ALL_STRUCTURES, this)::open);
		if (Waypoint.enabled) {
			addItem(14, ItemUtil.createItem(Blocks.beacon, 0, "§fWegpunkt deaktivieren"), () -> {
				Waypoint.enabled = false;
				mc().displayGuiScreen(null);
				labyBridge.notify("§aWegpunkt deaktiviert", "§aDer Wegpunkt wurde deaktiviert.");
			});
		} else {
			addItem(14, ItemUtil.setLore(ItemUtil.createItem(Blocks.barrier, 0, "§cWegpunkt deaktivieren"), "", "§cEs ist derzeit kein Wegpunkt aktiv."), null);
		}

		updateStatusItem();
	}
}
