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

package dev.l3g7.griefer_utils.core.injection.mixins.player_profile_cache;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.minecraft.server.management.PlayerProfileCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;

@Mixin(PlayerProfileCache.class)
public abstract class MixinPlayerProfileCache {

	@Shadow
	@Final
	private LinkedList<GameProfile> gameProfiles;

	/**
	 * Fix concurrent modification while accessing the cache.
	 */
	@Inject(method = "load", at = @At(value = "HEAD"))
	public void injectLoad(CallbackInfo ci) {
		Reflection.set(this, "gameProfiles", new SynchronizedLinkedList());
	}

	/**
	 * Fix concurrent modification while accessing the cache.
	 */
	@Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerProfileCache;getEntriesWithLimit(I)Ljava/util/List;"))
	public List<?> redirectIsInGame(PlayerProfileCache instance, int limitSize) {
		//noinspection SynchronizeOnNonFinalField
		synchronized (gameProfiles) {
			return getEntriesWithLimit(limitSize);
		}
	}

	@Shadow
	protected abstract List<?> getEntriesWithLimit(int limitSize);

}
