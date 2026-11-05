package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import net.minecraft.world.ChunkCoordIntPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates {@link RenderChunk}s and keeps their data up to date.
 */
public abstract class RenderChunkManager {

	static final List<RenderChunkManager> managers = new ArrayList<>();

	static {
		managers.add(FileProvider.getSingleton(DefaultRenderChunkManager.class));
		if (Constants.SCHEMATICA) {
			//noinspection StaticInitializerReferencesSubClass (it works lol)
			managers.add(FileProvider.getSingleton(SchematicaRenderChunkManager.class));
		}
	}

	public static void reloadAll() {
		for (RenderChunkManager manager : managers)
			manager.reload();
	}

	protected final Map<ChunkCoordIntPair, RenderChunk> chunks = new ConcurrentHashMap<>();

	abstract void reload();

	protected void checkRecompilation(boolean forceRecompilation) {
		for (RenderChunk chunk : chunks.values())
			chunk.checkRecompile(forceRecompilation);
	}

}
