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

package dev.l3g7.griefer_utils.event.events.network;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import dev.l3g7.griefer_utils.util.misc.PlayerDataProvider;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S38PacketPlayerListItem.AddPlayerData;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.minecraft.network.play.server.S38PacketPlayerListItem.Action.*;
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

			// create full deep-copy of component
			TabListNameUpdateEvent event = new TabListNameUpdateEvent(info.getGameProfile(), originalComponent);
			EVENT_BUS.post(event);
			info.setDisplayName(event.component);
		}
	}

	public static IChatComponent getCachedName(UUID uuid) {
		return cachedNames.get(uuid);
	}

	/**
	 * An event being posted when a tab list entry updates its name.
	 */
	public static class TabListNameUpdateEvent extends TabListEvent {

		public final GameProfile profile;
		public IChatComponent component;

		private TabListNameUpdateEvent(GameProfile profile, IChatComponent component) {
			this.profile = profile;
			this.component = component.createCopy();
			deepCopyStyle(this.component);
		}

		private void deepCopyStyle(IChatComponent component) {
			component.setChatStyle(component.getChatStyle().createDeepCopy());
			component.getSiblings().forEach(this::deepCopyStyle);
		}

		@EventListener
		private static void onPacket(PacketReceiveEvent event) {
			if (!(event.packet instanceof S38PacketPlayerListItem))
				return;

			// Ignore packets not updating name
			S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.packet;
			if (packet.getAction() != ADD_PLAYER && packet.getAction() != UPDATE_DISPLAY_NAME)
				return;

			for (AddPlayerData data : packet.getEntries()) {
				if (data.getDisplayName() == null)
					continue;

				// Post TabListNameUpdateEvent
				TabListNameUpdateEvent tabListEvent = new TabListNameUpdateEvent(data.getProfile(), data.getDisplayName());
				EVENT_BUS.post(tabListEvent);

				// Update values
				cachedNames.put(data.getProfile().getId(), data.getDisplayName());
				Reflection.set(data, tabListEvent.component, "displayName");
			}
		}

	}

	/**
	 * An event being posted when a player is added to the tab list.
	 */
	public static class TabListPlayerAddEvent extends TabListEvent {

		public final AddPlayerData data;

		public TabListPlayerAddEvent(AddPlayerData data) {
			this.data = data;
		}

		@EventListener
		private static void onPacket(PacketReceiveEvent event) {
			if (!(event.packet instanceof S38PacketPlayerListItem))
				return;

			S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.packet;
			if (packet.getAction() != ADD_PLAYER)
				return;

			for (AddPlayerData data : packet.getEntries()) {
				if (PlayerUtil.isValid(data.getProfile().getName()))
					EVENT_BUS.post(new TabListPlayerAddEvent(data));
			}

		}

	}

	/**
	 * An event being posted when a player is removed from the tab list.
	 */
	public static class TabListPlayerRemoveEvent extends TabListEvent {

		public final AddPlayerData data;
		public final String cachedName;

		public TabListPlayerRemoveEvent(AddPlayerData data, String cachedName) {
			this.data = data;
			this.cachedName = cachedName;
		}

		@EventListener
		private static void onPacket(PacketReceiveEvent event) {
			if (!(event.packet instanceof S38PacketPlayerListItem))
				return;

			S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.packet;
			if (packet.getAction() != REMOVE_PLAYER)
				return;

			if (packet.getEntries().size() == mc().getNetHandler().getPlayerInfoMap().size())
				// When whole TabList is affected, TabListClearEvent is posted instead
				return;

			for (AddPlayerData data : packet.getEntries()) {
				String name = PlayerDataProvider.get(data.getProfile().getId()).getName();
				if (PlayerUtil.isValid(name))
					EVENT_BUS.post(new TabListPlayerRemoveEvent(data, name));
			}
		}

	}

	/**
	 * An event being posted when the tab list is cleared.
	 */
	public static class TabListClearEvent extends TabListEvent {

		public final ImmutableMap<AddPlayerData, String> entries;

		public TabListClearEvent(List<AddPlayerData> entries) {
			Map<AddPlayerData, String> namedEntries = new HashMap<>();

			for (AddPlayerData data : entries) {
				String name = PlayerDataProvider.get(data.getProfile().getId()).getName();
				if (PlayerUtil.isValid(name))
					namedEntries.put(data, name);
			}

			this.entries = ImmutableMap.copyOf(namedEntries);
			// Caused by: java.lang.NullPointerException: null value in entry: AddPlayerData{latency=0, gameMode=null, profile=com.mojang.authlib.GameProfile@69e35b4b[id=b48451cf-7ded-3811-9fe5-01793d0ce246,name=<null>,properties={},legacy=false], displayName=null}=null
		}

		@EventListener
		private static void onPacket(PacketReceiveEvent event) {
			if (!(event.packet instanceof S38PacketPlayerListItem))
				return;

			S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.packet;
			if (packet.getAction() != REMOVE_PLAYER)
				return;

			if (packet.getEntries().size() == mc().getNetHandler().getPlayerInfoMap().size())
				EVENT_BUS.post(new TabListClearEvent(packet.getEntries()));
		}

	}

}
