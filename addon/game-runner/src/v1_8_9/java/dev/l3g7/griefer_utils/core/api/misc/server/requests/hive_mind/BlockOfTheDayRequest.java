/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server.requests.hive_mind;

import dev.l3g7.griefer_utils.core.api.misc.server.Request;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.HIVEMIND_URL;

public abstract class BlockOfTheDayRequest extends Request<Void> {

	private final int version = 1;
	private final long timestamp = System.currentTimeMillis() / 1000;

	protected BlockOfTheDayRequest(String subpath) {
		super(HIVEMIND_URL, "/v2/block_of_the_day/" + subpath);
	}

	@Override
	protected Void parseResponse(Response response) {
		return null;
	}

	public static class Block extends BlockOfTheDayRequest {

		private final String id;
		private final int damage;
		private final String event;

		public Block(String id, int damage, String event) {
			super("block");
			this.id = id;
			this.damage = damage;
			this.event = event;
		}

	}

	public static class Reward extends BlockOfTheDayRequest {

		private final String type;
		private final int counter;
		private final int amount;
		private final String item;

		public Reward(String type, int counter, int amount, ItemStack eventItem) {
			super("reward");
			this.counter = counter;
			this.type = type;
			this.amount = amount;
			this.item = eventItem == null ? "" : eventItem.writeToNBT(new NBTTagCompound()).toString();
		}

	}

}
