/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server.requests.hive_mind;

import dev.l3g7.griefer_utils.core.api.misc.server.Request;
import dev.l3g7.griefer_utils.core.api.misc.server.Response;
import dev.l3g7.griefer_utils.core.api.misc.server.types.GUSession;

public class MobRemoverRequest extends Request<Long> {

	private String citybuild;
	private Long value;

	public MobRemoverRequest(String citybuild, Long value) {
		super("/hive_mind/mob_remover");

		this.citybuild = citybuild;
		this.value = value;
	}

	@Override
	protected Long parseResponse(GUSession session, Response response) {
		return response.convertTo(OnlineUsersResponse.class).value;
	}

	private static class OnlineUsersResponse {

		Long value;

	}

}
