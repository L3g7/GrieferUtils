package dev.l3g7.griefer_utils.features.player.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.player.self_disguise.Disguise;
import net.minecraft.entity.passive.EntityHorse;

/**
 * Disguised as {@link EntityHorse}.
 */
public class AbstractHorseDisguise extends Disguise<EntityHorse> {

	private final int type;

	public AbstractHorseDisguise(int type) {
		super(EntityHorse.class);
		this.type = type;
	}

	@Override
	public EntityHorse create(Arguments arguments) {
		boolean saddled = arguments.getLiteral("saddled");

		EntityHorse entity = super.create(arguments);
		entity.setHorseType(type);
		entity.setHorseSaddled(saddled);

		return entity;
	}
}
