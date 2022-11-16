/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.injection.transformer.transformers;

import dev.l3g7.griefer_utils.features.render.TrueSight;
import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.injection.transformer.Transformer.Target;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

/**
 * Injects {@link TrueSight#getRenderModelAlpha()} into {@link RendererLivingEntity}.
 */
@Target("net.minecraft.client.renderer.entity.RendererLivingEntity")
public class RendererLivingEntityTransformer extends Transformer {

	@Override
	protected void process() {
		MethodNode method = getMethod("renderModel", "(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V");
		ListIterator<AbstractInsnNode> it = method.instructions.iterator();
		while (it.hasNext()) {
			AbstractInsnNode node = it.next();
			if (matches(node, LDC, 0.15f)
				&& matches(it.next(), INVOKESTATIC, "net/minecraft/client/renderer/GlStateManager", "color", "(FFFF)V")) {
				it.previous();
				it.set(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(TrueSight.class), "getRenderModelAlpha", "()F", false));
			}
		}
	}

}
