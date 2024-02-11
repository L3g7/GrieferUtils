/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.recraft;

import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.RecraftPage.RecraftPageListSetting;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.guis.PieMenu;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class RecraftPieMenu extends PieMenu {

	public void open(boolean animation, RecraftPageListSetting pages) { // NOTE: blur / mark empty recordings?; refactor (merge to Recraft?)
		List<Pair<String, List<Pair<String, Runnable>>>> allPages = new ArrayList<>();

		for (RecraftPage page : pages.get()) {
			List<Pair<String, Runnable>> entries = new ArrayList<>();

			for (RecraftRecording recording : page.recordings.get())
				entries.add(Pair.of(recording.name.get(), recording::play));

			allPages.add(Pair.of(page.name.get(), entries));
		}

		open(animation, allPages);
	}

}
