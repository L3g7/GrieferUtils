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

package dev.l3g7.griefer_utils.core.injection.transformer;

import dev.l3g7.griefer_utils.core.mapping.Mapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

import static dev.l3g7.griefer_utils.core.mapping.Mapping.OBFUSCATED;
import static dev.l3g7.griefer_utils.core.mapping.Mapping.UNOBFUSCATED;

/**
 * A Bytecode transformer.
 */
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
		String targetMethod;

		if (Mapper.isObfuscated())
			targetMethod = Mapper.mapMethod(target.replace('.', '/'), name, desc, UNOBFUSCATED, OBFUSCATED);
		else
			targetMethod = name + desc;

		return classNode.methods.stream()
			.filter(m -> targetMethod.equals(m.name + m.desc))
			.findFirst()
			.orElseThrow(() -> new NoSuchMethodError("Could not find " + name + desc + " / " + targetMethod + "!"));
	}

	protected static boolean matches(AbstractInsnNode node, int opcode, Object... args) {
		if (node.getOpcode() != opcode)
			return false;

		switch (opcode) {
			case LDC: {
				assert args.length == 1;
				LdcInsnNode insn = (LdcInsnNode) node;
				return Objects.equals(insn.cst, args[0]);
			}
			case BIPUSH: {
				assert args.length == 1;
				IntInsnNode insn = (IntInsnNode) node;
				return Objects.equals(insn.operand, args[0]);
			}
			case GETSTATIC: {
				assert args.length == 3;
				FieldInsnNode insn = (FieldInsnNode) node;

				if (Mapper.isObfuscated())
					return insn.owner.equals(Mapper.mapClass((String) args[0], UNOBFUSCATED, OBFUSCATED))
						&& insn.name.equals(Mapper.mapField((String) args[0], (String) args[1], UNOBFUSCATED, OBFUSCATED));

				return insn.owner.equals(args[0])
					&& insn.name.equals(args[1]);
			}
			case INVOKEINTERFACE:
			case INVOKESTATIC:
			case INVOKEVIRTUAL: {
				assert args.length == 3;
				MethodInsnNode insn = (MethodInsnNode) node;

				if (Mapper.isObfuscated())
					return insn.owner.equals(Mapper.mapClass((String) args[0], UNOBFUSCATED, OBFUSCATED))
						&& (insn.name + insn.desc).equals(Mapper.mapMethod((String) args[0], (String) args[1], (String) args[2], UNOBFUSCATED, OBFUSCATED));

				return insn.owner.equals(args[0])
					&& insn.name.equals(args[1])
					&& insn.desc.equals(args[2]);
			}
			default:
				throw new UnsupportedOperationException("matches for " + opcode + " not implemented!");
		}
	}

	public String getTarget() {
		return target;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Target {
		String value();
	}
}