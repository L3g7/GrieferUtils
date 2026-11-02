package dev.l3g7.griefer_utils.features.world.building.redstone_helper;

import dev.l3g7.griefer_utils.core.util.render.render_engine.ChunkCompiler;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderObject;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Vertices;
import net.minecraft.util.BlockPos;

class RotatingTextRenderObject extends RenderObject {

	private final String text;

	public RotatingTextRenderObject(String text) {
		this.text = text;
	}

	@Override
	public boolean equals(RenderObject r) {
		return r instanceof RotatingTextRenderObject rot && text.equals(rot.text);
	}

	@Override
	public void draw(ChunkCompiler compiler, BlockPos pos, int rotation) {
		double x = (double) pos.getX() + 0.5d;
		double y = (double) pos.getY() + 0.02d;
		double z = (double) pos.getZ() + 0.5d;

		for (FontRenderData.CharRenderInfo renderInfo : FontRenderData.getCharRenderInfos(text)) {
			Vertices offsets = renderInfo.rotatedVertices()[rotation];
			compiler.render(renderInfo.texture(), offsets.translate(x, y, z));
		}
	}
}
