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

package dev.l3g7.griefer_utils.asm.processors;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.file_provider.meta.MethodMeta;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

/**
 * The processor handling editor methods annotated with {@link EditReturn}.
 */
@Singleton
public class EditReturnProcessor extends Processor {

	private static final ImmutableList<Integer> RETURN_OPCODES = ImmutableList.of(IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN);

	public @interface EditReturn { }

	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EditReturn.class;
	}

	@Override
	protected void process(ClassNode classNode, MethodTarget editor) {
		String targetName = editor.targetName;

		// Read target description is without the last parameter (being the old return value)
		String returnType = Type.getReturnType(editor.targetDesc).getDescriptor();
		String targetDesc = editor.targetDesc.replaceFirst(Pattern.quote(returnType) + "\\)" + Pattern.quote(returnType) + "$", ")" + returnType);

		// Search for method
		for (MethodNode method : classNode.methods) {
			if (method.name.equals(targetName) && method.desc.equals(targetDesc)) {
				inject(method, editor.editorMethod);
				return;
			}
		}

		throw new IllegalStateException("Could not find method " + targetName + targetDesc);
	}

	/**
	 * Injects an editor reference before all return nodes.
	 */
	private void inject(MethodNode targetMethod, MethodMeta editor) {
		InsnList injection = new InsnList();

		// Add editor parameters
		int args = Type.getArgumentTypes(targetMethod.desc).length;
		for (int i = 0; i < args + 1; i++) {
			injection.add(new VarInsnNode(ALOAD, i));
			injection.add(new InsnNode(SWAP));
		}
		injection.add(new MethodInsnNode(INVOKESTATIC, editor.owner().name, editor.name(), editor.desc(), false));

		// Insert injection before return nodes
		for (AbstractInsnNode node : targetMethod.instructions.toArray())
			if (RETURN_OPCODES.contains(node.getOpcode()))
				targetMethod.instructions.insertBefore(node, injection);
	}

}
