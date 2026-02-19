package dev.l3g7.griefer_utils.features.world.self_disguise;

import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

public class Disguise<E extends Entity> {

	private final Class<E> type;

	public Disguise(Class<E> type) {
		this.type = type;
	}

	public E create(Arguments arguments) {
		return Reflection.construct(type, world());
	}

	public boolean clampCoordinates(Arguments arguments) {
		return false;
	}

	public static class Arguments extends ArrayList<String> {
		public Arguments(String[] arguments) {
			super(Arrays.asList(arguments));
		}

		public boolean getLiteral(String literal) {
			boolean found = false;
			while (remove(literal))
				found = true;

			return found;
		}

		public String getEnum(String fallback, String... options) {
			String value = fallback;

			for (Iterator<String> iterator = this.iterator(); iterator.hasNext(); ) {
				String entry = iterator.next();
				for (String option : options) {
					if (entry.equalsIgnoreCase(option)) {
						iterator.remove();
						value = option;
					}
				}
			}

			return value;
		}

	}

	public static int find(String needle, String[] haystack) {
		int index = 0;
		for (String s : haystack) {
			if (s.equals(needle))
				return index;

			index++;
		}

		return -1;
	}
}
