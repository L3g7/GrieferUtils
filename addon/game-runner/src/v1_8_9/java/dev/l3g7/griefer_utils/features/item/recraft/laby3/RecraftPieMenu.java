/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.laby3;

import dev.l3g7.griefer_utils.features.item.recraft.laby3.RecraftRecording.RecordingDisplaySetting;
import net.labymod.settings.elements.SettingsElement;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class RecraftPieMenu extends PieMenu {

	public void open(boolean animation, SettingsElement entryContainer) {
		List<Pair<String, List<Pair<String, Runnable>>>> allPages = new ArrayList<>();

		List<RecraftPageSetting> pages = RecraftBridgeImpl.getSubSettingsOfType(entryContainer, RecraftPageSetting.class);

		for (RecraftPageSetting page : pages) {
			List<RecordingDisplaySetting> recordings = RecraftBridgeImpl.getSubSettingsOfType(page, RecordingDisplaySetting.class);
			List<Pair<String, Runnable>> entries = new ArrayList<>();

			for (RecordingDisplaySetting recording : recordings)
				entries.add(Pair.of(recording.recording.name().get(), () -> recording.recording.getCore().play(false)));

			allPages.add(Pair.of(page.name.get(), entries));
		}

		open(animation, allPages);
	}

}
