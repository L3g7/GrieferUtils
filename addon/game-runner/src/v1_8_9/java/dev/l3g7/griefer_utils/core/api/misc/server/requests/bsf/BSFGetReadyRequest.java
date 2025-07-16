/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server.requests.bsf;

import dev.l3g7.griefer_utils.core.api.misc.server.Request;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.BSF_URL;

public class BSFGetReadyRequest extends Request<List<String>> {

	public BSFGetReadyRequest() {
		super(BSF_URL, "/get_ready");
	}

	@Override
	protected List<String> parseResponse(Response response) {
		return response.convertTo(List.class);
	}

}
