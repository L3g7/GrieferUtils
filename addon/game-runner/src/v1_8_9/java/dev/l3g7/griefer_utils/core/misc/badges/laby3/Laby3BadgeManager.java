/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby3;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.UserSetGroupEvent;
import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import dev.l3g7.griefer_utils.core.misc.badges.Badges.BadgeManager;
import dev.l3g7.griefer_utils.core.misc.gui.elements.Gui;
import net.labymod.core_implementation.mc18.gui.ModPlayerTabOverlay;
import net.labymod.main.LabyMod;
import net.labymod.main.ModSettings;
import net.labymod.user.User;
import net.labymod.user.group.LabyGroup;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showBadges;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class Laby3BadgeManager implements BadgeManager {

	private static final Map<UUID, LabyGroup> onlineUsers = new ConcurrentHashMap<>();

	@Override
	public void setOnline(UUID uuid) {
		onlineUsers.put(uuid, user(uuid).getGroup());
		if (showBadges())
			user(uuid).setGroup(GrieferUtilsGroup.from(uuid));
	}

	@Override
	public void setOffline(UUID uuid) {
		user(uuid).setGroup(onlineUsers.remove(uuid));
	}

	@Override
	public void toggleBadges(boolean enabled) {
		// Sync groups
		if (enabled) {
			for (UUID uuid : onlineUsers.keySet())
				user(uuid).setGroup(GrieferUtilsGroup.from(uuid));
		} else {
			for (UUID uuid : onlineUsers.keySet())
				user(uuid).setGroup(onlineUsers.get(uuid));
		}
	}

	@EventListener
	private void onSetGroup(UserSetGroupEvent event) {
		// Update onlineUsers to store new group
		User user = (User) event.user;
		if (!onlineUsers.containsKey(user.getUuid()) || event.group instanceof GrieferUtilsGroup)
			return;

		if (event.group == null)
			onlineUsers.remove(user.getUuid());
		else
			onlineUsers.put(user.getUuid(), (LabyGroup) event.group);

		if (showBadges())
			event.cancel();
	}

	private User user(UUID uuid) {
		return LabyMod.getInstance().getUserManager().getUser(uuid);
	}

	@ExclusiveTo(LABY_3)
	@Mixin(value = ModPlayerTabOverlay.class, remap = false)
	private static class MixinModPlayerTabOverlay {

		@Unique
		private int grieferUtils$tablistPadding;

		/**
		 * Force padding of name if badges are enabled
		 */
		@Redirect(method = "newTabOverlay", at = @At(value = "FIELD", target = "Lnet/labymod/main/ModSettings;revealFamiliarUsers:Z", ordinal = 1), remap = false)
		private boolean redirectRevealFamiliarUsers(ModSettings instance) {
			return instance.revealFamiliarUsers || showBadges();
		}

		/**
		 * Force padding of name if badges are enabled
		 */
		@Redirect(method = "newTabOverlay", at = @At(value = "INVOKE", target = "Lnet/labymod/user/User;isFamiliar()Z"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/labymod/core_implementation/mc18/gui/ModPlayerTabOverlay;drawRect(IIIII)V", ordinal = 0)))
		private boolean redirectIsFamiliar(User instance) {
			if (showBadges() && instance.getGroup() instanceof GrieferUtilsGroup)
				return true;

			return LabyMod.getSettings().revealFamiliarUsers && instance.isFamiliar();
		}

		@Redirect(method = "newTabOverlay", at = @At(value = "INVOKE", target = "Lnet/labymod/core_implementation/mc18/gui/ModPlayerTabOverlay;drawRect(IIIII)V", ordinal = 1), require = 1, remap = true)
		private void redirectDrawRect(int left, int top, int right, int bottom, int color) {
			this.grieferUtils$tablistPadding = left;
			Gui.drawRect(left, top, right, bottom, color);
		}

		@Inject(method = "newTabOverlay", at = @At("TAIL"), remap = false)
		public void injectNewTabOverlay(int screenWidth, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
			Badges.renderUserPercentage(screenWidth - grieferUtils$tablistPadding);
		}

	}

}
