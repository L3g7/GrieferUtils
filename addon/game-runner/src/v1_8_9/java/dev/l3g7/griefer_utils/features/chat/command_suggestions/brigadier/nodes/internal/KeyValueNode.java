package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.internal;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.ArgumentNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A key value argument node.
 */
public class KeyValueNode<T> extends ArgumentNode<T> {

	private final String key;
	private final ArgumentNode<T> value;

	public KeyValueNode(String key, ArgumentNode<T> value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public ArgumentType<T> getType() {
		return new KeyValueArgumentType<T>(key, value.getType());
	}

	private static class KeyValueArgumentType<V> implements ArgumentType<V> {

		private static final SimpleCommandExceptionType EXPECTED_KEY = new SimpleCommandExceptionType(new LiteralMessage("Expected key"));
		private static final Dynamic2CommandExceptionType INVALID_KEY = new Dynamic2CommandExceptionType((got, wanted) -> new LiteralMessage("Invalid key '" + got + "', expected '" + wanted + "'"));

		private final String key;
		private final ArgumentType<V> value;

		public KeyValueArgumentType(String key, ArgumentType<V> value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public V parse(StringReader reader) throws CommandSyntaxException {
			int start = reader.getCursor();
			String key = reader.readStringUntil('=');

			if (key.isEmpty()) {
				reader.setCursor(start);
				throw EXPECTED_KEY.create();
			}

			if (!key.equals(this.key)) {
				reader.setCursor(start);
				throw INVALID_KEY.create(key, this.key);
			}

			return value.parse(reader);
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			String fullKey = key + "=";
			// If key is done, suggest value
			if (builder.getRemainingLowerCase().startsWith(fullKey))
				return value.listSuggestions(context, builder.createOffset(fullKey.length()));

			// If key is partial, suggest key
			if (fullKey.startsWith(builder.getRemainingLowerCase()))
				builder.suggest(fullKey);

			return builder.buildFuture();
		}

		@Override
		public Collection<String> getExamples() {
			var values = value.getExamples();

			List<String> examples = new ArrayList<>(values.size());
			for (String s : values)
				examples.add(key + "=" + s);

			return examples;
		}
	}
}
