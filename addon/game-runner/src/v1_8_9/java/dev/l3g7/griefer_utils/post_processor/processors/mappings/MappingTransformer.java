/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.mappings;

import dev.pymdk.mapper.FastMapper;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.Arrays;

public class MappingTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] classBytes) {
		byte[] input = Arrays.copyOf(classBytes, classBytes.length);
 		return FastMapper.mapClass(input).getData();
	}

}