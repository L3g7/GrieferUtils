/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Cancelable
public class PlaySoundEvent extends Event {

	public final String name;

	public PlaySoundEvent(ISound sound) {
		this.name = sound.getSoundLocation().getResourcePath();
	}

	@Mixin(SoundManager.class)
	private static class MixinSoundManager {

		@Shadow
		private boolean loaded;

		@Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
	    public void injectPlaySound(ISound p_sound, CallbackInfo ci) {
	    	if (loaded && MinecraftForge.EVENT_BUS.post(new PlaySoundEvent(p_sound)))
				ci.cancel();
	    }

	}

}
