package dev.l3g7.griefer_utils.features.world.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.world.self_disguise.Disguise;
import net.minecraft.entity.monster.EntityCreeper;

/**
 * Disguised as {@link EntityCreeper}.
 */
public class CreeperDisguise extends Disguise<EntityCreeper> {
	public CreeperDisguise() {
		super(EntityCreeper.class);
	}

	@Override
	public EntityCreeper create(Arguments arguments) {
		EntityCreeper entity = super.create(arguments);
		if (arguments.getLiteral("powered"))
			entity.getDataWatcher().updateObject(17, (byte) 1);

		return entity;
	}
}
