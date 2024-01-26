/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc;

import net.minecraft.util.Vec3;

public class Vec3d extends Vec3 {

	public final double x, y, z;

	public Vec3d(double x, double y, double z) {
		super(x, y, z);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3d add(float value) {
		return add(value, value, value);
	}

	public Vec3d add(Vec3d value) {
		return add(value.x, value.y, value.z);
	}

	public Vec3d add(double x, double y, double z) {
		return new Vec3d(this.x + x, this.y + y, this.z + z);
	}

	public Vec3d subtract(Vec3d other) {
		return subtract(other.x, other.y, other.z);
	}

	public Vec3d subtract(double value) {
		return subtract(value, value, value);
	}

	public Vec3d subtract(double x, double y, double z) {
		return add(-x, -y, -z);
	}

	public Vec3d scale(double factor) {
		return new Vec3d(x * factor, y * factor, z * factor);
	}

	public Vec3d normalize() {
		return scale(1 / length());
	}

	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}

}
