/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.PlaySoundEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;

@Singleton
public class HideGrieferPass extends Feature {

	private boolean waitingForLevelSound, waitingForQuestSound;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("GrieferPass verstecken")
		.description("Versteckt Nachrichten und Sounds vom GrieferPass.")
		.icon("chest_golden");

	@EventListener
	private void onMessageReceive(MessageReceiveEvent event) {
		String text = event.message.getUnformattedText();
		if (!text.startsWith("[GrieferPass] Du hast"))
			return;

		event.cancel();
		if (text.startsWith("[GrieferPass] Du hast Level "))
			waitingForLevelSound = true;
		if (text.startsWith("[GrieferPass] Du hast die Aufgabe"))
			waitingForQuestSound = true;
	}

	@EventListener
	private void onSoundPlay(PlaySoundEvent event) {
		if (waitingForQuestSound && event.name.equals("note.pling"))
			event.cancel();
		else if (waitingForLevelSound && event.name.equals("random.levelup"))
			event.cancel();
	}

}
