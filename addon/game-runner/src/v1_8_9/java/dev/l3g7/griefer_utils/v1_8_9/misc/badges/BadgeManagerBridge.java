/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc.badges;

import dev.l3g7.griefer_utils.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;

import java.util.UUID;

@Bridged
public interface BadgeManagerBridge {

	BadgeManagerBridge badgeManager = FileProvider.getBridge(BadgeManagerBridge.class);

	boolean isSpecial(String uuid);

	void queueUser(UUID uuid);

	void removeUser(UUID uuid);

	void clearUsers();

}
