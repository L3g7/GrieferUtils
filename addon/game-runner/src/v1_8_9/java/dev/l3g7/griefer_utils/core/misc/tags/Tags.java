/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.tags;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.DebounceTimer;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent.TabListClearEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent.TabListPlayerAddEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent.TabListPlayerRemoveEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.features.uncategorized.settings.Badges;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.*;

import static dev.l3g7.griefer_utils.core.misc.tags.Tags.TagManager.tagManager;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showBadges;

public class Tags {

	public record SpecialBadge(String title, int colorWithLabymod, int colorWithoutLabymod) {
		public static final SpecialBadge DEFAULT_BADGE = new SpecialBadge(null, 0xFFFFFF, 0xFFFFFF);
	}

	public static Optional<SpecialBadge> getBadge(UUID uuid) {
		return Optional.ofNullable(SpecialPlayers.specialPlayers.get(uuid));
	}

	public static boolean isOnline(UUID uuid) {
		return OnlineUsers.onlineUsers.contains(uuid);
	}

	/**
	 * GrieferUtils team members.
	 */
	private static class SpecialPlayers {

		private static final HashMap<UUID, SpecialBadge> specialPlayers = new HashMap<>();

		@EventListener
		private static void onStaticData(StaticDataReceiveEvent event) {
			specialPlayers.putAll(event.data.specialBadges);
		}

	}

	/**
	 * GrieferUtils users.
	 */
	private static class OnlineUsers {

		private static final DebounceTimer TIMER = new DebounceTimer("OnlineUsers", 2500);
		private static final Set<UUID> onlineUsers = new ConcurrentSet<>();
		private static final Set<UUID> queuedUsers = new ConcurrentSet<>();

		public static void queueUser(UUID uuid) {
			queuedUsers.add(uuid);
			TIMER.schedule(OnlineUsers::requestQueuedUsers);
		}

		private static void requestQueuedUsers() {
			if (queuedUsers.isEmpty() || !GUServer.isAvailable())
				return;

			Set<UUID> requestedUsers = new HashSet<>(queuedUsers);
			queuedUsers.removeAll(requestedUsers);

			GUServer.getOnlineUsers(requestedUsers).thenAccept(data -> {
				synchronized (onlineUsers) {
					data.forEach(tagManager::setOnline);
					onlineUsers.addAll(data);
				}
			});
		}

		@EventListener
		private static void onTabListAdd(TabListPlayerAddEvent event) {
			queueUser(event.data.getProfile().getId());
		}

		@EventListener
		private static void onTabListRemove(TabListPlayerRemoveEvent event) {
			UUID uuid = event.data.getProfile().getId();
			synchronized (onlineUsers) {
				tagManager.setOffline(uuid);
				onlineUsers.remove(uuid);
			}
		}

		@EventListener
		private static void onTabListClearAdd(TabListClearEvent event) {
			synchronized (onlineUsers) {
				onlineUsers.forEach(tagManager::setOffline);
				onlineUsers.clear();
			}
		}

	}

	public static void renderBadge(SpecialBadge badge, String icon, boolean revealFamiliarUsers, double x, double y) {
		Color color = new Color(revealFamiliarUsers ? badge.colorWithLabymod() : badge.colorWithoutLabymod());

		if (icon.equals("icon"))
			GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1);

		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		DrawUtils.bindTexture(new ResourceLocation("griefer_utils", "icons/" + icon + ".png"));
		DrawUtils.drawTexture(x, y, 255, 255, 8, 8, 1.1f);
		GlStateManager.color(1, 1, 1, 1);
	}

	public static void renderUserPercentage(double rightEnd) {
		if (!showBadges() || !Badges.showPercentage.get())
			return;

		double y = mc().fontRendererObj.FONT_HEIGHT;

		int totalCount = mc().getNetHandler().getPlayerInfoMap().size();
		int userCount = 0;
		for (NetworkPlayerInfo npi : mc().getNetHandler().getPlayerInfoMap())
			if (Tags.isOnline(npi.getGameProfile().getId()))
				userCount++;

		int percent = totalCount == 0 ? 0 : (int) Math.round(userCount / (double) totalCount * 100);
		String text = GUServer.isAvailable() ? String.format("§7%d§8/§7%d §a%d%%", userCount, totalCount, percent) : "§c?";
		DrawUtils.drawRightString(text, rightEnd, 1.5 + y, 0.7);

		DrawUtils.bindTexture(new ResourceLocation("griefer_utils", "icons/icon.png"));
		rightEnd -= mc().fontRendererObj.getStringWidth(text) * 0.7;
		DrawUtils.drawTexture(rightEnd - 8, 1.25 + y, 256, 256, 7, 7);
	}

	@Bridged
	public interface TagManager {

		TagManager tagManager = FileProvider.getBridge(TagManager.class);

		void setOnline(UUID uuid);
		void setOffline(UUID uuid);
		void toggleBadges(boolean enabled);

	}

}
