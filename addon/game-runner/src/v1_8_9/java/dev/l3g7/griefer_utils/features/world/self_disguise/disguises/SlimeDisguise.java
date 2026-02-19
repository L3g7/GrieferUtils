package dev.l3g7.griefer_utils.features.world.self_disguise.disguises;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.world.self_disguise.Disguise;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;

import java.util.Iterator;

/**
 * Disguised as {@link EntitySlime} / {@link EntityMagmaCube}.
 */
public class SlimeDisguise<E extends EntitySlime> extends Disguise<E> {
	public SlimeDisguise(Class<E> type) {
		super(type);
	}

	@Override
	public E create(Arguments arguments) {
		int size = 2;

		for (Iterator<String> iterator = arguments.iterator(); iterator.hasNext(); ) {
			String entry = iterator.next().toLowerCase();
			if (entry.equals("big")) {
				size = 4;
				iterator.remove();
			} else if (entry.equals("normal")) {
				size = 2;
				iterator.remove();
			} else if (entry.equals("tiny")) {
				size = 1;
				iterator.remove();
			} else if (entry.startsWith("size=")) {
				size = Math.min(Integer.parseInt(entry.substring("size=".length())), 100);
				iterator.remove();
			}
		}

		E entity = super.create(arguments);
		Reflection.invoke(entity, "setSlimeSize", size);
		return entity;
	}
}
