package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier;

import com.mojang.brigadier.tree.CommandNode;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The brigadier command dispatcher and redirection cache.
 */
public class CommandDispatcher extends com.mojang.brigadier.CommandDispatcher<CommandDispatcher.Source> {

	public static final Source SOURCE = new Source();
	private final Map<String, Node<?>> labels = new HashMap<>();
	private int errors = 0;

	public void registerLabel(String label, Node<?> destination) {
		labels.put(label, destination);
	}

	public void registerNode(Node<?> node, Consumer<CommandNode<Source>> callback) {
		try {
			callback.accept(node.build(this));
		} catch (DeserializationException ignored) {
			errors++;
		}
	}

	public Node<?> getLabel(String label) {
		if (!labels.containsKey(label))
			throw new DeserializationException("Missing label " + label);

		return labels.get(label);
	}

	public int getErrors() {
		return errors;
	}

	public static class Source {}

}
