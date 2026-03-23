package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.internal;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.nodes.ArgumentNode;

/**
 * A brigadier integer / float / boolean argument node.
 */
public class NativePrimitiveNode {

	public static class NativeIntegerNode extends ArgumentNode<Integer> {

		private int min = Integer.MIN_VALUE;
		private int max = Integer.MAX_VALUE;

		@Override
		public ArgumentType<Integer> getType() {
			return IntegerArgumentType.integer(min, max);
		}
	}

	public static class NativeFloatNode extends ArgumentNode<Float> {
		@Override
		public ArgumentType<Float> getType() {
			return FloatArgumentType.floatArg();
		}
	}

	public static class NativeBooleanNode extends ArgumentNode<Boolean> {
		@Override
		public ArgumentType<Boolean> getType() {
			return BoolArgumentType.bool();
		}
	}

}
