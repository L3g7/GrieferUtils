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

import dev.l3g7.griefer_utils.asm.editors.Editor;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.file_provider.meta.MethodMeta;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

/**
 * The processor base.
 */
public abstract class Processor implements Opcodes {

	/**
	 * @return the annotation marking the editor methods targeted by the processor.
	 */
	protected abstract Class<? extends Annotation> getAnnotation();
	protected abstract void process(ClassNode classNode, MethodTarget editor);

	/**
	 * Processes all editor methods using the processor targeting them.
	 * @see Processor#getAnnotation()
	 */
	public static void process(ClassNode node, ClassMeta editor) {
		for (ClassMeta processorMeta : FileProvider.getClassesWithSuperClass(Processor.class)) {
			Processor processor = FileProvider.getSingleton(processorMeta.load());

			for (MethodMeta editMethod : editor.methods) {
				if (editMethod.name().equals("<init>"))
					continue;

				if (editMethod.hasAnnotation(processor.getAnnotation()))
					processor.process(node, new MethodTarget(node.name, editMethod));
			}
		}
	}

	/**
	 * Makes the editor method public and static and resolves {@link Editor#self} references.
	 */
	public static void preProcess(String targetClass, MethodMeta editorMtd) {
		if (editorMtd.name().equals("<init>"))
			return;

		MethodNode mtdNode = editorMtd.asmNode;

		// Make editor method accessible
		mtdNode.access &= ~(ACC_PRIVATE | ACC_PROTECTED);
		mtdNode.access |= ACC_PUBLIC | ACC_STATIC;

		// Update Editor#self references
		mtdNode.desc = mtdNode.desc.replaceFirst("\\(", "(L" + targetClass + ";");

		for (AbstractInsnNode node : mtdNode.instructions.toArray()) {
			if (node.getOpcode() == GETFIELD) {
				FieldInsnNode fieldNode = (FieldInsnNode) node;
				if (fieldNode.name.equals("self") && fieldNode.desc.equals("Ljava/lang/Object;")
					&& (fieldNode.owner.equals(Type.getInternalName(Editor.class)) || fieldNode.owner.equals(editorMtd.owner().name)))
					// Keep 'ALOAD 0' required by GETFIELD
					// Making the editor static results in the first parameter being referenced.
					mtdNode.instructions.remove(fieldNode);
			}
		}
	}

	protected static class MethodTarget {

		public final MethodMeta editorMethod;
		public final String targetName, targetDesc;

		public MethodTarget(String targetClass, MethodMeta editorMethod) {
			this.editorMethod = editorMethod;
			MethodNode mtdNode = editorMethod.asmNode;

			// remove the "edit" in front of the method name
			if (!mtdNode.name.startsWith("edit") || mtdNode.name.length() < 5)
				throw new IllegalStateException(mtdNode.name + " is an invalid name!");
			this.targetName = Character.toLowerCase(mtdNode.name.charAt(4)) + mtdNode.name.substring(5);

			// Remove first paramter from descriptor
			this.targetDesc = mtdNode.desc.replaceFirst("\\(L" + Pattern.quote(targetClass) + ";", "(");
		}
	}
}
