package dev.l3g7.griefer_utils.features.player.self_disguise.disguises;

import dev.l3g7.griefer_utils.features.player.self_disguise.Disguise;
import net.minecraft.entity.passive.EntityVillager;

/**
 * Disguised as {@link EntityVillager}.
 */
public class VillagerDisguise extends Disguise<EntityVillager> {

	private static final String[] professions = new String[]{"farmer", "librarian", "priest", "blacksmith", "butcher", "nitwit"};

	public VillagerDisguise() {
		super(EntityVillager.class);
	}

	@Override
	public EntityVillager create(Arguments arguments) {
		String profession = arguments.getEnum("nitwit", professions);

		EntityVillager entity = super.create(arguments);
		entity.setProfession(find(profession, professions));
		return entity;
	}
}
