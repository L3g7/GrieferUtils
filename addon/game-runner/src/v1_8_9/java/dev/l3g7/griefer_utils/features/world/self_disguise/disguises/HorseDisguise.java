package dev.l3g7.griefer_utils.features.world.self_disguise.disguises;

import net.minecraft.entity.passive.EntityHorse;

/**
 * Disguised as {@link EntityHorse} (base type).
 */
public class HorseDisguise extends AbstractHorseDisguise {

	private static final String[] colors = new String[]{"white", "creamy", "chestnut", "brown", "black", "gray", "dark_brown"};
	private static final String[] variants = new String[]{"white_stripes", "whitefield", "white_dots", "black_dots"};

	public HorseDisguise() {
		super(0);
	}

	@Override
	public EntityHorse create(Arguments arguments) {
		String color = arguments.getEnum("brown", colors);
		String colorVariant = arguments.getEnum(null, variants);

		int variant = find(color, colors);
		if (colorVariant != null)
			variant |= 0x100 * find(colorVariant, variants);

		EntityHorse entity = super.create(arguments);
		entity.setHorseVariant(variant);

		return entity;
	}
}
