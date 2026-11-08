/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.sinks;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;

/**
 * A {@link VertexSink} drawing vertices using Minecraft's {@link WorldRenderer}.<br>
 * Used for immediate drawing.
 */
public class WorldSink implements VertexSink {

	public static WorldSink INSTANCE = new WorldSink();

	private final WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
	private WorldSink() {}

	@Override
	public void drawVertex(double x, double y, double z) {
		wr.pos(x, y, z).endVertex();
	}

}
