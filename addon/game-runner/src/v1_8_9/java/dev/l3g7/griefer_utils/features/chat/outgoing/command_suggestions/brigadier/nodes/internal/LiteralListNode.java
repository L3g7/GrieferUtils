package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.internal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.CommandDispatcher;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.CommandDispatcher.Source;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.DeserializationException;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.Node;

import java.util.function.Consumer;

/**
 * A compressed list of literal nodes.
 */
public class LiteralListNode extends Node<LiteralArgumentBuilder<Source>> {

	public final String[] values;

	public LiteralListNode(String[] values) {
		this.values = values;
	}

	@Override
	public void register(CommandDispatcher dispatcher, Consumer<CommandNode<Source>> callback) {
		for (String value : values) {
			NativeLiteralNode node = new NativeLiteralNode(this, value, null, null);
			node.copyFrom(this);
			node.register(dispatcher, callback);
		}
	}

	@Override
	protected LiteralArgumentBuilder<Source> builder() {
		throw new DeserializationException("Cannot build lists");
	}

}
