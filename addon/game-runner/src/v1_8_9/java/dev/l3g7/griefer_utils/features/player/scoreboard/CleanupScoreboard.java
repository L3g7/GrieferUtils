/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.scoreboard;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.player.scoreboard.ScoreboardHandler.LineProvider;

@Singleton
public class CleanupScoreboard extends Feature implements LineProvider {

	final SwitchSetting playTime = SwitchSetting.create()
		.name("Spielzeit entfernen")
		.icon("clock")
		.defaultValue(true)
		.callback(ScoreboardHandler::update);

	final SwitchSetting ip = SwitchSetting.create()
		.name("IP entfernen")
		.icon("griefer_games")
		.defaultValue(true)
		.callback(ScoreboardHandler::update);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Scoreboard aufräumen")
		.description("Löscht bestimmte Einträge im Scoreboard")
		.icon("wooden_board")
		.subSettings(playTime, ip)
		.since("2.4-BETA-1")
		.callback(ScoreboardHandler::update);

	public static CleanupScoreboard get() {
		return get(CleanupScoreboard.class);
	}

	@Override
	public void createLine() {}

	@Override
	public boolean shouldHide(String key) {
		if (!isEnabled())
			return false;

		if (key.equals("playtime"))
			return playTime.get();

		if (key.equals("address"))
			return ip.get();

		return false;
	}
}
