/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

public abstract class RenderObject {

	static final List<Generator> generators = new ArrayList<>();

	public static void registerGenerator(Generator generator) {
		generators.add(generator);
	}

	static RenderObject fromState(BlockPos worldPos, IBlockState state, WorldClient world) {
		if (state == null)
			return null;

		for (Generator generator : generators) {
			if (!generator.isEnabled())
				continue;

			RenderObject renderObject = generator.getRenderObject(worldPos, state, world);
			if (renderObject != null)
				return renderObject;
		}

		return null;
	}

	public abstract boolean equals(RenderObject r);

	public abstract void draw(ChunkCompiler compiler, int rotation);

	public interface Generator {

		boolean isEnabled();

		RenderObject getRenderObject(BlockPos worldPos, IBlockState state, WorldClient world);

	}

}
