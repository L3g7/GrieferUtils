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

package dev.l3g7.griefer_utils;

import dev.l3g7.griefer_utils.features.Category;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.util.Util;
import net.labymod.utils.Material;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests whether all icons accessed actually exist.
 */
public class IconTest implements Opcodes {

	private static final List<ClassNode> classes = new ArrayList<>();

	/**
	 * Load all classes
	 */
	@BeforeAll
	static void beforeAll() {
		FileProvider.getFiles().stream().filter(f -> f.endsWith(".class")).forEach(file -> {
			try (InputStream in = FileProvider.getData(file)) {
				ClassNode classNode = new ClassNode();
				new ClassReader(IOUtils.toByteArray(in)).accept(classNode, 0);

				if (classNode.name.equals(Type.getInternalName(Category.class)))
					return;

				classes.add(classNode);
			} catch (IOException e) {
				throw Util.elevate(e, "Tried to read class meta of " + file);
			}
		});
	}

	/**
	 * Tests whether all icons accessed actually exist.
	 */
	@Test
	void testIcons() {
		for (ClassNode classNode : classes) {
			for (MethodNode method : classNode.methods) {
				for (AbstractInsnNode node : method.instructions.toArray()) {
					if (node.getOpcode() != INVOKEVIRTUAL)
						continue;

					MethodInsnNode insnNode = (MethodInsnNode) node;

					if (!insnNode.name.equals("icon") || !insnNode.owner.startsWith("dev/l3g7/griefer_utils/settings/elements/"))
						continue;

					AbstractInsnNode valNode = node;
					do {
						valNode = valNode.getPrevious();
					} while (valNode instanceof LineNumberNode || valNode instanceof LabelNode);
					testResource(classNode, method, valNode);
				}
			}
		}
	}

	/**
	 * Tests whether the icon accessed by the insn node is available.
	 */
	private void testResource(ClassNode classNode, MethodNode method, AbstractInsnNode node) {
		if (node instanceof LdcInsnNode) {
			Object cst = ((LdcInsnNode) node).cst;
			assertTrue(cst instanceof String, String.valueOf(cst));
			assertTrue(FileProvider.getFiles().contains("assets/minecraft/griefer_utils/icons/" + cst + ".png"), (String) cst);
		} else if (node instanceof FieldInsnNode) {
			String owner = ((FieldInsnNode) node).owner;
			assertEquals(owner, Type.getInternalName(Material.class), owner);
		} else if (node instanceof VarInsnNode) {
			testConstructor(classNode, method, (VarInsnNode) node);
		} else {
			fail("Unknown node " + node.getClass().getSimpleName());
		}
	}

	/**
	 * Checks icons given as a constructor parameter.
	 */
	private void testConstructor(ClassNode owner, MethodNode constructor, VarInsnNode position) {
		for (ClassNode classNode : classes) {
			for (MethodNode method : classNode.methods) {
				if (!method.name.equals("<init>"))
					continue;

				for (AbstractInsnNode node : method.instructions.toArray()) {
					if (node.getOpcode() != INVOKESPECIAL)
						continue;

					MethodInsnNode insnNode = (MethodInsnNode) node;
					if (!insnNode.owner.equals(owner.name) || !insnNode.name.equals(constructor.name) || !insnNode.desc.equals(constructor.desc))
						continue;

					AbstractInsnNode n = node.getPrevious();
					for (int i = 0; i < Type.getArgumentTypes(constructor.desc).length - position.var; i++)
						n = n.getPrevious();

					testResource(classNode, method, n);
				}
			}
		}
	}

}