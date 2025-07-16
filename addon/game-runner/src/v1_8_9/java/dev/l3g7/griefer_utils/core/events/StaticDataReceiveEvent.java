/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.StaticApiRequest.StaticApiData;

/**
 * Fired when {@link GUServer} loads the static API.
 */
public class StaticDataReceiveEvent extends Event {

	public final StaticApiData data;

	public StaticDataReceiveEvent(StaticApiData data) {
		this.data = data;
	}

}
