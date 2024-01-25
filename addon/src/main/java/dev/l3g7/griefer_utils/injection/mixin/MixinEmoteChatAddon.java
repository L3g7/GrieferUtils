/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.injection.mixin;

import de.emotechat.addon.EmoteChatAddon;
import dev.l3g7.griefer_utils.core.misc.Constants;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EmoteChatAddon.class)
public class MixinEmoteChatAddon {

	public MixinEmoteChatAddon() {
		Constants.EMOTECHAT = true;
	}

}
