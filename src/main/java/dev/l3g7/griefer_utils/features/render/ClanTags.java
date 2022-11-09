/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.features.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.user.User;
import net.labymod.utils.ModColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.labyMod;

/**
 * Shows a player's clan tag underneath their name tag.
 */
@Singleton
public class ClanTags extends Feature {

	private final Map<UUID, String> tags = new HashMap<>();

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Clantags")
		.description("Zeigt den Clantag eines Spielers unter seinem Nametag.")
		.icon("rainbow_name")
		.defaultValue(true)
		.callback(this::toggleClanTags);

	private void toggleClanTags(boolean enabled) {
		if (enabled) {
			tags.forEach((uuid, tag) -> {
				User user = labyMod().getUserManager().getUser(uuid);
				user.setSubTitle(tag);
				user.setSubTitleSize(0.8);
			});
		} else {
			for (User user : labyMod().getUserManager().getUsers().values())
				user.setSubTitle(null);
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onPlayerTick(MysteryModPayloadEvent event) {
		if (!event.channel.equals("user_subtitle"))
			return;

		for (JsonElement elem : event.payload.getAsJsonArray()) {
			JsonObject obj = elem.getAsJsonObject();

			UUID uuid = UUID.fromString(obj.get("targetId").getAsString());
			User user = labyMod().getUserManager().getUser(uuid);

			String tag = ModColor.createColors(obj.get("text").getAsString());
			tags.put(uuid, tag);
			if (isEnabled()) {
				user.setSubTitle(tag);
				user.setSubTitleSize(obj.get("scale").getAsDouble());
			}
		}
	}

}
