package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.internal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.CommandDispatcher;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.CommandDispatcher.Source;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.Node;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * A brigadier literal node.
 */
public class NativeLiteralNode extends Node<NativeLiteralNode.TooltipLiteralArgumentBuilder> {

	public final String literal;
	private final String label;
	private final String description;

	public NativeLiteralNode(Node<?> source, String literal, String label, String description) {
		super(source);
		this.literal = literal;
		this.label = label;
		this.description = description;
	}

	@Override
	protected TooltipLiteralArgumentBuilder builder() {
		if (description == null)
			return new TooltipLiteralArgumentBuilder(literal, null);
		else
			return new TooltipLiteralArgumentBuilder(literal, new LiteralMessage(description));
	}

	@Override
	protected CommandNode<Source> create(CommandDispatcher dispatcher) {
		if (label != null)
			dispatcher.registerLabel(label, this);

		return super.create(dispatcher);
	}

	/**
	 * A {@link LiteralArgumentBuilder} with tooltip.
	 */
	public static class TooltipLiteralArgumentBuilder extends LiteralArgumentBuilder<Source> {
		private final Message tooltip;

		public TooltipLiteralArgumentBuilder(String literal, Message tooltip) {
			super(literal);
			this.tooltip = tooltip;
		}

		@Override
		public LiteralCommandNode<Source> build() {
			final TooltipLiteralCommandNode result = new TooltipLiteralCommandNode(getLiteral(), getCommand(), getRequirement(), getRedirect(), getRedirectModifier(), isFork(), tooltip);

			for (final CommandNode<Source> argument : getArguments())
				result.addChild(argument);

			return result;
		}
	}

	/**
	 * A {@link LiteralCommandNode} with tooltip.
	 */
	public static class TooltipLiteralCommandNode extends LiteralCommandNode<Source> {
		private final Message tooltip;
		private final String literalLowerCase;

		public TooltipLiteralCommandNode(String literal, Command<Source> command, Predicate<Source> requirement, CommandNode<Source> redirect, RedirectModifier<Source> modifier, boolean forks, Message tooltip) {
			super(literal, command, requirement, redirect, modifier, forks);
			this.tooltip = tooltip;
			this.literalLowerCase = literal.toLowerCase(Locale.ROOT);
		}

		@Override
		public CompletableFuture<Suggestions> listSuggestions(CommandContext<Source> context, SuggestionsBuilder builder) {
			if (getRequirement().test(context.getSource()) && literalLowerCase.startsWith(builder.getRemainingLowerCase())) {
				return builder.suggest(getLiteral(), tooltip).buildFuture();
			} else {
				return Suggestions.empty();
			}
		}
	}
}
