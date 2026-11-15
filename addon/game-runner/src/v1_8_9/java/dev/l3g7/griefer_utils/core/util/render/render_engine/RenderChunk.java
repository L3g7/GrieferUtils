package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.util.render.render_engine.data.RenderType;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;

import java.util.*;
import java.util.Map.Entry;

/**
 * A single compiled chunk.
 * @see ChunkPart
 */
public class RenderChunk {

	public static final int ROTATION_STEPS = 16;
	public static final double ROTATION_SLICE = 360d / ROTATION_STEPS;

	/**
	 * The compiler used for all chunks. Compilation only happens on the render thread.
	 */
	private static final ChunkCompiler compiler = new ChunkCompiler();

	/**
	 * Helper method to delete all RenderChunks.
	 */
	public static void deleteAll(Collection<RenderChunk> renderChunks) {
		for (RenderChunk chunk : renderChunks)
			chunk.delete();

		renderChunks.clear();
	}

	/**
	 * All parts in this chunk, is there's no data in an entry it is <code>null</code>.
	 */
	private final ChunkPart[] parts = new ChunkPart[16];

	/**
	 * Adds a RenderObject to this chunk.
	 * @param chunkPos     The position in this chunk (x and z are between 0 and 15)
	 * @param renderObject The RenderObject to add.
	 */
	public void add(BlockPos chunkPos, RenderObject renderObject) {
		int index = chunkPos.getY() / 16;
		ChunkPart part = parts[index];
		if (part == null)
			part = parts[index] = new ChunkPart();

		part.add(chunkPos, renderObject);
	}

	/**
	 * Removes the RenderObject at the given position, if there is one.
	 * @param chunkPos The position in this chunk (x and z are between 0 and 15)
	 */
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

	/**
	 * Deletes all GL resources used in this chunk.
	 */
	public void delete() {
		synchronized (parts) {
			for (ChunkPart part : parts)
				if (part != null)
					part.delete();
		}
	}

	/**
	 * @return Whether any part of this chunk has any RenderObjects.
	 */
	public boolean hasData() {
		synchronized (parts) {
			for (ChunkPart compiledPart : parts)
				if (compiledPart != null)
					return true;

			return false;
		}
	}

	/**
	 * Checks whether any part must be recompiled.
	 * @param forceRecompilation Forcefully recompiles all parts.
	 */
	public void checkRecompile(boolean forceRecompilation) {
		synchronized (parts) {
			for (ChunkPart part : parts)
				if (part != null && (part.mustRecompile || forceRecompilation))
					part.recompile();
		}
	}

	/**
	 * Draws this chunk into the world.
	 * @param pair     The chunk's position, used for culling.
	 * @param frustum  The frustum to cull with.
	 * @param rotation The player's rotation.
	 */
	public void draw(ChunkCoordIntPair pair, Frustum frustum, int rotation) {
		synchronized (parts) {
			for (int i = 0; i < parts.length; i++) {
				ChunkPart part = parts[i];
				if (part == null || part.buffers.isEmpty())
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

				Atlas.getAtlas().bind();
				for (Entry<RenderType, CompiledBuffer> entry : part.buffers.entrySet()) {
					entry.getKey().setup();
					entry.getValue().draw(rotation);
				}
			}
		}
	}

	/**
	 * A single 16x16x16 part of a chunk.
	 */
	private static class ChunkPart {

		private boolean mustRecompile = false;

		private final Map<RenderType, CompiledBuffer> buffers = new EnumMap<>(RenderType.class);
		private final Map<BlockPos, RenderObject> renderObjects = new HashMap<>();

		/**
		 * Adds a RenderObject to this chunk.
		 * @param chunkPos        The position in this chunk (x and z are between 0 and 15)
		 * @param newRenderObject The RenderObject to add.
		 */
		public synchronized void add(BlockPos chunkPos, RenderObject newRenderObject) {
			RenderObject oldRenderObject = this.renderObjects.put(chunkPos, newRenderObject);

			if (oldRenderObject == null || !oldRenderObject.equals(newRenderObject))
				mustRecompile = true;
		}

		/**
		 * Removes the RenderObject at the given position, if there is one.
		 * @param chunkPos The position in this chunk (x and z are between 0 and 15)
		 * @return Whether this part is empty after removing the position.
		 */
		public synchronized boolean remove(BlockPos chunkPos) {
			if (renderObjects.remove(chunkPos) != null)
				mustRecompile = true;

			return renderObjects.isEmpty();
		}

		/**
		 * Recompiles this section by drawing it into new VBOs and deleting the old ones.
		 */
		private synchronized void recompile() {
			delete();
			if (renderObjects.isEmpty())
				return;

			Map<RenderType, List<Entry<BlockPos, RenderObject>>> renderObjectsByType = new HashMap<>();

			// Sort RenderObjects
			for (Entry<BlockPos, RenderObject> entry : renderObjects.entrySet()) {
				for (RenderType renderType : entry.getValue().renderTypes()) {
					renderObjectsByType
						.computeIfAbsent(renderType, k -> new ArrayList<>())
						.add(entry);
				}
			}

			// Render by type
			for (Entry<RenderType, List<Entry<BlockPos, RenderObject>>> typeEntry : renderObjectsByType.entrySet()) {
				RenderType type = typeEntry.getKey();
				int[] firstVertexForStep = new int[type.size];
				int[] vertexCount = new int[type.size];

				// Draw all steps consecutively into the compiler
				compiler.begin();
				for (int step = 0; step < type.size; step++) {
					firstVertexForStep[step] = compiler.getVertexCount();
					for (Entry<BlockPos, RenderObject> renderObject : typeEntry.getValue()) {
						compiler.prepare(renderObject.getKey());
						renderObject.getValue().draw(compiler, type, renderObject.getKey(), step);
					}
					vertexCount[step] = compiler.getVertexCount() - firstVertexForStep[step];
				}

				if (compiler.getVertexCount() == 0)
					continue; // Nothing to draw

				int vbo = OpenGlHelper.glGenBuffers();
				if (vbo == 0)
					return; // glGenBuffers failed, try again next frame

				compiler.upload(vbo);
				buffers.put(type, new CompiledBuffer(vbo, firstVertexForStep, vertexCount));
			}

			mustRecompile = false;
		}

		private synchronized void delete() {
			if (buffers.isEmpty())
				return;

			for (CompiledBuffer buffer : buffers.values())
				buffer.delete();
			buffers.clear();
		}

	}

}
