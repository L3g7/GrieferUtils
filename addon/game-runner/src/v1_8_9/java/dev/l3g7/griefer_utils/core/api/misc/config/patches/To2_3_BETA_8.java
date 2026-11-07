/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;
import net.labymod.ingamechat.tools.filter.Filters.Filter;
import net.labymod.main.LabyMod;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

public class To2_3_BETA_8 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.3-BETA-8") && LABY_3.isActive();
	}

	@Override
	public void patch() {
		// Fix null entries in filters
		for (Filter filter : LabyMod.getInstance().getChatToolManager().getFilters()) {
			String[] containsNot = filter.getWordsContainsNot();
			if (containsNot.length == 0)
				continue;

			String[] newContainsNot = new String[containsNot.length];
			int idx = 0;
			for (String s : filter.getWordsContainsNot())
				if (s != null)
					newContainsNot[idx++] = s;

			String[] newContainsNotCopy = new String[idx];
			System.arraycopy(newContainsNot, 0, newContainsNotCopy, 0, idx);
			filter.setWordsContainsNot(newContainsNotCopy);
		}

		LabyMod.getInstance().getChatToolManager().saveTools();
	}

}
