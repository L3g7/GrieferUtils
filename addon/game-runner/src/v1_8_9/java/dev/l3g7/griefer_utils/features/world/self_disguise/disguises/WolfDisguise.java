package dev.l3g7.griefer_utils.features.world.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.world.self_disguise.Disguise;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.EnumDyeColor;

/**
 * Disguised as {@link EntityWolf}.
 */
public class WolfDisguise extends Disguise<EntityWolf> {

	private static final String[] colors = new String[]{"black", "blue", "brown", "cyan", "gray", "green", "light_blue", "lime", "magenta", "orange", "pink", "purple", "red", "silver", "white", "yellow"};

	public WolfDisguise() {
		super(EntityWolf.class);
	}

	@Override
	public EntityWolf create(Arguments arguments) {
		boolean angry = arguments.getLiteral("angry");
		boolean tamed = arguments.getLiteral("tamed");
		String colorName = arguments.getEnum("white", colors);

		EnumDyeColor color = EnumDyeColor.WHITE;
		for (EnumDyeColor value : EnumDyeColor.values())
			if (value.getName().equalsIgnoreCase(colorName))
				color = value;

		EntityWolf entity = super.create(arguments);
		entity.setAngry(angry);
		entity.setTamed(tamed);
		entity.setCollarColor(color);
		return entity;
	}
}
