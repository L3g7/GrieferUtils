/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.core.misc.server.requests.hive_mind;

import dev.l3g7.griefer_utils.core.misc.server.Request;
import dev.l3g7.griefer_utils.core.misc.server.Response;
import dev.l3g7.griefer_utils.core.misc.server.types.GUSession;

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
