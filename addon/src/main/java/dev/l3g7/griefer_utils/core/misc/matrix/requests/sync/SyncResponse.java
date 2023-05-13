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

package dev.l3g7.griefer_utils.core.misc.matrix.requests.sync;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncResponse {

	@SerializedName("next_batch")
	public String nextBatch;

	@SerializedName("account_data")
	public EventList accountData = new EventList();

	@SerializedName("device_one_time_keys_count")
	public Map<String, Integer> deviceOneTimeKeysCount;

	public Rooms rooms = new Rooms();

	@SerializedName("to_device")
	public EventList toDevice;

	public static class EventList {
		public List<RawEvent> events = Collections.emptyList();
	}

	public static class RawEvent {

		public String type;
		public Object content;

		@SerializedName("state_key")
		public String stateKey;

		@SerializedName("sender_key")
		public String senderKey;

	}

	public static class Rooms {

		public Map<String, JoinedRoom> join = new HashMap<>();
		public Map<String, InvitedRoom> invite = new HashMap<>();

	}

	public static class JoinedRoom {

		public EventList timeline;

		@SerializedName("account_data")
		public EventList accountData;

	}

	public static class InvitedRoom {

		@SerializedName("invite_state")
		public EventList inviteState;

	}

}