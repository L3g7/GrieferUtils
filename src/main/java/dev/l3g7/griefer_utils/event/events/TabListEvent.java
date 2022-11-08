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

package dev.l3g7.griefer_utils.event.events;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.labymod.PacketReceiveEvent;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.minecraft.network.play.server.S38PacketPlayerListItem.Action.ADD_PLAYER;
import static net.minecraft.network.play.server.S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME;
import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

/**
 * An event related to the tab list.
 */
public class TabListEvent extends Event {

	private static final Map<UUID, IChatComponent> cachedNames = new HashMap<>();

	public static void updatePlayerInfoList() {
		if (mc().getNetHandler() == null)
			return;

		for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
			IChatComponent originalComponent = cachedNames.get(info.getGameProfile().getId());
			TabListNameUpdateEvent event = new TabListNameUpdateEvent(info.getGameProfile(), originalComponent);
			EVENT_BUS.post(event);
			info.setDisplayName(event.component);
		}
	}

	/**
	 * An event being posted when a tab list entry updates its name.
	 */
	public static class TabListNameUpdateEvent extends TabListEvent {

		public final GameProfile profile;
		public IChatComponent component;

		private TabListNameUpdateEvent(GameProfile profile, IChatComponent component) {
			this.profile = profile;
			this.component = component;
		}

		@EventListener
		private static void onPacket(PacketReceiveEvent event) {
			if (!(event.packet instanceof S38PacketPlayerListItem))
				return;

			// Ignore packets not updating name
			S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.packet;
			if (packet.func_179768_b() != ADD_PLAYER && packet.func_179768_b() != UPDATE_DISPLAY_NAME)
				return;

			for (S38PacketPlayerListItem.AddPlayerData data : packet.func_179767_a()) {
				if (data.getDisplayName() == null)
					continue;

				// Post TabListNameUpdateEvent
				TabListNameUpdateEvent tabListEvent = new TabListNameUpdateEvent(data.getProfile(), data.getDisplayName());
				EVENT_BUS.post(tabListEvent);

				// Update values
				cachedNames.put(data.getProfile().getId(), data.getDisplayName());
				Reflection.set(data, tabListEvent.component, "displayName", "field_179965_e", "e");
			}
		}

	}

}
