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

package dev.l3g7.griefer_utils.util.misc;

public class Vec3f {

	public float x, y, z;

	public Vec3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3f(double x, double y, double z) {
		this((float) x, (float) y, (float) z);
	}

	public Vec3f add(Vec3f other) {
		x += other.x;
		y += other.y;
		z += other.z;
		return this;
	}

	public Vec3f plus(Vec3f other) {
		return new Vec3f(x + other.x, y + other.y, z + other.z);
	}

	public Vec3f minus(Vec3f other) {
		return new Vec3f(x - other.x, y - other.y, z - other.z);
	}

	public Vec3f multiply(float factor) {
		x *= factor;
		y *= factor;
		z *= factor;
		return this;
	}

}
