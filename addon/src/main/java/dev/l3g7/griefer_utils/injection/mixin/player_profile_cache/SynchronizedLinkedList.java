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

package dev.l3g7.griefer_utils.injection.mixin.player_profile_cache;

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
