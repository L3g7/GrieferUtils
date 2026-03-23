package dev.l3g7.griefer_utils.features.player.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.player.self_disguise.Disguise;
import net.minecraft.entity.monster.EntityGuardian;

/**
 * Disguised as {@link EntityGuardian} (elder).
 */
public class ElderGuardianDisguise extends Disguise<EntityGuardian> {
	public ElderGuardianDisguise() {
		super(EntityGuardian.class);
	}

	@Override
	public EntityGuardian create(Arguments arguments) {
		EntityGuardian entity = super.create(arguments);
		entity.setElder();
		return entity;
	}
}
