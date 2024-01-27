/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.recraft;

import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.RecraftRecording.RecordingDisplaySetting;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.guis.PieMenu;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class RecraftPieMenu extends PieMenu {

	public void open(boolean animation, BaseSetting<?> entryContainer) {
		List<Pair<String, List<Pair<String, Runnable>>>> allPages = new ArrayList<>();

		List<RecraftPageSetting> pages = Recraft.getSubSettingsOfType(entryContainer, RecraftPageSetting.class);

		for (RecraftPageSetting page : pages) {
			List<RecordingDisplaySetting> recordings = Recraft.getSubSettingsOfType(page, RecordingDisplaySetting.class);
			List<Pair<String, Runnable>> entries = new ArrayList<>();

			for (RecordingDisplaySetting recording : recordings)
				entries.add(Pair.of(recording.recording.name.get(), () -> RecraftPlayer.play(recording.recording)));

			allPages.add(Pair.of(page.name.get(), entries));
		}

		open(animation, allPages);
	}

}