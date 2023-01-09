package dev.l3g7.griefer_utils.features.world.furniture.optifine_patches;

import dev.l3g7.griefer_utils.features.world.furniture.abstraction.BlockBakedModel;
import dev.l3g7.griefer_utils.features.world.furniture.block.version_specific.VersionBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;

public class OptifineBlockModelRenderer {

	public static List<BakedQuad> getQuads(List<BakedQuad> quads, IBakedModel iBakedModel, IBlockState state, BlockPos pos, EnumFacing facing) {
		Block block = state.getBlock();

		if (iBakedModel instanceof BlockBakedModel)
			return ((BlockBakedModel) iBakedModel).getQuads(block.getActualState(state, world(), pos), facing);

		if (block instanceof VersionBlock)
			return mc().getBlockRendererDispatcher().getModelFromBlockState(block.getActualState(state, world(), pos), world(), pos).getFaceQuads(facing);

		return quads;
	}

	public static IBakedModel getModel(IBakedModel model, IBlockState state, BlockPos pos) {
		if (!(state.getBlock() instanceof VersionBlock))
			return model;

		return mc().getBlockRendererDispatcher().getModelFromBlockState(state.getBlock().getActualState(state, world(), pos), world(), pos);
	}

}
