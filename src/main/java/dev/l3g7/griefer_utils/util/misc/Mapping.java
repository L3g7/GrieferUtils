package dev.l3g7.griefer_utils.util.misc;

import com.google.gson.reflect.TypeToken;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.core.asm.LabyModCoreMod;
import org.objectweb.asm.Type;

import java.io.InputStreamReader;
import java.util.Map;

/**
 * A provider for MCP -> Notch and MCP -> SRG mappings.
 */
public class Mapping {

	private static final boolean obfuscated = LabyModCoreMod.isObfuscated();

	public static String mapClass(MappingTarget target, String descriptor) {
		Type type = Type.getType(descriptor);
		if (type.getSort() != Type.OBJECT)
			return descriptor;

		if (!obfuscated)
			return descriptor;

		descriptor = type.getInternalName();
		if (!descriptor.startsWith("net/minecraft"))
			return descriptor;
		if (!target.mappings.containsKey(descriptor))
			throw new NoClassDefFoundError("Could not find mapping for class " + descriptor);

		return Type.getObjectType(target.mappings.get(descriptor).notch).getDescriptor();
	}

	public static String mapField(MappingTarget target, String owner, String name) {
		if (!obfuscated)
			return name;

		if (!owner.startsWith("net/minecraft"))
			return name;
		if (!target.mappings.containsKey(owner))
			throw new NoClassDefFoundError("Could not find mapping for class " + owner);

		return target.mappings.get(owner).fields.get(name);
	}

	public static String mapMethodName(MappingTarget target, String owner, String name, String desc) {
		owner = owner.replace('.', '/');
		if (!obfuscated)
			return name;

		if (!owner.startsWith("net/minecraft"))
			return name;
		if (!target.mappings.containsKey(owner))
			throw new NoClassDefFoundError("Could not find mapping for class " + owner);

		return target.mappings.get(owner).methods.get(name + desc);
	}

	public static String mapMethodDesc(MappingTarget target, String desc) {
		if (!obfuscated)
			return desc;

		StringBuilder mappedDesc = new StringBuilder("(");
		for (Type type : Type.getArgumentTypes(desc))
			mappedDesc.append(mapClass(target, type.getDescriptor()));

		mappedDesc.append(")");
		Type type = Type.getReturnType(desc);
		mappedDesc.append(mapClass(target, type.getDescriptor()));
		return mappedDesc.toString();
	}

	private static class MappingClass {
		String notch;
		Map<String, String> methods;
		Map<String, String> fields;
	}

	public enum MappingTarget {
		NOTCH(IOUtil.gson.fromJson(new InputStreamReader(FileProvider.getData("mcp_notch_mappings.json")), new TypeToken<Map<String, MappingClass>>() {}.getType())),
		SRG(IOUtil.gson.fromJson(new InputStreamReader(FileProvider.getData("mcp_srg_mappings.json")), new TypeToken<Map<String, MappingClass>>() {}.getType()));

		private final Map<String, MappingClass> mappings;
		MappingTarget(Map<String, MappingClass> mappings) {
			this.mappings = mappings;
		}
	}

}
