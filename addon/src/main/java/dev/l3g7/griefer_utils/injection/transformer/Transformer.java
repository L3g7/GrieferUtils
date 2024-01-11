/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.injection.transformer;

import dev.l3g7.griefer_utils.core.misc.functions.Function;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ListIterator;

public abstract class Transformer implements Opcodes {

	protected ClassNode classNode;
	protected final String target;

	protected Transformer() {
		this.target = getClass().getDeclaredAnnotation(Target.class).value();
	}

	public void transform(ClassNode node) {
		this.classNode = node;
		process();
	}

	protected abstract void process();

	protected MethodNode getMethod(String name, String desc) {
		String targetMethod = name + desc;

		return classNode.methods.stream()
			.filter(m -> targetMethod.equals(m.name + m.desc))
			.findFirst()
			.orElseThrow(() -> new NoSuchMethodError("Could not find " + name + desc + " / " + targetMethod + "!"));
	}
	protected ListIterator<AbstractInsnNode> getIterator(MethodNode method, int opcode, String methodName) {
		return getIterator(method, opcode, m -> ((MethodInsnNode) m).name.equals(methodName));
	}

	protected ListIterator<AbstractInsnNode> getIterator(MethodNode method, int opcode, Function<AbstractInsnNode, Boolean> nodeValidator) {
		ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();

		while (iterator.hasNext()) {
			AbstractInsnNode node = iterator.next();
			if (node.getOpcode() == opcode && nodeValidator.apply(node))
				return iterator;
		}

		throw new IllegalStateException("Could not generate iterator!");
	}

	public String getTarget() {
		return target;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Target {
		String value();
	}

}