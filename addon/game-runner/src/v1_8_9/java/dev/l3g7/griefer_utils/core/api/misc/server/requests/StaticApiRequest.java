/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server.requests;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.server.Request;
import dev.l3g7.griefer_utils.core.api.util.io.IO;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.STATIC_API_URL;

public class StaticApiRequest extends Request<JsonObject> {

	public StaticApiRequest() {
		super(STATIC_API_URL, "/v6/");
	}

	@Override
	protected JsonObject parseResponse(Response response) {
		return response.convertTo(JsonObject.class, IO.GSON);
	}

}
