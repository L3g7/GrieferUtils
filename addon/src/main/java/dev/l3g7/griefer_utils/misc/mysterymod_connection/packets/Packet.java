/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.misc.mysterymod_connection.packets;

import java.util.UUID;

public abstract class Packet {

	public boolean hasUuid = true;
	public UUID uuid;

	public Packet() {}

	public Packet(boolean hasUuid) {
		this();
		this.hasUuid = hasUuid;
	}

}
