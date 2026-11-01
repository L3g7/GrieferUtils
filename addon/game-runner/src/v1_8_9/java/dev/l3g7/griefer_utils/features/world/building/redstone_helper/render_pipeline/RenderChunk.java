package dev.l3g7.griefer_utils.features.world.building.redstone_helper.render_pipeline;

import dev.l3g7.griefer_utils.features.world.building.redstone_helper.RenderObject;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class RenderChunk {

	public static void onSettingsChange() {
//			for (CompiledChunk chunk : data.values())
//				chunk.partsToRecompile.addAll(chunk.parts.values());
	}

	private final ChunkPart[] parts = new ChunkPart[16];

	private ChunkPart getPartFor(BlockPos pos) {
		return parts[pos.getY() / 16];
	}

	public void add(RenderObject renderObject) {
		int index = renderObject.pos.getY() / 16;
		ChunkPart part = parts[index];
		if (part == null)
			part = parts[index] = new ChunkPart();

		part.add(renderObject);
	}

	public void remove(BlockPos pos) {
		synchronized (parts) {
			int index = pos.getY() / 16;

			ChunkPart part = parts[index];
			if (part != null && part.remove(pos))
				parts[index] = null;
		}
	}

	public void delete() {
		synchronized (parts) {
			for (ChunkPart part : parts)
				if (part != null)
					part.delete();
		}
	}

	public boolean hasData() {
		synchronized (parts) {
			for (ChunkPart compiledPart : parts)
				if (compiledPart != null)
					return true;

			return false;
		}
	}

	public void draw(ChunkCoordIntPair pair, Frustum frustum, int rotation) {
		synchronized (parts) {
			for (int i = 0; i < parts.length; i++) {
				ChunkPart part = parts[i];
				if (part == null)
					continue;

				boolean isVisible = frustum.isBoundingBoxInFrustum(new AxisAlignedBB(
					pair.getXStart(),
					i << 4,
					pair.getZStart(),
					pair.getXEnd(),
					i << 4 | 15,
					pair.getZEnd()));

				if (!isVisible)
					continue;

				if (part.mustRecompile) {
					part.recompile();
				} else if (part.list == 0) {
					parts[i] = null;
					return;
				}

				mc().getTextureManager().bindTexture(new ResourceLocation("textures/font/ascii.png"));
				GL11.glCallList(part.list + rotation);
			}
		}
	}

	private static class ChunkPart {

		private static final ChunkCompiler COMPILER = new ChunkCompiler(80 * 256);

		int list = 0;
		boolean mustRecompile = false;
		private final Map<BlockPos, RenderObject> renderObjects = new HashMap<>();

		public synchronized void add(RenderObject newRenderObject) {
			RenderObject oldRenderObject = this.renderObjects.put(newRenderObject.pos, newRenderObject);

			if (oldRenderObject == null || !oldRenderObject.equals(newRenderObject))
				this.mustRecompile = true;
		}

		/**
		 * @return Whether this part is empty after removing the position.
		 */
		public synchronized boolean remove(BlockPos pos) {
			if (renderObjects.remove(pos) != null)
				mustRecompile = true;

			return renderObjects.isEmpty();
		}

		public synchronized boolean hasData() {
			return !renderObjects.isEmpty();
		}

		private synchronized void recompile() {
			if (renderObjects.isEmpty()) {
				delete();
				return;
			}

			if (list != 0)
				delete();

			if (list == 0)
				list = GL11.glGenLists(16);

			if (list == 0)
				return; // glGenLists failed

			for (int rotation = 0; rotation < 16; rotation++) {
				COMPILER.reset();
				for (RenderObject ro : renderObjects.values())
					ro.draw(COMPILER, rotation);

				GL11.glNewList(list + rotation, GL11.GL_COMPILE);
				COMPILER.draw();
				GL11.glEndList();
			}

			mustRecompile = false;
		}

		private synchronized void delete() {
			mustRecompile = true;
			if (list == 0)
				return;

			GL11.glDeleteLists(list, 16);
			list = 0;
		}

	}

}
