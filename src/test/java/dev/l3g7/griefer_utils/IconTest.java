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
import org.junit.jupiter.api.AfterAll;
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
	private static final List<String> unusedIcons = new ArrayList<>();

	/**
	 * Loads all classes and icons.
	 */
	@BeforeAll
	static void loadResources() {
		// Load classes
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

		// Load icons
		FileProvider.getFiles().stream().filter(f -> f.startsWith("assets/minecraft/griefer_utils/icons/")).forEach(file -> {
			if (!file.endsWith("/README.md"))
				unusedIcons.add(file);
		});

	}

	/**
	 * Tests whether all icons accessed actually exist.
	 */
	@Test
	void testIcons() {
		for (ClassNode classNode : classes) {

			// Check category icons
			if (classNode.name.endsWith("package-info")) {
				for (AnnotationNode node : classNode.visibleAnnotations) {
					if (!node.desc.equals(Type.getDescriptor(Category.Meta.class)))
						continue;
					List<Object> values = node.values;
					for (int i = 0; i < values.size(); i += 2)
						if (values.get(i).equals("icon"))
							testResource((String) values.get(i + 1));
				}
			}

			// Check setting icons
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
					testInsnNode(classNode, method, valNode);
				}
			}
		}
	}

	/**
	 * Tests whether all available icons are used.
	 */
	@AfterAll
	static void checkUnusedIcons() {
		for (String unusedIcon : unusedIcons)
			fail("Unused icon " + unusedIcon);
	}

	/**
	 * Tests whether the icon accessed by the insn node is available.
	 */
	private void testInsnNode(ClassNode classNode, MethodNode method, AbstractInsnNode node) {
		if (node instanceof LdcInsnNode) {
			// Resource icons
			Object cst = ((LdcInsnNode) node).cst;
			assertTrue(cst instanceof String, String.valueOf(cst));
			testResource((String) cst);
		} else if (node instanceof FieldInsnNode) {
			// Material icons
			String owner = ((FieldInsnNode) node).owner;
			assertEquals(owner, Type.getInternalName(Material.class), owner);
		} else if (node instanceof MethodInsnNode) {
			// ItemStack icons
			MethodInsnNode m = (MethodInsnNode) node;
			String check = m.owner + "." + m.name + m.desc;
			assertTrue(check.matches("net/minecraft/item/ItemStack.<init>\\(Lnet/minecraft/(?:block/Block|item/Item);I?I?\\)V"), check);
		} else if (node instanceof VarInsnNode) {
			// Constructor icons
			testConstructor(classNode, method, (VarInsnNode) node);
		} else {
			fail("Unknown node " + node.getClass().getSimpleName());
		}
	}

	/**
	 * Tests icons given as a constructor parameter.
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

					testInsnNode(classNode, method, n);
				}
			}
		}
	}

	/**
	 * Tests resource icons.
	 */
	private void testResource(String path) {
		assertTrue(FileProvider.getFiles().contains("assets/minecraft/griefer_utils/icons/" + path + ".png"), path + ".png");
		unusedIcons.remove("assets/minecraft/griefer_utils/icons/" + path + ".png");
	}
}