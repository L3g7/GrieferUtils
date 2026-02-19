package dev.l3g7.griefer_utils.features.world.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.world.self_disguise.Disguise;
import net.minecraft.entity.monster.EntityCreeper;
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
