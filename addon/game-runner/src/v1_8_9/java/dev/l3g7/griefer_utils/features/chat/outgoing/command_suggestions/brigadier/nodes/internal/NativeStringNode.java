package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.internal;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.ArgumentNode;

import java.util.Arrays;
import java.util.Collection;

/**
 * A brigadier string argument node.
 */
public abstract class NativeStringNode extends ArgumentNode<String> {

	protected int min = 0;
	protected int max = Integer.MAX_VALUE;

	public static class NativeWordNode extends NativeStringNode {

		@Override
		public ArgumentType<String> getType() {
			return new LimitedStringArgumentType(StringArgumentType.word(), min, max);
		}

	}

	public static class NativeTextNode extends NativeStringNode {

		@Override
		public ArgumentType<String> getType() {
			return new LimitedStringArgumentType(StringArgumentType.greedyString(), min, max);
		}

	}

	/**
	 * A string argument type with min / max length constraints.
	 */
	public static class LimitedStringArgumentType extends ValidatingArgumentType {

		private static final Dynamic2CommandExceptionType TOO_SHORT = new Dynamic2CommandExceptionType((found, min) -> new LiteralMessage("String must not be less than " + min + " characters, found " + found));
		private static final Dynamic2CommandExceptionType TOO_LONG = new Dynamic2CommandExceptionType((found, max) -> new LiteralMessage("String must not be more than " + max + " characters, found " + found));

		private static final Collection<String> EXAMPLES = Arrays.asList("word", "words with spaces", "\"and symbols\"");
		private final StringArgumentType type;
		private final int min, max;

		public LimitedStringArgumentType(StringArgumentType type, int min, int max) {
			this.type = type;
			this.min = min;
			this.max = max;
		}

		@Override
		public void check(StringReader reader) throws CommandSyntaxException {
			int start = reader.getCursor();
			int length = type.parse(reader).length();

			if (length < min) {
				reader.setCursor(start);
				throw TOO_SHORT.createWithContext(reader, length, min);
			}

			if (length > max) {
				reader.setCursor(start);
				throw TOO_LONG.createWithContext(reader, length, max);
			}
		}

		@Override
		public Collection<String> getExamples() {
			return EXAMPLES;
		}
	}
}
