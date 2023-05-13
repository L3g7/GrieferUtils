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

package dev.l3g7.griefer_utils.core.misc.matrix.requests;

import dev.l3g7.griefer_utils.core.misc.matrix.events.Event.EventContent;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.PostRequest.PutRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response;

import java.util.HashMap;
import java.util.Map;

public class SendToDeviceRequest extends PutRequest<Void> {

	// userId -> deviceId -> event
	private final Map<String, Map<String, EventContent>> messages = new HashMap<>();

	public SendToDeviceRequest(String eventType) {
		super("/_matrix/client/r0/sendToDevice/" + eventType + "/" + newTxId());
	}

	public void addMessage(String user, String deviceId, EventContent message) {
		messages.computeIfAbsent(user, i -> new HashMap<>()).put(deviceId, message);
	}

	@Override
	protected Void parseResponse(Session session, Response response) {
		return null;
	}

}