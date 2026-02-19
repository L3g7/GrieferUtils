package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.suggestions.internal;

import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.suggestions.Suggestion;

/**
 * A fixed text suggestion.
 */
public class NativeLiteralSuggestion extends Suggestion {
	private final String literal;

	public NativeLiteralSuggestion(String literal) {
		this.literal = literal;
	}

	@Override
	public String get() {
		return literal;
	}
}
