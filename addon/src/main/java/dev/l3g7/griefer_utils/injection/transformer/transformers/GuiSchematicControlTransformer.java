/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

@Target("com.github.lunatrius.schematica.client.gui.control.GuiSchematicControl")
public class GuiSchematicControlTransformer extends Transformer {


	@Override
	protected void process() {
		MethodNode methodNode = getMethod("func_73866_w_", "()V");
		ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
		while (iterator.hasNext()) {
			AbstractInsnNode node = iterator.next();
			if (node.getOpcode() != INVOKEINTERFACE)
				continue;

			MethodInsnNode min = (MethodInsnNode) node;
			if (!min.name.equals("clear"))
				continue;

			iterator.next();
			iterator.add(new VarInsnNode(ALOAD, 0));
			iterator.add(new FieldInsnNode(GETFIELD, "com/github/lunatrius/schematica/client/gui/control/GuiSchematicControl", "field_146292_n", "Ljava/util/List;"));
			iterator.add(new VarInsnNode(ALOAD, 0));
			iterator.add(new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/SaveSchematicaPosition", "addGuiButton", "(Ljava/util/List;Lnet/minecraft/client/gui/GuiScreen;)V", false));
			break;
		}

		methodNode = getMethod("func_146284_a", "(Lnet/minecraft/client/gui/GuiButton;)V");
		iterator = methodNode.instructions.iterator();
		iterator.add(new VarInsnNode(ALOAD, 1));
		iterator.add(new VarInsnNode(ALOAD, 0));
		iterator.add(new MethodInsnNode(INVOKESTATIC, "dev/l3g7/griefer_utils/features/world/better_schematica/SaveSchematicaPosition", "onActionPerformed", "(Lnet/minecraft/client/gui/GuiButton;Lnet/minecraft/client/gui/GuiScreen;)V", false));
	}

}
