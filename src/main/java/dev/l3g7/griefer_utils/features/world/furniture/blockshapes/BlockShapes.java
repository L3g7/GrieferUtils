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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class BlockShapes {
	private final Map<String, Map<String, BlockShape>> shapes = new HashMap<String, Map<String, BlockShape>>();

	public BlockShapes clone() {
		BlockShapes newShapes = new BlockShapes();
		for (Map.Entry<String, Map<String, BlockShape>> shapeEntry : this.shapes.entrySet()) {
			newShapes.getShapes().put(shapeEntry.getKey(), BlockShapes.cloneBlocksShapes(shapeEntry.getValue()));
		}
		return newShapes;
	}

	public static Map<String, BlockShape> cloneBlocksShapes(Map<String, BlockShape> shapes) {
		HashMap<String, BlockShape> stateShapes = new HashMap<String, BlockShape>();
		for (Map.Entry<String, BlockShape> stringBlockShapeEntry : shapes.entrySet()) {
			stateShapes.put(stringBlockShapeEntry.getKey(), stringBlockShapeEntry.getValue().clone());
		}
		return stateShapes;
	}

	public Map<String, BlockShape> getShapes(String block) {
		return this.shapes.getOrDefault(block, Collections.emptyMap());
	}

	public void addShape(String block, String state, List<AxisAlignedBB> shape, boolean hasCollisionShape, List<AxisAlignedBB> collisionShape) {
		this.shapes.computeIfAbsent(block, key -> new HashMap<>()).put(state, new BlockShape(shape, hasCollisionShape, collisionShape));
	}

	public void write(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(this.shapes.size());
		for (Map.Entry<String, Map<String, BlockShape>> shapeEntry : this.shapes.entrySet()) {
			dataOutputStream.writeUTF(shapeEntry.getKey());
			dataOutputStream.writeInt(shapeEntry.getValue().size());
			for (Map.Entry<String, BlockShape> stateEntry : shapeEntry.getValue().entrySet()) {
				BlockShape blockShape = stateEntry.getValue();
				dataOutputStream.writeUTF(stateEntry.getKey());
				this.writeShapes(dataOutputStream, blockShape.getShape());
				dataOutputStream.writeBoolean(blockShape.isHasCollisionShape());
				if (!blockShape.isHasCollisionShape()) continue;
				this.writeShapes(dataOutputStream, blockShape.getCollisionShapes());
			}
		}
	}

	private void writeShapes(DataOutputStream dataOutputStream, List<AxisAlignedBB> axisAlignedBBS) throws IOException {
		dataOutputStream.writeInt(axisAlignedBBS.size());
		for (AxisAlignedBB axisAlignedBB : axisAlignedBBS) {
			dataOutputStream.writeDouble(axisAlignedBB.minX);
			dataOutputStream.writeDouble(axisAlignedBB.minY);
			dataOutputStream.writeDouble(axisAlignedBB.minZ);
			dataOutputStream.writeDouble(axisAlignedBB.maxX);
			dataOutputStream.writeDouble(axisAlignedBB.maxY);
			dataOutputStream.writeDouble(axisAlignedBB.maxZ);
		}
	}

	public BlockShapes mergeShapes(BlockShapes otherShapes) {
		this.shapes.putAll(otherShapes.getShapes());
		return this;
	}

	public static BlockShapes read(DataInputStream dataInputStream) throws IOException {
		BlockShapes blockShapes = new BlockShapes();
		int shapesSize = dataInputStream.readInt();
		for (int i = 0; i < shapesSize; ++i) {
			String blockKey = dataInputStream.readUTF().replace("cb:", "griefer_utils:");
			HashMap<String, BlockShape> shapesMap = new HashMap<String, BlockShape>();
			int states = dataInputStream.readInt();
			for (int j = 0; j < states; ++j) {
				String state = dataInputStream.readUTF();
				List<AxisAlignedBB> shapes = BlockShapes.readShapes(dataInputStream);
				boolean hasCollisionShapes = dataInputStream.readBoolean();
				List<AxisAlignedBB> collisionShapes = hasCollisionShapes ? BlockShapes.readShapes(dataInputStream) : null;
				shapesMap.put(state, new BlockShape(shapes, hasCollisionShapes, collisionShapes));
			}
			blockShapes.getShapes().put(blockKey, shapesMap);
		}
		return blockShapes;
	}

	private static List<AxisAlignedBB> readShapes(DataInputStream dataInputStream) throws IOException {
		ArrayList<AxisAlignedBB> shapes = new ArrayList<AxisAlignedBB>();
		int size = dataInputStream.readInt();
		for (int i = 0; i < size; ++i) {
			AxisAlignedBB axisAlignedBB = new AxisAlignedBB(dataInputStream.readDouble(), dataInputStream.readDouble(), dataInputStream.readDouble(), dataInputStream.readDouble(), dataInputStream.readDouble(), dataInputStream.readDouble());
			shapes.add(axisAlignedBB);
		}
		return shapes;
	}

	public Map<String, Map<String, BlockShape>> getShapes() {
		return this.shapes;
	}

}
