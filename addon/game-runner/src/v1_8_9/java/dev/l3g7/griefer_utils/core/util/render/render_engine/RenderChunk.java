package dev.l3g7.griefer_utils.core.util.render.render_engine;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

class RenderChunk {

	public static void deleteAll(Collection<RenderChunk> renderChunks) {
		renderChunks.removeIf(chunk -> {
			chunk.delete();
			return true;
		});
	}

	private final ChunkPart[] parts = new ChunkPart[16];

	public void add(BlockPos chunkPos, RenderObject renderObject) {
		int index = chunkPos.getY() / 16;
		ChunkPart part = parts[index];
		if (part == null)
			part = parts[index] = new ChunkPart();

		part.add(chunkPos, renderObject);
	}

	public void remove(BlockPos chunkPos) {
		synchronized (parts) {
			int index = chunkPos.getY() / 16;

			ChunkPart part = parts[index];
			if (part != null && part.remove(chunkPos)) {
				part.delete();
				parts[index] = null;
			}
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

	public void checkRecompile(boolean forceRecompilation) {
		synchronized (parts) {
			for (ChunkPart part : parts)
				if (part != null && (part.mustRecompile || forceRecompilation))
					part.recompile();
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

				if (part.list == 0) {
					parts[i] = null;
					return;
				}

				part.compiler.bindTexture();
				GL11.glCallList(part.list + rotation);
			}
		}
	}

	private static class ChunkPart {

		private final ChunkCompiler compiler = new ChunkCompiler();
		private boolean mustRecompile = false;

		int list = 0;
		private final Map<BlockPos, RenderObject> renderObjects = new HashMap<>();

		public synchronized void add(BlockPos pos, RenderObject newRenderObject) {
			RenderObject oldRenderObject = this.renderObjects.put(pos, newRenderObject);

			if (oldRenderObject == null || !oldRenderObject.equals(newRenderObject))
				mustRecompile = true;
		}

		/**
		 * @return Whether this part is empty after removing the position.
		 */
		public synchronized boolean remove(BlockPos pos) {
			if (renderObjects.remove(pos) != null)
				mustRecompile = true;

			return renderObjects.isEmpty();
		}

		private synchronized void recompile() {
			if (renderObjects.isEmpty()) {
				delete();
				return;
			}

			if (list != 0)
				deleteList();

			list = GL11.glGenLists(16);

			if (list == 0)
				return; // glGenLists failed

			for (int rotation = 0; rotation < 16; rotation++) {
				mc().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
				compiler.begin(list + rotation);
				try {
					for (Map.Entry<BlockPos, RenderObject> entry : renderObjects.entrySet())
						entry.getValue().draw(compiler, entry.getKey(), rotation);
				} finally {
					compiler.end();
				}
			}

			mustRecompile = false;
		}

		private synchronized void delete() {
			deleteList();
		}

		private synchronized void deleteList() {
			if (list == 0)
				return;

			GL11.glDeleteLists(list, 16);
			list = 0;
		}

	}

}
