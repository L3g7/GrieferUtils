/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server.requests.bsf;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.api.misc.server.Request;

import java.util.List;
import java.util.Set;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.BSF_URL;

public class BSFProcessRequest extends Request<List<String>> {

	private final String cb;
	@SerializedName("center_x")
	private final int centerX;
	@SerializedName("center_z")
	private final int centerZ;
	private final int[] origin;
	private final Set<Data> data;

	public BSFProcessRequest(String cb, int centerX, int centerZ, int[] origin, Set<Data> data) {
		super(BSF_URL, "/process");
		this.cb = cb;
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.origin = origin;
		this.data = data;
	}

	@Override
	protected List<String> parseResponse(Response response) {
		return response.convertTo(List.class);
	}

	public record Data(int position, int[] m1, byte[] tail) {

		public Data(int position, char[] m1, byte[] tail) {
			this(position, new int[m1.length], tail);
			for (int i = 0; i < m1.length; i++) {
				this.m1[i] = m1[i];
			}
		}

	}

}
