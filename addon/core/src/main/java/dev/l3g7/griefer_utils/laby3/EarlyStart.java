package dev.l3g7.griefer_utils.laby3;

import dev.l3g7.griefer_utils.api.bridges.Bridge;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.mapping.Mapper;
import dev.l3g7.griefer_utils.api.misc.LibLoader;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.api.util.IOUtil;
import dev.l3g7.griefer_utils.laby3.injection.Injector;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.core.asm.LabyModTransformer;
import net.labymod.core.asm.mappings.Minecraft18MappingImplementation;
import net.labymod.core.asm.mappings.UnobfuscatedImplementation;
import net.minecraft.launchwrapper.Launch;

import java.io.IOException;
import java.util.Map;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.MINECRAFT_1_8_9;

public class EarlyStart {

	public static void start() throws IOException, ReflectiveOperationException {
		Bridge.Initializer.init(LABY_3, MINECRAFT_1_8_9);

		// Load mcp mappings for automatic name resolution in Reflection
		Mapper.loadMappings("1.8.9", "22");

		// Load and inject libraries
		LibLoader.loadLibraries(
			// mXparser: for evaluating expressions (Calculator)
			"https://repo1.maven.org/maven2",
			"org/mariuszgromada/math", "MathParser.org-mXparser", "5.1.0",
			"B5472B5E1BBEFEA2DA6052C68A509C84C7F2CA5F99B76A4C5F58354C08818630",

			// ZXing: for reading qr codes (QRCodeScanner)
			"https://repo1.maven.org/maven2",
			"com/google/zxing", "core", "3.5.1",
			"1BA7C0FBB6C267E2FB74E1497D855ADAE633CCC98EDC8C75163AA64BC08E3059",

			// Mixin: for modifying other classes (core.injection)
			"https://repo.spongepowered.org/repository/maven-public",
			"org/spongepowered", "mixin", "0.7.11-SNAPSHOT", "mixin-0.7.11-20180703.121122-1.jar",
			"DA3D6E47B9C12B5A312D89B67BC27E2429D823C09CDE8A90299E9FDCC4EEFC20"
		);

		// Sets LabyMod's mapping adapter
		// It's usually set in the MinecraftVisitor, but since Mixin changes the transformer order (i think),
		// transformers from LabyMod addons may be loaded before the MinecraftVisitor, causing the adapter to be null
		// and causing a crash if any addon tries to map something.
		Reflection.set(LabyModTransformer.class, "mappingImplementation", LabyModCoreMod.isObfuscated() ? new Minecraft18MappingImplementation() : new UnobfuscatedImplementation());

		// Cache classes with overwritten versions so Forge can read them
		// Forge's remapper loads the classes using getClassBytes, and puts them in a ClassReader, so a version of the
		// classes with a modified major version have to be loaded and cached manually to prevent crashes
		Map<String, byte[]> resourceCache = Reflection.get(Launch.classLoader, "resourceCache");

		for (String file : FileProvider.getFiles(f -> f.endsWith(".class"))) {
			byte[] bytes = IOUtil.toByteArray(FileProvider.getData(file));
			bytes[7 /* major_version */] = 52 /* Java 1.8 */;
			resourceCache.put(file.substring(0, file.length() - 6), bytes);
		}

		// Add Injector as transformer
		Launch.classLoader.registerTransformer(Injector.class.getName());
		// TODO ForgeModWarning.loadedUsingLabyMod = true;
	}

}
