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

package dev.l3g7.griefer_utils.injection.transformer.transformers;

import dev.l3g7.griefer_utils.injection.transformer.Transformer;
import dev.l3g7.griefer_utils.injection.transformer.Transformer.Target;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

@Target("net.labymod.ingamegui.modules.ScoreboardModule")
public class ScoreboardModuleTransformer extends Transformer {

	@Override
	protected void process() {
		injectShouldUnlockScoreboard(getMethod("renderScoreboard", "(Lnet/minecraft/scoreboard/ScoreObjective;DDZ)V"));
	}

	public static void injectShouldUnlockScoreboard(MethodNode method) {
		ListIterator<AbstractInsnNode> it = method.instructions.iterator();
		while (it.hasNext()) {
			AbstractInsnNode node = it.next();
			if (matches(node, INVOKEINTERFACE, "java/util/List", "size", "()I") && matches(it.next(), BIPUSH, 15)) {
				JumpInsnNode jumpNode = (JumpInsnNode) it.next();

				// shouldUnlockScoreboard
				InsnList insertion = new InsnList();
				insertion.add(new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/render/scoreboard/ScoreboardHandler", "shouldUnlockScoreboard", "()Z", false));
				insertion.add(new JumpInsnNode(IFNE, jumpNode.label));
				method.instructions.insert(jumpNode, insertion);
			}
		}
	}
}