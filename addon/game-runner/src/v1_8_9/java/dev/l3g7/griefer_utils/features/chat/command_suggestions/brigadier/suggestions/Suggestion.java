package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.suggestions;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.DeserializationException;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.requirements.Requirement;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.suggestions.internal.NativeLiteralSuggestion;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.suggestions.minecraft.BankSuggestion;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.suggestions.minecraft.MoneySuggestion;

import static dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.requirements.internal.NativeFixedRequirement.ALWAYS;

/**
 * A custom suggestion.
 */
public abstract class Suggestion {

	private static final Suggestion UNKNOWN = new Suggestion() {
		@Override
		public boolean canUse() {
			return false;
		}

		@Override
		public String get() {
			throw new DeserializationException("Cannot get unknown suggestion");
		}
	};

	public static JsonDeserializer<Suggestion> DESERIALIZER = (jsonElem, ty, context) -> {
		JsonObject json = jsonElem.getAsJsonObject();
		if (json.has("literal"))
			return context.deserialize(json, NativeLiteralSuggestion.class);

		String type = json.get("type").getAsString();
		return switch (type) {
			case "mc_bank" -> context.deserialize(json, BankSuggestion.class);
			case "mc_money" -> context.deserialize(json, MoneySuggestion.class);

			default -> UNKNOWN;
		};
	};

	protected Requirement requirements = ALWAYS;

	public abstract String get();

	public boolean canUse() {
		return requirements.test();
	}

}
