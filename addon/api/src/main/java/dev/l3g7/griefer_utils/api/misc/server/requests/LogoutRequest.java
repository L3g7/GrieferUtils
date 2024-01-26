/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.misc.server.requests;

import dev.l3g7.griefer_utils.api.misc.server.Request;
import dev.l3g7.griefer_utils.api.misc.server.Response;
import dev.l3g7.griefer_utils.api.misc.server.types.GUSession;

public class LogoutRequest extends Request<Void> {

	public LogoutRequest() {
		super("/logout");
	}

	@Override
	protected Void parseResponse(GUSession session, Response response) {
		return null;
	}

}