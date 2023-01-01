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

package dev.l3g7.griefer_utils.features.world.furniture.blockshapes;

import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockShape {
	public static BlockShape DEFAULT_SHAPE = new BlockShape(Collections.singletonList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)), false, null);
	private List<AxisAlignedBB> shape;
	private boolean hasCollisionShape;
	private List<AxisAlignedBB> collisionShapes;

	public BlockShape clone() {
		return new BlockShape(BlockShape.clone(this.shape), this.hasCollisionShape, BlockShape.clone(this.collisionShapes));
	}

	public static List<AxisAlignedBB> clone(List<AxisAlignedBB> input) {
		if (input == null) {
			return new ArrayList<AxisAlignedBB>();
		}
		ArrayList<AxisAlignedBB> output = new ArrayList<AxisAlignedBB>();
		for (AxisAlignedBB inputBox : input) {
			output.add(new AxisAlignedBB(inputBox.minX, inputBox.minY, inputBox.minZ, inputBox.maxX, inputBox.maxY, inputBox.maxZ));
		}
		return output;
	}

	public List<AxisAlignedBB> getShape() {
		return this.shape;
	}

	public boolean isHasCollisionShape() {
		return this.hasCollisionShape;
	}

	public List<AxisAlignedBB> getCollisionShapes() {
		return this.collisionShapes;
	}

	public void setShape(List<AxisAlignedBB> shape) {
		this.shape = shape;
	}

	public void setHasCollisionShape(boolean hasCollisionShape) {
		this.hasCollisionShape = hasCollisionShape;
	}

	public void setCollisionShapes(List<AxisAlignedBB> collisionShapes) {
		this.collisionShapes = collisionShapes;
	}

	public BlockShape(List<AxisAlignedBB> shape, boolean hasCollisionShape, List<AxisAlignedBB> collisionShapes) {
		this.shape = shape;
		this.hasCollisionShape = hasCollisionShape;
		this.collisionShapes = collisionShapes;
	}

}