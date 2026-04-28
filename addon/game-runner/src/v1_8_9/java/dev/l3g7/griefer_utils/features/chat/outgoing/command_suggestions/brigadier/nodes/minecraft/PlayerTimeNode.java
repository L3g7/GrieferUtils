package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.minecraft;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.ArgumentNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * A GrieferGames player time argument node.
 */
public class PlayerTimeNode extends ArgumentNode<String> {

	@Override
	public PlayerTimeArgumentType getType() {
		return new PlayerTimeArgumentType();
	}

	public static class PlayerTimeArgumentType extends ValidatingArgumentType {

		private static final SimpleCommandExceptionType INVALID_TIME_LOCALIZED = new SimpleCommandExceptionType(new LiteralMessage("Ungültiges Zeitformat"));
		private static final IntegerArgumentType INITIAL = IntegerArgumentType.integer(0);
		private static final IntegerArgumentType MINUTES = IntegerArgumentType.integer(0, 59);

		@Override
		public String parse(StringReader reader) throws CommandSyntaxException {
			try {
				return super.parse(reader);
			} catch (CommandSyntaxException | IndexOutOfBoundsException e) {
				throw INVALID_TIME_LOCALIZED.create();
			}
		}

		@Override
		public void check(StringReader reader) throws CommandSyntaxException {
			if (reader.peek() == '@')
				reader.skip();

			int initial = INITIAL.parse(reader);
			if (initial > 23) {
				// Must be ticks
				readString(reader, "ticks", true);
				return;
			}

			if (initial > 12) {
				// Must be ticks or 24h format
				if (reader.peek() == 't') {
					// ticks
					readString(reader, "ticks", true);
					return;
				}

				// 24h format
				readChar(reader, ':', true);
				MINUTES.parse(reader);
				return;
			}

			// Maybe 12h format
			if (reader.peek() == 'a' || reader.peek() == 'p') {
				reader.skip();
				readChar(reader, 'm', true);
				return;
			}

			// Maybe 24h format
			if (reader.peek() == ':') {
				reader.skip();
				MINUTES.parse(reader);
				return;
			}

			// Only ticks left
			readString(reader, "ticks", true);
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			if ("6000ticks".startsWith(builder.getRemainingLowerCase()))
				builder.suggest("6000ticks");

			return builder.buildFuture();
		}

		@Override
		public Collection<String> getExamples() {
			return Arrays.asList("17:30", "4pm", "4000ticks");
		}
	}
}
