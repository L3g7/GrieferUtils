package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.minecraft;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.ArgumentNode;

import java.util.Arrays;
import java.util.Collection;

/**
 * A GrieferGames locked chest time argument node.
 */
public class ChestTimeNode extends ArgumentNode<String> {

	@Override
	public PlayerTimeArgumentType getType() {
		return new PlayerTimeArgumentType();
	}

	public static class PlayerTimeArgumentType extends ValidatingArgumentType {

		private static final SimpleCommandExceptionType INVALID_TIME_LOCALIZED = new SimpleCommandExceptionType(new LiteralMessage("Ungültiges Zeitformat"));
		private static final IntegerArgumentType INITIAL = IntegerArgumentType.integer(0);

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
 			INITIAL.parse(reader);

			if (!reader.canRead())
				throw INVALID_TIME_LOCALIZED.create();

			char c = Character.toLowerCase(reader.peek());

			if (c != 'd' && c != 'h' && c != 'm')
				throw INVALID_TIME_LOCALIZED.create();

			reader.skip();
		}

		@Override
		public Collection<String> getExamples() {
			return Arrays.asList("1d", "12h", "30m");
		}
	}
}
