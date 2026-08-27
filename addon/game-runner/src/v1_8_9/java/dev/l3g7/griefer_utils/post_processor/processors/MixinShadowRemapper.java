package dev.l3g7.griefer_utils.post_processor.processors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.AnnotationMeta;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.FieldMeta;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.MethodMeta;
import dev.l3g7.griefer_utils.core.api.mapping.MappingLoader;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.post_processor.LatePostProcessor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

/**
 * Remaps @Shadow fields and methods in Mixins.
 * Injected by the {@link MappingLoader} after the mappings have been loaded.
 */
public class MixinShadowRemapper extends LatePostProcessor.Processor {

	private final Map<String, Map<String, String>> mappings = new HashMap<>(); // class name -> member name -> new member name

	public MixinShadowRemapper() {
		JsonObject refMap = IO.read(FileProvider.getData("assets/griefer_utils/refmap-labymod-3.json")).asJsonObject();
		JsonObject notch = refMap.getAsJsonObject("data").getAsJsonObject("notch");

		for (Map.Entry<String, JsonElement> classEntry : notch.entrySet()) {
			JsonObject classMapping = classEntry.getValue().getAsJsonObject();

			Map<String, String> members = new HashMap<>();
			for (Map.Entry<String, JsonElement> member : classMapping.entrySet()) {
				if (!member.getKey().startsWith("<GU>"))
					continue;

				String keyName = getName(member.getKey().substring("<GU>".length()));
				if (keyName != null)
					members.put(keyName, getName(member.getValue().getAsString()));
			}

			if (!members.isEmpty())
				mappings.put(classEntry.getKey(), members);
		}
	}

	private static String getName(String mapping) {
		int nameEnd = mapping.indexOf(':');
		if (nameEnd == -1)
			nameEnd = mapping.indexOf('(');
		if (nameEnd == -1)
			return null;

		int ownerEnd = mapping.indexOf(';');
		if (ownerEnd > nameEnd)
			ownerEnd = -1;

		return mapping.substring(ownerEnd + 1, nameEnd);
	}

	@Override
	public void process(ClassNode classNode) {
		Map<String, String> mappings = this.mappings.get(classNode.name);
		if (mappings == null)
			return; // No fields to map

		Map<String, FieldMeta> renamedFields = new HashMap<>();
		Map<String, MethodMeta> renamedMethods = new HashMap<>();

		ClassMeta classMeta = new ClassMeta(classNode);

		for (FieldMeta field : classMeta.fields) {
			AnnotationMeta shadow = field.getAnnotation(Shadow.class);
			if (shadow != null && !Boolean.FALSE.equals(shadow.getRawValue("remap"))) {
				String newName = mappings.get(field.name());
				if (newName == null)
					continue; // Not mapped

				renamedFields.put(field.name(), field);
				field.asmNode.name = newName;
				setModified();
			}
		}

		for (MethodMeta method : classMeta.methods) {
			AnnotationMeta shadow = method.getAnnotation(Shadow.class);
			if (shadow != null && !Boolean.FALSE.equals(shadow.getRawValue("remap"))) {
				String newName = mappings.get(method.name());
				if (newName == null)
					continue; // Not mapped

				renamedMethods.put(method.name(), method);
				method.asmNode.name = newName;
				setModified();
			}
		}

		if (renamedFields.isEmpty() && renamedMethods.isEmpty())
			return;

		// Rename in instructions
		for (MethodMeta method : classMeta.methods) {
			if (method.isAbstract())
				continue;

			for (AbstractInsnNode instruction : method.asmNode.instructions) {
				if (instruction instanceof FieldInsnNode f && f.owner.equals(classMeta.name)) {
					FieldMeta fieldMeta = renamedFields.get(f.name);
					if (fieldMeta != null)
						f.name = fieldMeta.name();
				} else if (instruction instanceof MethodInsnNode m && m.owner.equals(classMeta.name)) {
					MethodMeta methodMeta = renamedMethods.get(m.name);
					if (methodMeta != null)
						m.name = methodMeta.name();
				}
			}
		}
	}

}
