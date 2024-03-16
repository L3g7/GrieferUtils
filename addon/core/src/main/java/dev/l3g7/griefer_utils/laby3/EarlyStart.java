package dev.l3g7.griefer_utils.laby3;

import dev.l3g7.griefer_utils.api.bridges.Bridge;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.mapping.Mapper;
import dev.l3g7.griefer_utils.api.misc.LibLoader;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.core.asm.LabyModTransformer;
import net.labymod.core.asm.mappings.Minecraft18MappingImplementation;
import net.labymod.core.asm.mappings.UnobfuscatedImplementation;
import net.minecraft.launchwrapper.Launch;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.MINECRAFT_1_8_9;

public class EarlyStart {

	public static void start() {
		FileProvider.addPreprocessor(PreStart.Java17to8Transpiler::preprocess);

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

		// Add Injector as transformer
		Launch.classLoader.registerTransformer("dev.l3g7.griefer_utils.laby3.injection.Injector");

		// TODO ForgeModWarning.loadedUsingLabyMod = true;
	}

}
