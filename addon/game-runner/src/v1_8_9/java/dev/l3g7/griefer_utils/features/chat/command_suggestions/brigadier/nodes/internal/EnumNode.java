package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.internal;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.ArgumentNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * An enum argument node.
 */
public class EnumNode extends ArgumentNode<String> {

	public final String[] values;

	public EnumNode(String[] values) {
		this.values = values;
	}

	@Override
	public ArgumentType<String> getType() {
		return new EnumArgumentType(values);
	}

	public static class EnumArgumentType extends ValidatingArgumentType {

		private static final SimpleCommandExceptionType EXPECTED_VALUE = new SimpleCommandExceptionType(new LiteralMessage("Expected value"));
		private static final DynamicCommandExceptionType INVALID_VALUE = new DynamicCommandExceptionType(value -> new LiteralMessage("Invalid value '" + value + "'"));

		private final String[] values;
		private final Collection<String> examples;

		public EnumArgumentType(String[] values) {
			this.values = values;

			if (values.length > 4)
				examples = Arrays.asList(values).subList(0, 4);
			else
				examples = Arrays.asList(values);
		}

		@Override
		public void check(StringReader reader) throws CommandSyntaxException {
			int i = reader.getCursor();

			while (reader.canRead() && reader.peek() != ' ')
				reader.skip();

			String value = reader.getString().substring(i, reader.getCursor());
			if (value.isEmpty())
				throw EXPECTED_VALUE.create();

			for (String enumVal : values)
				if (enumVal.equalsIgnoreCase(value))
					return;

			throw INVALID_VALUE.create(value);
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			for (String value : values)
				if (value.toLowerCase().startsWith(builder.getRemainingLowerCase()))
					builder.suggest(value);

			return builder.buildFuture();
		}

		@Override
		public Collection<String> getExamples() {
			return examples;
		}
	}
}
