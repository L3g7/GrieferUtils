package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.minecraft;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.ArgumentNode;

import java.util.Arrays;
import java.util.Collection;

/**
 * A PlotSquared plot id argument node.
 */
public class PlotIdNode extends ArgumentNode<String> {

	@Override
	public PlotIdArgumentType getType() {
		return new PlotIdArgumentType();
	}

	public static class PlotIdArgumentType extends ValidatingArgumentType {

		private static final Collection<String> EXAMPLES = Arrays.asList("0;0", "-48;2", "123;456");

		@Override
		public void check(StringReader reader) throws CommandSyntaxException {
			reader.readInt();
			readChar(reader, ';', true);
			reader.readInt();
		}

		@Override
		public Collection<String> getExamples() {
			return EXAMPLES;
		}
	}
}
