/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.primitives.containers.Lazy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;

import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

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
	 * @param state The block state at the given position.
	 * @param world The world to use.
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
	 * Whether this RenderObject is affected by the player's rotation.<br>
	 * Must be constant for each RenderObject.<br>
	 * If <code>true</code> this RenderObject will be rendered once per rotation step.
	 */
	default boolean rotates() {
		return false;
	}

	/**
	 * Sets up GL capabilities before rendering.
	 * @param compiler The compiler to set up.
	 */
	default void setup(ChunkCompiler compiler) {
		compiler.enable(GL_DEPTH_TEST, GL_TEXTURE_2D);
	}

	/**
	 * Draws this object into a {@link ChunkCompiler}.
	 * @param compiler The compiler to draw into.
	 * @param chunkPosition This object's chunkPosition.
	 * @param rotation The player rotation step to draw for. Always 0 if {@link RenderObject#rotates()} returns <code>false</code>.
	 * @see ChunkCompiler
	 */
	void draw(ChunkCompiler compiler, BlockPos chunkPosition, int rotation);

	/**
	 * A factory generating RenderObjects.
	 */
	interface Generator {

		boolean isEnabled();

		/**
		 * @param worldPos The RenderObject's position in the world.
		 * @param state The block state at the given position.
		 * @param world The world to use.
		 * @return The RenderObject for the given position, or <code>null</code> if there is none.
		 */
		RenderObject getRenderObject(BlockPos worldPos, IBlockState state, WorldClient world);

	}

}
