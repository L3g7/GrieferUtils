package dev.l3g7.griefer_utils.features.features.selfDisguise;

import net.minecraft.entity.Entity;

/**
 * @author L3g73 (L3g7 said i should put that there ._.)
 */
public class Disguise {
	private final Entity entity;
	private boolean isBlockCoordinates = false;

	public Disguise(Entity entity) {
		this.entity = entity;
	}

	public void enableBlockCoordinates() {
		isBlockCoordinates = true;
	}

	public Entity getEntity() {
		return entity;
	}

	public boolean isBlockCoordinates() {
		return isBlockCoordinates;
	}

}