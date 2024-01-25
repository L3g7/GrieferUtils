/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.event.events.network;

import dev.l3g7.griefer_utils.core.event_bus.Event;
import dev.l3g7.griefer_utils.misc.server.WebAPI;
import dev.l3g7.griefer_utils.misc.server.WebAPI.Data;

/**
 * Is fired when {@link WebAPI} receives data.
 */
public class WebDataReceiveEvent extends Event {

	public final Data data;

	public WebDataReceiveEvent(Data data) {
		this.data = data;
	}

}
