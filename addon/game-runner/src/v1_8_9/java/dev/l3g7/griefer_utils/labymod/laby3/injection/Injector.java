package dev.l3g7.griefer_utils.labymod.laby3.injection;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.injection.InjectorBase;
import dev.l3g7.griefer_utils.post_processor.processors.runtime.MixinPluginProcessor;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.service.IMixinService;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Injector extends InjectorBase implements IClassTransformer {

	public Injector() throws ReflectiveOperationException {
		// Add MixinPlugin processor
		List<IClassTransformer> transformers = Reflection.get(Launch.classLoader, "transformers");
		transformers.add(MixinPluginProcessor.INSTANCE);

		// Load MixinBootstrap using the system classloader
		Class<?> mixinBootstrap = Launch.classLoader.getClass().getClassLoader().loadClass("org.spongepowered.asm.launch.MixinBootstrap");
		mixinBootstrap.getDeclaredMethod("init").invoke(null);

		// Load MixinPlugin using the current classloader
		InjectorBase.class.getClassLoader().loadClass("dev.l3g7.griefer_utils.core.injection.MixinPlugin");
		InjectorBase.class.getClassLoader().loadClass("dev.l3g7.griefer_utils.core.injection.MixinPlugin$1");

		// Initialize injector
		InjectorBase.initialize(null, "LabyMod-3");

		// Finalize mixin initialization
		MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);

		if (!Reflection.exists("net.minecraftforge.common.ForgeHooks")) {
			// Account for transformers loading classes while grieferutils' mixin config is being initialised, causing the mixins not be applied // TODO what?
			Set<String> set = Reflection.get(MixinEnvironment.class, "excludeTransformers");
			set.add("net.labymod.addons.");
		} else {
			MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
		}

		// Wipe cached classes
		try {
			Class<?> mxInfoClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo");
			IMixinService classLoaderUtil0 = Reflection.get(mxInfoClass, "classLoaderUtil");
			Object classLoaderUtil = Reflection.get(classLoaderUtil0, "classLoaderUtil");
			Reflection.set(classLoaderUtil, "cachedClasses", new ConcurrentHashMap<>());
		} catch (Throwable ignored) {}
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!shouldTransform(name, transformedName))
			return basicClass;

		return super.transform(name, transformedName, basicClass);
	}

}
