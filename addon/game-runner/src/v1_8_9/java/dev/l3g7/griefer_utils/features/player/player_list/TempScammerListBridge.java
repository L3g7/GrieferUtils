/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.player.player_list;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.v1_8_9.features.player.player_list.PlayerList.MarkAction;
import net.minecraft.util.ChatComponentText;

import java.util.UUID;

@Bridged
public interface TempScammerListBridge {

	boolean isEnabled();

	boolean shouldMark(String name, UUID uuid);

	MarkAction getChatAction();

	ChatComponentText toComponent(MarkAction action);

}
