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

package dev.l3g7.griefer_utils.api.misc.xbox_profile_resolver.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class XboxProfile {

	public String id;
	public String displayName;
	public String avatar;

	public XboxProfile(JsonObject obj) {
		this.id = obj.get("id").getAsString();
		for (JsonElement elem : obj.get("settings").getAsJsonArray()) {
			JsonObject setting = elem.getAsJsonObject();
			String settingId = setting.get("id").getAsString();

			if (settingId.equals("GameDisplayName"))
				displayName = setting.get("value").getAsString();
			else if (settingId.equals("GameDisplayPicRaw"))
				avatar = setting.get("value").getAsString();
		}
	}

}