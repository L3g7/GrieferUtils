package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.CommandDispatcher;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.CommandDispatcher.Source;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.internal.LiteralListNode;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.internal.NativeLiteralNode;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.Requirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.internal.NativeFixedRequirement;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.minecraft.SubserverRequirement;

import java.util.List;
import java.util.function.Consumer;

/**
 * A command node.
 *
 * @see ArgumentNode
 * @see NativeLiteralNode
 */
public abstract class Node<T extends ArgumentBuilder<Source, ? super T>> {

	private static final SimpleCommandExceptionType INVALID_SIDE = new SimpleCommandExceptionType(new LiteralMessage("Cannot run commands clientside."));

	public static JsonDeserializer<Node<?>> DESERIALIZER = (jsonElem, ty, context) -> {
		JsonObject json = jsonElem.getAsJsonObject();

		if (json.has("literal"))
			return context.deserialize(json, NativeLiteralNode.class);
		else if (json.get("type").getAsString().equals("literal_list"))
			return context.deserialize(json, LiteralListNode.class);
		else
			return context.deserialize(json, ArgumentNode.class);
	};

	protected boolean command = false;
	protected String redirect = null;
	protected HubAvailability hubAvailability = null;
	protected Requirement requirements;
	protected List<Node<?>> children;

	private transient CommandNode<Source> builtNode;

	protected Node() {}

	protected Node(Node<?> source) {
		this.command = source.command;
		this.redirect = source.redirect;
		this.hubAvailability = source.hubAvailability;
		this.requirements = source.requirements;
		this.children = source.children;
	}

	public void register(CommandDispatcher dispatcher, Consumer<CommandNode<Source>> callback) {
		dispatcher.registerNode(this, callback);
	}

	public void copyFrom(Node<?> source) {
		this.command = source.command;
		this.redirect = source.redirect;
		this.requirements = source.requirements;
		this.children = source.children;
	}

	protected abstract T builder();

	public CommandNode<Source> build(CommandDispatcher dispatcher) {
		if (builtNode != null)
			return builtNode;

		builtNode = create(dispatcher);
		return builtNode;
	}

	protected CommandNode<Source> create(CommandDispatcher dispatcher) {
		T builder = builder();

		if (command) {
			builder.executes(ctx -> {
				throw INVALID_SIDE.create();
			});
		}

		if (redirect != null)
			builder.redirect(dispatcher.getLabel(redirect).build(dispatcher));

		Requirement requirement = HubAvailability.mergeRequirement(requirements, hubAvailability);
		builder.requires(src -> requirement.test());

		if (children != null) {
			for (Node<?> child : children) {
				if (child.hubAvailability == null)
					child.hubAvailability = this.hubAvailability;

				child.register(dispatcher, builder::then);
			}
		}

		return builder.build();
	}

	public enum HubAvailability {
		/**
		 * Available in Lobby, Portal and all citybuilds.
		 */
		ALL,

		/**
		 * Available in Lobby and all citybuilds.
		 */
		LOBBY,

		/**
		 * Available in Portal and all citybuilds.
		 */
		PORTAL,

		/**
		 * Available in citybuilds only.
		 */
		UNAVAILABLE;

		private static Requirement mergeRequirement(Requirement requirement, HubAvailability availability) {
			if (availability == null)
				availability = UNAVAILABLE;

			if (requirement == null)
				return availability.toRequirement();

			return requirement.and(availability.toRequirement());
		}

		private Requirement toRequirement() {
			return switch (this) {
				case ALL -> NativeFixedRequirement.ALWAYS;
				case LOBBY -> new SubserverRequirement("PORTAL").not();
				case PORTAL -> new SubserverRequirement("LOBBY").not();
				case UNAVAILABLE -> LOBBY.toRequirement().and(PORTAL.toRequirement());
			};
		}

	}

}
