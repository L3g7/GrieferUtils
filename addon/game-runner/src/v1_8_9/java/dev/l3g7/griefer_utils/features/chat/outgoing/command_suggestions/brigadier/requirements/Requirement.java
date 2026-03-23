package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal.GrieferUtilsConfigRequirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal.NativeBoolOpRequirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal.NativeFixedRequirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.minecraft.RankRequirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.minecraft.SubserverRequirement;

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
			case "true" -> NativeFixedRequirement.ALWAYS;
			case "false" -> NativeFixedRequirement.NEVER;
			case "and" -> context.deserialize(json, NativeBoolOpRequirement.NativeAndRequirement.class);
			case "not" -> context.deserialize(json, NativeBoolOpRequirement.NativeNotRequirement.class);
			case "or" -> context.deserialize(json, NativeBoolOpRequirement.NativeOrRequirement.class);

			case "griefer_utils_config" -> context.deserialize(json, GrieferUtilsConfigRequirement.class);
			case "mc_rank" -> context.deserialize(json, RankRequirement.class);
			case "mc_subserver" -> context.deserialize(json, SubserverRequirement.class);

			default -> UNKNOWN;
		};
	};

	public abstract boolean test();

}
