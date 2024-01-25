/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class MissingAdventurerBlocks extends Module {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Fehlende Adv. Blöcke")
		.description("Zeigt dir an, wie viele Blöcke mit dem in der Hand gehaltenen Adventure-Werkzeug noch abgebaut werden müssen.")
		.icon(Material.FIREBALL);

	@Override
	public boolean isShown() {
		return super.isShown() && getMissingBlocks() != -1;
	}

	@Override
	public String[] getKeys() {
		return getDefaultKeys();
	}

	@Override
	public String[] getDefaultKeys() {
		return new String[] { "Fehlende Blöcke" };
	}

	@Override
	public String[] getValues() {
		return new String[] { Constants.DECIMAL_FORMAT_98.format(getMissingBlocks()) };
	}

	@Override
	public String[] getDefaultValues() {
		return new String[] { "0" };
	}

	private int getMissingBlocks() {
		return player() == null ? -1 : getMissingBlocks(player().getHeldItem());
	}

	public static int getMissingBlocks(ItemStack stack) {
		if (stack == null)
			return -1;

		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null || !tag.hasKey("adventure"))
			return -1;

		NBTTagCompound adventureTag = tag.getCompoundTag("adventure");
		return adventureTag.getInteger("adventure.req_amount") - adventureTag.getInteger("adventure.amount");
	}

}
