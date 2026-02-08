package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.CommandDispatcher;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.CommandDispatcher.Source;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.DeserializationException;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.internal.EnumNode;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.internal.KeyValueNode;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.internal.NativePrimitiveNode;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.internal.NativeStringNode;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.minecraft.PlayerNameNode;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.minecraft.PlayerTimeNode;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.minecraft.PlotIdNode;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.suggestions.Suggestion;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A brigadier argument node.
 */
public abstract class ArgumentNode<T> extends Node<RequiredArgumentBuilder<Source, T>> {

	private static final ArgumentNode<Void> UNKNOWN = new ArgumentNode<>() {
		@Override
		public void register(CommandDispatcher dispatcher, Consumer<CommandNode<Source>> callback) {
			// NO-OP
		}

		@Override
		public ArgumentType<Void> getType() {
			throw new DeserializationException("Cannot get type of unknown node");
		}
	};

	public static JsonDeserializer<ArgumentNode<?>> DESERIALIZER = (jsonElem, ty, context) -> {
		JsonObject json = jsonElem.getAsJsonObject();

		String type = json.get("type").getAsString();
		return switch (type) {
			case "boolean" -> context.deserialize(json, NativePrimitiveNode.NativeBooleanNode.class);
			case "float" -> context.deserialize(json, NativePrimitiveNode.NativeFloatNode.class);
			case "integer" -> context.deserialize(json, NativePrimitiveNode.NativeIntegerNode.class);
			case "word" -> context.deserialize(json, NativeStringNode.NativeWordNode.class);
			case "text" -> context.deserialize(json, NativeStringNode.NativeTextNode.class);

			case "enum" -> context.deserialize(json, EnumNode.class);
			case "key_value" -> context.deserialize(json, KeyValueNode.class);

			case "mc_player_time" -> context.deserialize(json, PlayerTimeNode.class);
			case "mc_player_name" -> context.deserialize(json, PlayerNameNode.class);
			case "mc_plot_id" -> context.deserialize(json, PlotIdNode.class);

			default -> UNKNOWN;
		};
	};

	private static final Dynamic2CommandExceptionType INVALID_CHAR = new Dynamic2CommandExceptionType((got, wanted) -> new LiteralMessage("Invalid character '" + got + "', expected '" + wanted + "'"));

	private String name;
	private List<Suggestion> suggestions;

	public abstract ArgumentType<T> getType();

	@Override
	protected RequiredArgumentBuilder<Source, T> builder() {
		RequiredArgumentBuilder<Source, T> builder = RequiredArgumentBuilder.argument(name, getType());

		if (suggestions != null && !suggestions.isEmpty())
			builder.suggests(this::listSuggestions);

		return builder;
	}

	private CompletableFuture<Suggestions> listSuggestions(CommandContext<Source> context, SuggestionsBuilder builder) {
		for (int i = 0; i < Math.min(suggestions.size(), 4); i++) {
			var suggestion = suggestions.get(i);
			if (suggestion.canUse()) {
				String value = suggestion.get();
				if (value.toLowerCase().startsWith(builder.getRemainingLowerCase()))
					builder.suggest(value);
			}
		}

		return builder.buildFuture();
	}

	/**
	 * A string argument type that encapsulates all consumed chars.
	 */
	public static abstract class ValidatingArgumentType implements ArgumentType<String> {

		public abstract void check(StringReader reader) throws CommandSyntaxException;

		@Override
		public String parse(StringReader reader) throws CommandSyntaxException {
			int start = reader.getCursor();

			try {
				check(reader);
			} catch (CommandSyntaxException e) {
				reader.setCursor(start);
				throw e;
			}

			return reader.getRead().substring(start);
		}

		public static void readChar(StringReader reader, char target, boolean ignoreCase) throws CommandSyntaxException {
			if (!reader.canRead())
				throw INVALID_CHAR.create("EOL", target);

			char c = reader.peek();
			if (ignoreCase)
				c = Character.toLowerCase(c);

			if (c != target)
				throw INVALID_CHAR.create(c, target);

			reader.skip();
		}

		public static void readString(StringReader reader, String target, boolean ignoreCase) throws CommandSyntaxException {
			for (char c : target.toCharArray())
				readChar(reader, c, ignoreCase);
		}


	}

}
