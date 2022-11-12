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

import dev.l3g7.griefer_utils.mixin.mixins.MixinEntityPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * An event being posted when a player's display name is read.
 */
public class DisplayNameGetEvent extends Event {

	public final EntityPlayer player;
	public IChatComponent displayName;

	private DisplayNameGetEvent(EntityPlayer player, IChatComponent displayName) {
		this.player = player;
		this.displayName = displayName;
	}

	/**
	 * Triggered by {@link MixinEntityPlayer}
	 */
	public static IChatComponent post(EntityPlayer player, IChatComponent displayName) {
		DisplayNameGetEvent event = new DisplayNameGetEvent(player, displayName);
		MinecraftForge.EVENT_BUS.post(event);
		return event.displayName;
	}

}
