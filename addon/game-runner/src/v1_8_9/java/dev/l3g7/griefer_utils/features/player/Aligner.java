package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.features.Feature;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@Singleton
public class Aligner extends Feature {

	@MainElement
	private final KeySetting key = KeySetting.create()
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
