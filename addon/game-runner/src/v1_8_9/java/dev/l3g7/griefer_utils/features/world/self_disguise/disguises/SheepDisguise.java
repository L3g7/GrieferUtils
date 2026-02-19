package dev.l3g7.griefer_utils.features.world.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.world.self_disguise.Disguise;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;

/**
 * Disguised as {@link EntitySheep}.
 */
public class SheepDisguise extends Disguise<EntitySheep> {

	private static final String[] colors = new String[]{"black", "blue", "brown", "cyan", "gray", "green", "light_blue", "lime", "magenta", "orange", "pink", "purple", "red", "silver", "white", "yellow"};

	public SheepDisguise() {
		super(EntitySheep.class);
	}

	@Override
	public EntitySheep create(Arguments arguments) {
		String colorName = arguments.getEnum("white", colors);

		EnumDyeColor color = EnumDyeColor.WHITE;
		for (EnumDyeColor value : EnumDyeColor.values())
			if (value.getName().equalsIgnoreCase(colorName))
				color = value;

		EntitySheep entity = super.create(arguments);
		entity.setFleeceColor(color);
		return entity;
	}
}
