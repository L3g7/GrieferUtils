/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby3;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.events.WebDataReceiveEvent;
import dev.l3g7.griefer_utils.core.injection.InheritedInvoke;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.UserSetGroupEvent;
import dev.l3g7.griefer_utils.features.uncategorized.settings.credits.Credits;
import dev.l3g7.griefer_utils.core.misc.badges.BadgeManagerBridge;
import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import dev.l3g7.griefer_utils.core.misc.gui.elements.Gui;
import dev.l3g7.griefer_utils.core.misc.server.GUClient;
import io.netty.util.internal.ConcurrentSet;
import net.labymod.core_implementation.mc18.gui.ModPlayerTabOverlay;
import net.labymod.main.LabyMod;
import net.labymod.main.ModSettings;
import net.labymod.user.User;
import net.labymod.user.group.LabyGroup;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.misc.badges.Badges.showBadges;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class Laby3BadgeManagerBridge implements BadgeManagerBridge {

	private static final Map<UUID, LabyGroup> users = new ConcurrentHashMap<>();

	private static long lastRequest = 0;
	private static final Map<UUID, GrieferUtilsGroup> specialBadges = new HashMap<>();
	private static final Set<UUID> queuedUsers = new ConcurrentSet<>();

	public boolean isSpecial(String uuid) {
		return specialBadges.containsKey(UUID.fromString(uuid));
	}

	public void queueUser(UUID uuid) {
		queuedUsers.add(uuid);
	}

	public void removeUser(UUID uuid) {
		user(uuid).setGroup(users.remove(uuid));
	}

	public void clearUsers() {
		for (UUID uuid : users.keySet())
			removeUser(uuid);
	}

	private static User user(UUID uuid) {
		return LabyMod.getInstance().getUserManager().getUser(uuid);
	}

	@EventListener
	private static void onTick(TickEvent.ClientTickEvent event) {
		if (lastRequest + 2500 <= System.currentTimeMillis())
			requestQueuedUsers();
	}

	@EventListener
	private static void onSetGroup(UserSetGroupEvent event) {
		User user = (User) event.user;
		if (!users.containsKey(user.getUuid()) || event.group instanceof GrieferUtilsGroup)
			return;

		if (event.group == null)
			users.remove(user.getUuid());
		else
			users.put(user.getUuid(), (LabyGroup) event.group);

		event.cancel();
	}

	private static void requestQueuedUsers() {
		if (queuedUsers.isEmpty() || !GUClient.get().isAvailable())
			return;

		lastRequest = System.currentTimeMillis();

		Set<UUID> requestedUsers = new ConcurrentSet<>();
		requestedUsers.addAll(queuedUsers);
		queuedUsers.removeAll(requestedUsers);

		CompletableFuture.supplyAsync((Supplier<List<UUID>>) () -> GUClient.get().getOnlineUsers(requestedUsers)).thenAccept(uuids -> {
			for (UUID uuid : uuids) {
				users.put(uuid, user(uuid).getGroup());
				user(uuid).setGroup(specialBadges.getOrDefault(uuid, new GrieferUtilsGroup()));
			}
		});
	}

	@EventListener
	private static void onWebData(WebDataReceiveEvent event) {
		event.data.specialBadges.forEach((k, v) -> specialBadges.put(k, new GrieferUtilsGroup(v)));
		Credits.addTeam();
	}

	public static void renderUserPercentage(int left, int width) {
		if (!showBadges() || !Badges.showPercentage.get())
			return;

		int x = width - left;

		int totalCount = mc().getNetHandler().getPlayerInfoMap().size();
		int familiarCount = 0;

		for (NetworkPlayerInfo npi : mc().getNetHandler().getPlayerInfoMap())
			if (LabyMod.getInstance().getUserManager().getUser(npi.getGameProfile().getId()).isFamiliar())
				familiarCount++;

		if (LabyMod.getSettings().revealFamiliarUsers && LabyMod.getSettings().revealFamiliarUsersPercentage) {
			int percent = totalCount == 0 ? 0 : (int) Math.round(familiarCount / (double) totalCount * 100);
			String labyModText = String.format("§7%d§8/§7%d §a%d%%", familiarCount, totalCount, percent);
			double delta = LabyMod.getInstance().getDrawUtils().getStringWidth(labyModText) * 0.7 + 14;
			x -= delta;

			LabyMod.getInstance().getDrawUtils().bindTexture("labymod/textures/labymod_logo.png");
			LabyMod.getInstance().getDrawUtils().drawTexture(x + 6, 2.25, 256, 256, 6.5, 6.5);
		}

		familiarCount = 0;
		for (NetworkPlayerInfo npi : mc().getNetHandler().getPlayerInfoMap())
			if (LabyMod.getInstance().getUserManager().getUser(npi.getGameProfile().getId()).getGroup() instanceof GrieferUtilsGroup)
				familiarCount++;

		int percent = totalCount == 0 ? 0 : (int) Math.round(familiarCount / (double) totalCount * 100);
		String text = GUClient.get().isAvailable() ? String.format("§7%d§8/§7%d §a%d%%", familiarCount, totalCount, percent) : "§c?";
		LabyMod.getInstance().getDrawUtils().drawRightString(text, x, 3, 0.7);

		LabyMod.getInstance().getDrawUtils().bindTexture("griefer_utils/icons/icon.png");
		x -= LabyMod.getInstance().getDrawUtils().getStringWidth(text) * 0.7;
		LabyMod.getInstance().getDrawUtils().drawTexture(x - 8, 2.25, 256, 256, 7, 7);
	}

	@Mixin(value = ModPlayerTabOverlay.class, remap = false)
	@ExclusiveTo(LABY_3)
	private static class MixinModPlayerTabOverlay {

		private int left = -1;

		@Redirect(method = "newTabOverlay", at = @At(value = "FIELD", target = "Lnet/labymod/main/ModSettings;revealFamiliarUsers:Z", ordinal = 1), remap = false)
		private boolean redirectRevealFamiliarUsers(ModSettings instance) {
			return instance.revealFamiliarUsers || showBadges();
		}

		@Redirect(method = "newTabOverlay", at = @At(value = "INVOKE", target = "Lnet/labymod/user/User;isFamiliar()Z"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/labymod/core_implementation/mc18/gui/ModPlayerTabOverlay;drawRect(IIIII)V", ordinal = 0)))
		private boolean redirectIsFamiliar(User instance) {
			if (showBadges() && instance.getGroup() instanceof GrieferUtilsGroup)
				return true;

			return LabyMod.getSettings().revealFamiliarUsers && instance.isFamiliar();
		}

		@InheritedInvoke(net.minecraft.client.gui.Gui.class)
		@Redirect(method = "newTabOverlay", at = @At(value = "INVOKE", target = "Lnet/labymod/core_implementation/mc18/gui/ModPlayerTabOverlay;drawRect(IIIII)V", ordinal = 1), require = 1, remap = true)
		private void redirectDrawRect(int left, int top, int right, int bottom, int color) {
			this.left = left + 1;
			Gui.drawRect(left, top, right, bottom, color);
		}

		@Inject(method = "newTabOverlay", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
		public void injectNewTabOverlay(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
			renderUserPercentage(left, width);
		}

	}

}
