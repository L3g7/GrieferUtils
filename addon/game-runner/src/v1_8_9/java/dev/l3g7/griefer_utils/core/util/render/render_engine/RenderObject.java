/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.primitives.containers.Lazy;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.RenderType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A single object being rendered at a block.
 */
public interface RenderObject {

	/**
	 * All generators that may provide RenderObjects.
	 */
	Lazy<List<Generator>> generators = new Lazy<>(() ->
		FileProvider.getClassesWithInterface(Generator.class)
			.stream().map(m -> FileProvider.getSingleton(m.<Generator>load()))
			.collect(Collectors.toList())
	);

	/**
	 * @param worldPos The RenderObject's position in the world.
	 * @param state    The block state at the given position.
	 * @param world    The world to use.
	 * @return The RenderObject for the given position, or <code>null</code> if there is none.
	 */
	static RenderObject fromState(BlockPos worldPos, IBlockState state, WorldClient world) {
		if (state == null)
			return null;

		for (Generator generator : generators.get()) {
			if (!generator.isEnabled())
				continue;

			RenderObject renderObject = generator.getRenderObject(worldPos, state, world);
			if (renderObject != null)
				return renderObject;
		}

		return null;
	}

	boolean equals(RenderObject r);

	/**
	 * How this RenderObject is rendered.<br>
	 * Must be constant for each RenderObject.<br>
	 */
	RenderType[] renderTypes();

	/**
	 * Draws this object into a {@link ChunkCompiler}.
	 *
	 * @param compiler      The compiler to draw into.
	 * @param renderType    How this RenderObject is rendered. A value from {@link #renderTypes()}.
	 * @param chunkPosition This object's chunkPosition.
	 * @param step          The step currently being compiled, from 0 to {@link RenderType#size} - 1.
	 * @see ChunkCompiler
	 */
	void draw(ChunkCompiler compiler, RenderType renderType, BlockPos chunkPosition, int step);

	/**
	 * A factory generating RenderObjects.
	 */
	interface Generator {

		boolean isEnabled();

		/**
		 * @param worldPos The RenderObject's position in the world.
		 * @param state    The block state at the given position.
		 * @param world    The world to use.
		 * @return The RenderObject for the given position, or <code>null</code> if there is none.
		 */
		RenderObject getRenderObject(BlockPos worldPos, IBlockState state, WorldClient world);

	}

}
