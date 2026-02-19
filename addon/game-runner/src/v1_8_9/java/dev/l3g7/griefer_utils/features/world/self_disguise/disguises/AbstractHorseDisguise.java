package dev.l3g7.griefer_utils.features.world.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.world.self_disguise.Disguise;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

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
		String armor = arguments.getEnum("diamond", "gold", "iron");
		boolean saddled = arguments.getLiteral("saddled");

		EntityHorse entity = super.create(arguments);
		entity.setHorseType(type);
		entity.setHorseSaddled(saddled);
		entity.setHorseArmorStack(switch (armor) {
			case "diamond" -> new ItemStack(Items.diamond_horse_armor);
			case "gold" -> new ItemStack(Items.golden_horse_armor);
			case "iron" -> new ItemStack(Items.iron_horse_armor);
			case null, default -> null;
		});

		return entity;
	}
}
