/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.sinks;

/**
 * A sink to draw vertices into.
 */
public interface VertexSink {

	/**
	 * Draws a single vertex into this sink.
	 */
	void drawVertex(double x, double y, double z);

}
