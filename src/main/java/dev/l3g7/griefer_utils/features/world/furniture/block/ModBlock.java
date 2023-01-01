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

package dev.l3g7.griefer_utils.features.world.furniture.block;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.world.furniture.ModBlocks;
import dev.l3g7.griefer_utils.features.world.furniture.block.fence.UpgradedFenceBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.fence.UpgradedGateBlock;
import dev.l3g7.griefer_utils.features.world.furniture.block.small.HedgeBlock;
import dev.l3g7.griefer_utils.features.world.furniture.properties.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Collections;
import java.util.List;

public abstract class ModBlock {
	public static final List<AxisAlignedBB> DEFAULT_SHAPES = Collections.singletonList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
	private final BlockProperties blockProperties;
	private Block blockHandle;
	private IBlockState defaultState;
	private JsonObject blockData = new JsonObject();
	private String versionBlockClass;
	private ModBlocks.BlockKey blockKey;
	private boolean isFullCube;
	private boolean waterlogged;
	private boolean cutout;
	private boolean climbable;
	private boolean trapDoor;
	private boolean canSustainBush;
	private float lightLevel;

	public ModBlock(BlockProperties blockProperties) {
		this.blockProperties = blockProperties;
	}

	public void initBlockData(JsonObject blockData) {
		this.waterlogged = blockData.has("waterlogged") && blockData.get("waterlogged").getAsBoolean();
		this.cutout = blockData.has("cutout") && blockData.get("cutout").getAsBoolean();
		this.climbable = blockData.has("climbable") && blockData.get("climbable").getAsBoolean();
		this.trapDoor = blockData.has("trapDoor") && blockData.get("trapDoor").getAsBoolean();
		this.canSustainBush = blockData.has("canSustainBush") && blockData.get("canSustainBush").getAsBoolean();
		this.lightLevel = blockData.has("lightlevel") ? blockData.get("lightlevel").getAsFloat() : 0.0f;
	}

	public abstract void addProperties(List<IProperty<?>> properties);

	public abstract IBlockState getDefaultState(IBlockState defaultState);

	public IBlockState getActualState(IBlockState state, BlockPos blockPosition, IBlockAccess world) {
		return state;
	}

	public boolean isValidPosition(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPosition) {
		return true;
	}

	public abstract boolean isTransparent(IBlockState blockState);

	public int getMetaFromState(IBlockState blockState) {
		return 0;
	}

	public IBlockState getStateFromMeta(int meta) {
		return this.defaultState;
	}

	public boolean hasItem() {
		return true;
	}

	public ModBlock getBlockToSupplyToItem() {
		return this;
	}

	public boolean isWaterlogged() {
		return this.waterlogged;// || this instanceof WaterloggedBlock;
	}

	public boolean isHighCollisionBlock() {
		return this instanceof UpgradedFenceBlock || this instanceof UpgradedGateBlock || this instanceof HedgeBlock;
	}

	public List<AxisAlignedBB> getShapes(IBlockState blockState, List<AxisAlignedBB> providedShapes) {
		return providedShapes;
	}

	public List<AxisAlignedBB> getCollisionShapes(IBlockState blockState, List<AxisAlignedBB> providedShapes) {
		return providedShapes;
	}

	public BlockProperties getBlockProperties() {
		return this.blockProperties;
	}

	public Block getBlockHandle() {
		return this.blockHandle;
	}

	public IBlockState getDefaultState() {
		return this.defaultState;
	}

	public JsonObject getBlockData() {
		return this.blockData;
	}

	public String getVersionBlockClass() {
		return this.versionBlockClass;
	}

	public ModBlocks.BlockKey getBlockKey() {
		return this.blockKey;
	}

	public boolean isFullCube() {
		return this.isFullCube;
	}

	public boolean isCutout() {
		return this.cutout;
	}

	public boolean isClimbable() {
		return this.climbable;
	}

	public boolean isTrapDoor() {
		return this.trapDoor;
	}

	public boolean isCanSustainBush() {
		return this.canSustainBush;
	}

	public float getLightLevel() {
		return this.lightLevel;
	}

	public void setBlockHandle(Block blockHandle) {
		this.blockHandle = blockHandle;
	}

	public void setDefaultState(IBlockState defaultState) {
		this.defaultState = defaultState;
	}

	public void setBlockData(JsonObject blockData) {
		this.blockData = blockData;
	}

	public void setVersionBlockClass(String versionBlockClass) {
		this.versionBlockClass = versionBlockClass;
	}

	public void setBlockKey(ModBlocks.BlockKey blockKey) {
		this.blockKey = blockKey;
	}

	public void setFullCube(boolean isFullCube) {
		this.isFullCube = isFullCube;
	}

	public void setWaterlogged(boolean waterlogged) {
		this.waterlogged = waterlogged;
	}

	public void setCutout(boolean cutout) {
		this.cutout = cutout;
	}

	public void setClimbable(boolean climbable) {
		this.climbable = climbable;
	}

	public void setTrapDoor(boolean trapDoor) {
		this.trapDoor = trapDoor;
	}

	public void setCanSustainBush(boolean canSustainBush) {
		this.canSustainBush = canSustainBush;
	}

	public void setLightLevel(float lightLevel) {
		this.lightLevel = lightLevel;
	}
}
