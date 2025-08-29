package dev.l3g7.griefer_utils.features.uncategorized.scripts;

import dev.l3g7.griefer_utils.core.api.misc.StringUnescaper;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.util.*;

import static dev.l3g7.griefer_utils.features.uncategorized.scripts.Util.removeTrailingComma;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

public class ConstantParser {

	private static final Map<String, Integer> HANDLE_TAGS = new HashMap<>();

	public static Object readConstant(Iterator<String> tokens) {
		if (!tokens.hasNext())
			return null;

		return readConstant(tokens.next(), tokens);
	}

	public static Object readConstant(String firstToken, Iterator<String> tokens) {
		String token = removeTrailingComma(firstToken);

		char firstChar = token.charAt(0);
		return switch (firstChar) {
			case '[' -> Type.getType(token);
			case '(' -> Type.getMethodType(token);
			case '{' -> {
				List<String> args = new ArrayList<>(4);
				for (int i = 0; i < 4; i++) {
					if (!tokens.hasNext()) yield null;

					args.add(tokens.next());
				}

				if (!args.getLast().equals("}") && !args.getLast().endsWith("},")) yield null;
				if (!args.get(0).endsWith(",") || !args.get(1).endsWith(",")) yield null;

				Integer tag = HANDLE_TAGS.get(args.get(0).substring(0, args.get(0).length() - 1).toLowerCase());
				if (tag == null) yield null;

				String[] ownerAndName = args.get(1).substring(0, args.get(1).length() - 1).split("\\.");
				if (ownerAndName.length != 2) yield null;
				String desc = args.get(2);

				yield new Handle(tag, ownerAndName[0], ownerAndName[1], desc, tag == H_INVOKESTATIC);
			}
			case '"' -> {
				StringBuilder string = new StringBuilder();
				firstToken = firstToken.substring(1);
				boolean isFirstToken = true;
				while (true) {
					if (!isFirstToken)
						string.append(' ');
					else
						isFirstToken = false;

					boolean endsWithComma = firstToken.endsWith("\",");
					if (endsWithComma || firstToken.endsWith("\"")) {
						int escapes = 0;
						while (escapes + 1 < firstToken.length() && firstToken.charAt(firstToken.length() - 2 - escapes) == '\\')
							escapes++;

						if (escapes % 2 == 0) {
							string.append(firstToken, 0, firstToken.length() - (endsWithComma ? 2 : 1));
							yield StringUnescaper.unescapeString(string.toString());
						}
					}

					string.append(firstToken);

					if (!tokens.hasNext()) yield null;
					firstToken = tokens.next();
				}
			}
			default -> {
				if (Character.isDigit(firstChar)) {
					String constantWithoutSuffix = token.substring(0, token.length() - 1);
					try {
						switch (Character.toLowerCase(token.charAt(token.length() - 1))) {
							case 'l': yield Long.parseLong(constantWithoutSuffix);
							case 'f': yield Float.parseFloat(constantWithoutSuffix);
							case 'd': yield Double.parseDouble(constantWithoutSuffix);
							default:  yield Integer.parseInt(token);
						}
					} catch (NumberFormatException e) {
						yield null;
					}
				}

				yield Type.getObjectType(token);
			}
		};
	}

	static {
		for (Field field : Opcodes.class.getDeclaredFields()) {
			if (field.getType() == int.class && field.getName().startsWith("H_")) {
				try {
					HANDLE_TAGS.put(field.getName().substring(2).toLowerCase(), field.getInt(null));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
