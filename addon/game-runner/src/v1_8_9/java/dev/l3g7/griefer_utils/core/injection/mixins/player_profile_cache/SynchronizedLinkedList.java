/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.injection.mixins.player_profile_cache;

import com.mojang.authlib.GameProfile;

import java.util.LinkedList;

/**
 * A LinkedList with synchronized remove and addFirst methods.
 * Should only be used by {@link MixinPlayerProfileCache}.
 */
public class SynchronizedLinkedList extends LinkedList<GameProfile> {

	@Override
	public boolean remove(Object o) {
		synchronized (this) {
			return super.remove(o);
		}
	}

	@Override
	public void addFirst(GameProfile gameProfile) {
		synchronized (this) {
			super.addFirst(gameProfile);
		}

	}
}
