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

package dev.l3g7.griefer_utils.core.misc.matrix.events.room;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.matrix.events.Event;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Room;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;

@Event(key = "m.room.encryption")
public class RoomEncryptionEvent extends RoomEventContent {

	public String algorithm;
	@SerializedName("rotation_period_ms")
	public int expiryTimeMs = 604800000;
	@SerializedName("rotation_period_msgs")
	public int maxMessages = 100;

	@Override
	public void handle(Session session, Room room) {
		if (!algorithm.equals("m.megolm.v1.aes-sha2"))
			return;

		room.encryptionMetadata.encrypted = true;
		room.encryptionMetadata.expiryTimeMs = expiryTimeMs;
		room.encryptionMetadata.maxMessages = maxMessages;
	}

}