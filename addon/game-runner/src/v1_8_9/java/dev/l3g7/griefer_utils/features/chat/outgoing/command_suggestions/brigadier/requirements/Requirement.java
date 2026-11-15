/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal.GrieferUtilsConfigRequirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal.GrieferUtilsVersionRequirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal.NativeBoolOpRequirement.NativeAndRequirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal.NativeBoolOpRequirement.NativeNotRequirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal.NativeBoolOpRequirement.NativeOrRequirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.minecraft.RankRequirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.minecraft.SubserverRequirement;

import java.util.Arrays;

import static dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal.NativeFixedRequirement.ALWAYS;
import static dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal.NativeFixedRequirement.NEVER;

/**
 * A condition that hides suggestions / nodes if it fails.
 */
public abstract class Requirement {

	private static final Requirement UNKNOWN = new Requirement() {
		@Override
		public boolean test() {
			return true;
		}
	};

	public static JsonDeserializer<Requirement> DESERIALIZER = (jsonElem, ty, context) -> {
		JsonObject json = jsonElem.getAsJsonObject();
		String type = json.get("type").getAsString();

		return switch (type) {
			case "true" -> ALWAYS;
			case "false" -> NEVER;
			case "and" -> context.deserialize(json, NativeAndRequirement.class);
			case "not" -> context.deserialize(json, NativeNotRequirement.class);
			case "or" -> context.deserialize(json, NativeOrRequirement.class);

			case "griefer_utils_config" -> context.deserialize(json, GrieferUtilsConfigRequirement.class);
			case "griefer_utils_version" -> context.deserialize(json, GrieferUtilsVersionRequirement.class);
			case "mc_rank" -> context.deserialize(json, RankRequirement.class);
			case "mc_subserver" -> context.deserialize(json, SubserverRequirement.class);

			default -> UNKNOWN;
		};
	};

	public abstract boolean test();

	public Requirement and(Requirement other) {
		return new NativeAndRequirement(Arrays.asList(this, other));
	}

	public Requirement not() {
		return new NativeNotRequirement(this);
	}

}
