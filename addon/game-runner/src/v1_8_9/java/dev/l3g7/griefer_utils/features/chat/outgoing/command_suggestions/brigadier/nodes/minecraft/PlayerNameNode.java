package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.minecraft;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.ArgumentNode;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.suggestions.minecraft.PlayerNameSuggestionProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * A Minecraft player name argument node.
 */
public class PlayerNameNode extends ArgumentNode<String> {

	@Override
	public PlayerNameArgumentType getType() {
		return new PlayerNameArgumentType();
	}

	public static class PlayerNameArgumentType implements ArgumentType<String> {

		private static final SimpleCommandExceptionType EXPECTED_PLAYER_NAME = new SimpleCommandExceptionType(new LiteralMessage("Expected player name"));
		private static final DynamicCommandExceptionType INVALID_PLAYER_NAME = new DynamicCommandExceptionType(value -> new LiteralMessage("Invalid player name '" + value + "'"));
		private static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("[~§\\w]{3,}|[~!§\\w+]{3,}");

		@Override
		public String parse(StringReader reader) throws CommandSyntaxException {
			int start = reader.getCursor();

			while(reader.canRead() && reader.peek() != ' ')
				reader.skip();

			String name = reader.getString().substring(start, reader.getCursor());

			if (name.isEmpty()) {
				reader.setCursor(start);
				throw EXPECTED_PLAYER_NAME.create();
			}

			if (!PLAYER_NAME_PATTERN.matcher(name).matches()) {
				reader.setCursor(start);
				throw INVALID_PLAYER_NAME.create(name);
			}

			return name;
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			return PlayerNameSuggestionProvider.request(builder.getRemaining()).thenApply(matches -> {
				for (String match : matches)
					if (match.toLowerCase().startsWith(builder.getRemainingLowerCase()))
						builder.suggest(match);

				return builder.build();
			});
		}

		@Override
		public Collection<String> getExamples() {
			return Arrays.asList("Notch", "L3g7", "GrieferUtils");
		}
	}
}
