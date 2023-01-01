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

package dev.l3g7.griefer_utils.features.world.furniture.block.version_specific;

import dev.l3g7.griefer_utils.features.world.furniture.OptifineBlockAccess;
import dev.l3g7.griefer_utils.features.world.furniture.block.FurnitureHorizontalBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.ModBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.plants.LogBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.type.ItemOnlyBlock;
import dev.l3g7.griefer_utils.features.world.furniture.blockshapes.BlockShape;
import dev.l3g7.griefer_utils.features.world.furniture.blockshapes.BlockShapesLoader;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.*;

public class VersionBlock extends Block {

	private static ModBlock registeringBlock;

	protected final ModBlock modBlock;
	private final Map<IBlockState, List<AxisAlignedBB>> collisionShapes = new HashMap<>();
	private final Map<IBlockState, AxisAlignedBB> mainBoundingBoxes = new HashMap<>();
	private final Map<String, IBlockState> validBlockStates = new HashMap<>();

	public VersionBlock(ModBlock modBlock) {
		super(modBlock.getBlockProperties().material, modBlock.getBlockProperties().materialColor);
		this.modBlock = modBlock;
		BlockProperties prop = modBlock.getBlockProperties();
		setStepSound(prop.soundType);
		setHardness(prop.hardness);
		setResistance(prop.resistance);
		setDefaultState(modBlock.getDefaultState(getBlockState().getBaseState()));
		modBlock.setDefaultState(getDefaultState());
	}

	public void addShapes() {
		Map<String, BlockShape> shapes = BlockShapesLoader.READ_SHAPES.getShapes(this.modBlock.getBlockKey().getLocation());

		for (IBlockState state : this.getBlockState().getValidStates()) {
			String shapeKey = this.getShapeKey(state);
			this.validBlockStates.put(shapeKey, state);

			BlockShape blockShape = shapes.getOrDefault(shapeKey, BlockShape.DEFAULT_SHAPE);
			List<AxisAlignedBB> boundingBoxes = this.modBlock.getShapes(state, blockShape.getShape());
			this.mainBoundingBoxes.put(state, this.getFullBoundingBox(boundingBoxes));

			List<AxisAlignedBB> collisionShapes = blockShape.isHasCollisionShape()
					? this.modBlock.getCollisionShapes(state, blockShape.getCollisionShapes())
					: this.modBlock.getShapes(state, blockShape.getShape());
			this.collisionShapes.put(state, collisionShapes);
		}
	}

	private String getShapeKey(IBlockState state) {
		return state.toString().replace(this.modBlock.getBlockKey().getLocation(), "").replace("[", "").replace("]", "");
	}

	private AxisAlignedBB getFullBoundingBox(List<AxisAlignedBB> boundingBoxes) {
		if (boundingBoxes.size() == 0)
			return new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

		Double minX = null;
		Double minY = null;
		Double minZ = null;
		Double maxX = null;
		Double maxY = null;
		Double maxZ = null;

		for (AxisAlignedBB boundingBox : boundingBoxes) {
			if (minX == null || boundingBox.minX < minX) minX = boundingBox.minX;
			if (minY == null || boundingBox.minY < minY) minY = boundingBox.minY;
			if (minZ == null || boundingBox.minZ < minZ) minZ = boundingBox.minZ;
			if (maxX == null || boundingBox.maxX > maxX) maxX = boundingBox.maxX;
			if (maxY == null || boundingBox.maxY > maxY) maxY = boundingBox.maxY;
			if (maxZ != null && !(boundingBox.maxZ > maxZ)) continue;
			maxZ = boundingBox.maxZ;
		}

		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	protected BlockState createBlockState() {
		List<IProperty<?>> list = new ArrayList<>();
		getCurrentBlockData().addProperties(list);

		return new BlockState(this, list.toArray(new IProperty[0]));
	}

	public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos blockPos) {
		IBlockState iBlockState = this.getActualState(world.getBlockState(blockPos), world, blockPos);
		AxisAlignedBB axisAlignedBB = this.mainBoundingBoxes.get(iBlockState);
		return axisAlignedBB != null
				? this.calculateBoundingBox(blockPos, axisAlignedBB)
				: this.calculateBoundingBox(blockPos, new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
	}

	public void addCollisionBoxesToList(World world, BlockPos blockPos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entity) {
		state = this.getActualState(state, world, blockPos);
		List<AxisAlignedBB> boxes = this.collisionShapes.getOrDefault(state, Collections.singletonList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)));
		boxes.forEach(box -> {
			AxisAlignedBB boundingBox = this.calculateBoundingBox(blockPos, box);
			if (!mask.intersectsWith(boundingBox))
				return;
			list.add(boundingBox);
		});
	}

	public void setBlockBoundsBasedOnState(IBlockAccess iBlockAccess, BlockPos blockPos) {
		if (!(iBlockAccess instanceof World))
			return;

		AxisAlignedBB axisAlignedBB = this.getSelectedBoundingBox((World)iBlockAccess, blockPos);
		this.minX = axisAlignedBB.minX - (double)blockPos.getX();
		this.minY = axisAlignedBB.minY - (double)blockPos.getY();
		this.minZ = axisAlignedBB.minZ - (double)blockPos.getZ();
		this.maxX = axisAlignedBB.maxX - (double)blockPos.getX();
		this.maxY = axisAlignedBB.maxY - (double)blockPos.getY();
		this.maxZ = axisAlignedBB.maxZ - (double)blockPos.getZ();
	}

	public IBlockState onBlockPlaced(World world, BlockPos blockPos, EnumFacing enumFacing, float f, float f2, float f3, int n, EntityLivingBase entityLivingBase) {
		IBlockState iBlockState = super.onBlockPlaced(world, blockPos, enumFacing, f, f2, f3, n, entityLivingBase);

		if (modBlock instanceof FurnitureHorizontalBlock)
			iBlockState = iBlockState.withProperty(FurnitureHorizontalBlock.DIRECTION, entityLivingBase.getHorizontalFacing());
		if (modBlock instanceof LogBlock)
			iBlockState = iBlockState.withProperty(LogBlock.LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(enumFacing.getAxis()));

		return iBlockState;
	}

	public IBlockState getActualState(IBlockState iBlockState, IBlockAccess iBlockAccess, BlockPos blockPos) {
		IBlockAccess minecraftBlockAccess = iBlockAccess.getClass().getName().equals("net.optifine.override.ChunkCacheOF") || iBlockAccess.getClass().getName().equals("ChunkCacheOF") ? new OptifineBlockAccess(iBlockAccess) : iBlockAccess;
		return this.getCurrentBlockData().getActualState(iBlockState, blockPos, minecraftBlockAccess);
	}

	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	public IBlockState getStateFromMeta(int n) {
		return modBlock.getStateFromMeta(n);
	}

	public int getMetaFromState(IBlockState iBlockState) {
		return modBlock.getMetaFromState(iBlockState);
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public boolean isFullCube() {
		return modBlock.isFullCube();
	}

	public boolean isFence() {
		return false;
	}

	protected ModBlock getCurrentBlockData() {
		return this.modBlock != null ? this.modBlock : registeringBlock;
	}

	protected AxisAlignedBB calculateBoundingBox(BlockPos blockPos, AxisAlignedBB axisAlignedBB) {
		return new AxisAlignedBB((double)blockPos.getX() + axisAlignedBB.minX, (double)blockPos.getY() + axisAlignedBB.minY, (double)blockPos.getZ() + axisAlignedBB.minZ, (double)blockPos.getX() + axisAlignedBB.maxX, (double)blockPos.getY() + axisAlignedBB.maxY, (double)blockPos.getZ() + axisAlignedBB.maxZ);
	}

	public boolean canPlaceBlockAt(World world, BlockPos blockPos) {
		if (this.modBlock instanceof ItemOnlyBlock)
			return false;
		return super.canPlaceBlockAt(world, blockPos);
	}

	public static ModBlock getRegisteringBlock() {
		return registeringBlock;
	}

	public static void setRegisteringBlock(ModBlock modBlock) {
		registeringBlock = modBlock;
	}

	public ModBlock getModBlock() {
		return this.modBlock;
	}

	public Map<String, IBlockState> getValidBlockStates() {
		return this.validBlockStates;
	}

}
