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

package dev.l3g7.griefer_utils.core.injection.transformer.transformers;

import dev.l3g7.griefer_utils.event.events.render.RenderBarrierCheckEvent;
import dev.l3g7.griefer_utils.core.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.core.injection.transformer.Transformer.Target;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.common.MinecraftForge;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

/**
 * Injects the {@link RenderBarrierCheckEvent} into {@link WorldClient#doVoidFogParticles(int, int, int)}.
 */
@Target("net.minecraft.client.multiplayer.WorldClient")
public class WorldClientTransformer extends Transformer {

	@Override
	protected void process() {
		MethodNode method = getMethod("doVoidFogParticles", "(III)V");
		ListIterator<AbstractInsnNode> it = method.instructions.iterator();
		while (it.hasNext()) {
			AbstractInsnNode node = it.next();
			if (matches(node, INVOKEINTERFACE, "net/minecraft/block/state/IBlockState", "getBlock", "()Lnet/minecraft/block/Block;")
				&& matches(node.getNext(), GETSTATIC, "net/minecraft/init/Blocks", "barrier", "Lnet/minecraft/block/Block;")) {
				AbstractInsnNode jumpNode =
					node           // iblockstate.getBlock()
					.getPrevious() // flag
					.getPrevious();// IFEQ L17

				LabelNode blockCheckLabel = new LabelNode();
				method.instructions.insert(jumpNode, blockCheckLabel);

				InsnList insns = new InsnList();
				insns.add(new JumpInsnNode(IFNE, blockCheckLabel));
				insns.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(WorldClientTransformer.class), "shouldRenderBarriers", "()Z", false));
				method.instructions.insertBefore(jumpNode, insns);
			}
		}
	}

	public static boolean shouldRenderBarriers() {
		return MinecraftForge.EVENT_BUS.post(new RenderBarrierCheckEvent());
	}

}
