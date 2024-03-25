/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class Aligner extends Feature {

	@MainElement
	private final KeySetting key = new KeySetting()
		.name("Ausrichten")
		.description("Setzt die yaw-Rotation des Spielers auf die nähesten 45°.")
		.icon("rotations")
		.pressCallback(b -> {
			if (!b)
				return;

			float yaw = player().getRotationYawHead();

			float extraYaw = yaw % 45;
			yaw = (float) Math.floor(yaw / 45);
			yaw *= 45;

			if (extraYaw > 22.5)
				yaw += 45;

			player().rotationYawHead = player().rotationYaw = yaw;
		});

}
