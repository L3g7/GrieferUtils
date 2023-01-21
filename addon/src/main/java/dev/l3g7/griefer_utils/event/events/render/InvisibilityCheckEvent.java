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

package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.injection.mixin.MixinEntity;
import dev.l3g7.griefer_utils.injection.mixin.MixinEntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * An event being posted when {@link Entity#isBurning()} is called.
 */
public class InvisibilityCheckEvent extends Event {

	public final Entity entity;

	private InvisibilityCheckEvent(Entity entity) {
		this.entity = entity;
	}

	@Override
	public boolean isCancelable() {
		return true;
	}

	/**
	 * Triggered by {@link MixinEntity} and {@link MixinEntityPlayer}.
	 */
	public static boolean isInvisible(Entity entity) {
		InvisibilityCheckEvent event = new InvisibilityCheckEvent(entity);
		return !MinecraftForge.EVENT_BUS.post(event);
	}

}
