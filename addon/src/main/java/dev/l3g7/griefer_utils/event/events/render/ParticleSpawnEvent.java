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

import dev.l3g7.griefer_utils.core.misc.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ParticleSpawnEvent extends Event {

	public final int particleID;
	public final boolean ignoreRange;
	public final Vec3d position;
	public final Vec3d offset;
	public final int[] args;

	public ParticleSpawnEvent(int particleID, boolean ignoreRange, Vec3d position, Vec3d offset, int[] args) {
		this.particleID = particleID;
		this.ignoreRange = ignoreRange;
		this.position = position;
		this.offset = offset;
		this.args = args;
	}

}