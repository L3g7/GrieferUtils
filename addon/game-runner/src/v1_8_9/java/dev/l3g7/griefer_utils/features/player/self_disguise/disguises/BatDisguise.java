package dev.l3g7.griefer_utils.features.player.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.player.self_disguise.Disguise;
import net.minecraft.entity.passive.EntityBat;

/**
 * Disguised as {@link EntityBat}.
 */
public class BatDisguise extends Disguise<EntityBat> {
	public BatDisguise() {
		super(EntityBat.class);
	}

	@Override
	public EntityBat create(Arguments arguments) {
		EntityBat entity = super.create(arguments);
		entity.setIsBatHanging(false);
		return entity;
	}
}
