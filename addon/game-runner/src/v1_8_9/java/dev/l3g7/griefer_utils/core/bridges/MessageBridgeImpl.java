/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.bridges;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageBridge;
import net.minecraft.util.IChatComponent;

@Bridge
@Singleton
public class MessageBridgeImpl implements MessageBridge {

	@Override
	public IChatComponent fromLaby(Object message) {
		return (IChatComponent) message;
	}

	@Override
	public Object toLaby(IChatComponent message) {
		return message;
	}

}
