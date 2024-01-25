/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.util;

import de.emotechat.addon.gui.ChatLineEntry;
import de.emotechat.addon.gui.chat.render.EmoteChatLine;
import net.labymod.ingamechat.renderer.ChatLine;

public class EmoteChatUtil {

	public static boolean isEmote(ChatLine chatLine) {
		if (!(chatLine instanceof EmoteChatLine))
			return false;

		return ((EmoteChatLine) chatLine).getEntries().stream().anyMatch(ChatLineEntry::isLoadedEmote);
	}

}
