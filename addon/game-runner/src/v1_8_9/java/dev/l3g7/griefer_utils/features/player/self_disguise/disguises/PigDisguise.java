/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.player.self_disguise.Disguise;
import net.minecraft.entity.passive.EntityPig;

/**
 * Disguised as {@link EntityPig}.
 */
public class PigDisguise extends Disguise<EntityPig> {
	public PigDisguise() {
		super(EntityPig.class);
	}

	@Override
	public EntityPig create(Arguments arguments) {
		EntityPig entity = super.create(arguments);
		entity.setSaddled(arguments.getLiteral("saddled"));
		return entity;
	}
}
