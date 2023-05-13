/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.core.misc.matrix.events;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.matrix.events.Event.EventContent;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;

import java.util.List;
import java.util.Map;

/**
 * A map of which rooms are considered ‘direct’ rooms for specific users.
 * <p>
 * <a href="https://spec.matrix.org/v1.6/client-server-api/#mdirect">Matrix spec</a>
 */
@Event(key = "m.direct", asContentType = false)
public class DirectEvent extends EventContent {

	/**
	 * User ID -> Room IDs
	 */
	@SerializedName("content")
	public Map<String, List<String>> directRooms;

	@Override
	public void handle(Session session) {
		session.directRooms = directRooms;
	}

}