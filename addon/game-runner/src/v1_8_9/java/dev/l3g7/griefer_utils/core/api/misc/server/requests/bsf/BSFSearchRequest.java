/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server.requests.bsf;

import dev.l3g7.griefer_utils.core.api.misc.server.Request;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.BSF_URL;

public class BSFSearchRequest extends Request<BSFSearchRequest.SearchResponse> {

	private final String cb;
	private final ChunkPos start;
	private final List<Integer> excluded;

	protected BSFSearchRequest(String path, String cb, int startX, int startZ, List<Integer> excluded) {
		super(BSF_URL, path);
		this.cb = cb;
		this.start = new ChunkPos(startX, startZ);
		this.excluded = excluded;
	}

	@Override
	protected SearchResponse parseResponse(Response response) {
		if (response.getStatus() == 422)
			return SearchResponse.WORLD_NOT_READY;

		if (response.getStatus() == 204)
			return SearchResponse.ALL_FOUND;

		return response.convertTo(SearchResponse.class);
	}

	public record SearchResponse(ChunkPos pos, int index) {
			public static SearchResponse ALL_FOUND = new SearchResponse(new ChunkPos(0, 0), -1);
			public static SearchResponse WORLD_NOT_READY = new SearchResponse(new ChunkPos(0, 0), -2);
	}

	public static class ChunkPos {

		public final int x, z;

		private ChunkPos(int x, int z) {
			this.x = x;
			this.z = z;
		}

	}

	public static class Structure extends BSFSearchRequest {

		private final int structure;

		public Structure(String cb, int startX, int startZ, int structure, List<Integer> excluded) {
			super("/search_structure", cb, startX, startZ, excluded);
			this.structure = structure;
		}

	}

	public static class Biome extends BSFSearchRequest {

		private final List<Integer> ids;

		public Biome(String cb, int startX, int startZ, List<Integer> ids, List<Integer> excluded) {
			super("/search_biome", cb, startX, startZ, excluded);
			this.ids = ids;
		}

	}

}
