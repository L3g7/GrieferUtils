package dev.l3g7.griefer_utils.features.uncategorized.scripts;

import dev.l3g7.griefer_utils.core.api.misc.StringUnescaper;
import dev.l3g7.griefer_utils.features.uncategorized.scripts.Scripts.ScriptSyntaxException;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.util.*;

import static dev.l3g7.griefer_utils.features.uncategorized.scripts.Util.removeTrailingComma;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

public class ConstantParser {

	private static final String CONSTANT_COULD_NOT_BE_READ = "Konstante konnte nicht gelesen werden";

	private static final Map<String, Integer> HANDLE_TAGS = new HashMap<>();

	public static Object readConstant(Iterator<String> tokens) throws ScriptSyntaxException {
		if (!tokens.hasNext())
			throw new ScriptSyntaxException(CONSTANT_COULD_NOT_BE_READ);

		return readConstant(tokens.next(), tokens);
	}

	public static String readString(String firstToken, Iterator<String> tokens) throws ScriptSyntaxException {
		if (!firstToken.startsWith("\""))
			throw new ScriptSyntaxException(CONSTANT_COULD_NOT_BE_READ);

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
					return StringUnescaper.unescapeString(string.toString());
				}
			}

			string.append(firstToken);

			if (!tokens.hasNext())
				throw new ScriptSyntaxException(CONSTANT_COULD_NOT_BE_READ);

			firstToken = tokens.next();
		}
	}

	public static Object readConstant(String firstToken, Iterator<String> tokens) throws ScriptSyntaxException {
		String token = removeTrailingComma(firstToken);

		char firstChar = token.charAt(0);
		return switch (firstChar) {
			case '[' -> Type.getType(token);
			case '(' -> Type.getMethodType(token);
			case '{' -> {
				List<String> args = new ArrayList<>(4);
				for (int i = 0; i < 4; i++) {
					if (!tokens.hasNext())
						throw new ScriptSyntaxException(CONSTANT_COULD_NOT_BE_READ);

					args.add(tokens.next());
				}

				if (!args.get(0).endsWith(",") || !args.get(1).endsWith(",") || !args.getLast().equals("}") && !args.getLast().endsWith("},"))
					throw new ScriptSyntaxException(CONSTANT_COULD_NOT_BE_READ);

				Integer tag = HANDLE_TAGS.get(args.get(0).substring(0, args.get(0).length() - 1).toLowerCase());
				String[] ownerAndName = args.get(1).substring(0, args.get(1).length() - 1).split("\\.");
				if (tag == null || ownerAndName.length != 2)
					throw new ScriptSyntaxException(CONSTANT_COULD_NOT_BE_READ);
				String desc = args.get(2);

				yield new Handle(tag, ownerAndName[0], ownerAndName[1], desc, tag == H_INVOKESTATIC);
			}
			case '"' -> readString(firstToken, tokens);
			default -> {
				if (Character.isDigit(firstChar)) {
					String constantWithoutSuffix = token.substring(0, token.length() - 1);
					try {
						yield switch (Character.toLowerCase(token.charAt(token.length() - 1))) {
							case 'l' -> Long.parseLong(constantWithoutSuffix);
							case 'f' -> Float.parseFloat(constantWithoutSuffix);
							case 'd' -> Double.parseDouble(constantWithoutSuffix);
							default -> Integer.parseInt(token);
						};
					} catch (NumberFormatException e) {
						throw new ScriptSyntaxException(CONSTANT_COULD_NOT_BE_READ, e);
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
