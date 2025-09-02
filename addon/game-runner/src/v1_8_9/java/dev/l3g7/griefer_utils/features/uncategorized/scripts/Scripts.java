package dev.l3g7.griefer_utils.features.uncategorized.scripts;

import dev.l3g7.griefer_utils.core.api.misc.Pair;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scripts {

	private static final Map<Path, Pair<Integer, Method>> LOADED_SCRIPTS = new HashMap<>();

	public static void run(Path path, Object[] args) throws Throwable {
		if (!Files.exists(path))
			throw new ScriptNotFoundException();

		List<String> lines = Files.readAllLines(path);
		if (lines.isEmpty())
			return;

		Pair<Integer, Method> currentScript = LOADED_SCRIPTS.get(path);

		int hash = lines.hashCode();
		if (currentScript == null || hash != currentScript.a) {
			LOADED_SCRIPTS.remove(path);

			ClassNode classNode = Script.load(lines);
			classNode.sourceFile = path.toString().replace('\\', '/');
			Class<?> clazz;
			try {
				clazz = Util.loadClass(classNode);
			} catch (Throwable t) {
				throw new ScriptSyntaxException(t);
			}
			Method method = clazz.getMethod("main", Object[].class);
			LOADED_SCRIPTS.put(path, currentScript = new Pair<>(hash, method));
		}

		currentScript.b.invoke(null, new Object[]{ args });
	}

	public static class ScriptNotFoundException extends Exception {}
	public static class ScriptSyntaxException extends Exception {

		public String lineInfo = null;

		public ScriptSyntaxException(String message) {
			super(message);
		}

		public ScriptSyntaxException(Throwable cause) {
			super(cause.getClass().getSimpleName() + ": " + cause.getMessage(), cause);
		}

		public ScriptSyntaxException(String message, Throwable cause) {
			super(message, cause);
		}

		public void setLine(int lineNumber, String line) {
			lineInfo = "in Zeile " + lineNumber + " (" + line + ")";
		}

	}

}