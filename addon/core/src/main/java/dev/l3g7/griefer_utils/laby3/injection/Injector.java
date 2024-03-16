package dev.l3g7.griefer_utils.laby3.injection;

import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.injection.InjectorBase;
import net.minecraft.launchwrapper.IClassTransformer;
import org.spongepowered.asm.service.IMixinService;

import java.util.concurrent.ConcurrentHashMap;

public class Injector extends InjectorBase implements IClassTransformer {

	public Injector() {
		InjectorBase.initialize(null);

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
