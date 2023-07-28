/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.injection;

import dev.l3g7.griefer_utils.core.mapping.Mapper;
import dev.l3g7.griefer_utils.core.mapping.Mapping;
import net.labymod.core.asm.LabyModCoreMod;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {

	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (LabyModCoreMod.isObfuscated())
			targetClassName = Mapper.mapClass(targetClassName.replace('.', '/'), Mapping.UNOBFUSCATED, Mapping.OBFUSCATED);
		return Launch.classLoader.findResource(targetClassName.replace('.', '/') + ".class") != null;
	}

	public void onLoad(String mixinPackage) {}
	public String getRefMapperConfig() { return null; }
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	public List<String> getMixins() { return null; }
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

}
