package dev.l3g7.griefer_utils.post_processor.processors;

import java.io.IOException;
import java.nio.file.FileSystem;

/**
 * A processor applied after building.
 */
public abstract class CompilationPostProcessor {

	public abstract void apply(FileSystem fs) throws IOException;

}
