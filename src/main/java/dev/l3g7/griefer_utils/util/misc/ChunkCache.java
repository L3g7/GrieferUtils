package dev.l3g7.griefer_utils.util.misc;

public interface ChunkCache {

	void handleChunks(boolean load);
	void clearCaches();
	void reset();

}
