/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.misc.server;

import dev.l3g7.griefer_utils.api.util.IOUtil;

public class Response {

	private final String body;

	public Response(String body) {
		this.body = body;
	}

	public <T> T convertTo(Class<T> type, T fallback) {
		T res = convertTo(type);
		return res == null ? fallback : res;
	}

	public <T> T convertTo(Class<T> type) {
		return IOUtil.gson.fromJson(body, type);
	}

}