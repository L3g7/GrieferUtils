/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors;

import java.io.IOException;
import java.nio.file.FileSystem;

/**
 * A processor applied after building.
 */
public abstract class CompilationPostProcessor {

	public abstract void apply(FileSystem fs) throws IOException;

}
