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

package dev.l3g7.griefer_utils.injection.mixin.furniture.entitytile;

import dev.l3g7.griefer_utils.features.world.furniture.block.version_specific.BeaconVersionBlock;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBeacon.BeamSegment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(TileEntityBeacon.class)
public abstract class MixinBeaconTileEntity extends TileEntity {

	@Shadow
	public abstract void updateBeacon();

	public void update() {
		IBlockState blockState = getWorld().getBlockState(getPos());
		if (blockState.getBlock() instanceof BeaconVersionBlock)
			((BeaconVersionBlock) blockState.getBlock()).updateColor();

		updateBeacon();
	}

	@Redirect(method = "updateSegmentColors", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
	public boolean redirectUpdateSegmentColors(List<BeamSegment> list, Object object) {
		BeamSegment beamSegment = (BeamSegment) object;
		IBlockState blockState = getWorld().getBlockState(getPos());

		if (blockState.getBlock() instanceof BeaconVersionBlock && list.size() == 0) {
			float[] colors = ((BeaconVersionBlock) blockState.getBlock()).getColor();
			Reflection.set(object, colors, "colors");
		}

		return list.add(beamSegment);
	}

}
