package dev.l3g7.griefer_utils.util.misc;

import dev.l3g7.griefer_utils.util.misc.functions.Function;

import java.util.ArrayList;
import java.util.List;

public class ChatLogModifier {

	private static final List<Function<String, String>> modifier = new ArrayList<>();

	public static void addModifier(Function<String, String> modifier) {
		ChatLogModifier.modifier.add(modifier);
	}

	public static String modifyMessage(String message) {
		for (Function<String, String> function : modifier) {
			message = function.apply(message);
			if (message == null)
				return null;
		}

		return message;
	}

}
