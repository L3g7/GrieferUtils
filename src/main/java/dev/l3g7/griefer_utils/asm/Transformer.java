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

package dev.l3g7.griefer_utils.asm;

import dev.l3g7.griefer_utils.asm.editors.Editor;
import dev.l3g7.griefer_utils.asm.processors.Processor;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.file_provider.meta.MethodMeta;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * The bytecode transformer for GrieferUtils.
 */
public class Transformer implements IClassTransformer {

	private final Map<String, List<ClassMeta>> editors = new HashMap<>();

	public Transformer() {
		for (ClassMeta editor : FileProvider.getClassesWithSuperClass(Editor.class)) {

			String target = getTargetClass(editor.signature);
			for (MethodNode method : editor.asmNode.methods)
				Processor.preProcess(target, new MethodMeta(editor, method));

			String targetClass = getTargetClass(editor.signature).replace('/', '.');
			editors.computeIfAbsent(targetClass, t -> new ArrayList<>()).add(editor);
		}
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] rawData) {
		// Read raw data to node
		ClassNode node = new ClassNode();
		new ClassReader(rawData).accept(node, SKIP_CODE);
		boolean isEditor = Type.getInternalName(Editor.class).equals(new ClassMeta(node).superName);

		if (isEditor) {
			return modify(rawData, classNode -> {
				ClassMeta classMeta = new ClassMeta(classNode);
				String target = getTargetClass(classNode.signature);
				for (MethodNode method : classNode.methods)
					Processor.preProcess(target, new MethodMeta(classMeta, method));
			});
		}

		if (editors.containsKey(name)) {
			return modify(rawData, classNode -> {
				for (ClassMeta editor : editors.get(name))
					Processor.process(classNode, editor);
			});
		}

		return rawData;
	}

	/**
	 * Reads the given data, modifies it using the given consumer and returns the resulting raw bytes.
	 */
	private byte[] modify(byte[] rawData, Consumer<ClassNode> modifier) {
		// Read raw data to node
		ClassNode classNode = new ClassNode();
		new ClassReader(rawData).accept(classNode, 0);

		// Modify node
		modifier.accept(classNode);

		// Write node to raw data
		ClassWriter writer = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private final Pattern targetPattern = Pattern.compile("^L" + Type.getInternalName(Editor.class) + "<L([^;]+);>;$");

	/**
	 * @param editorSignature the signature of the editor class.
	 * @return The class targeted by the editor.
	 */
	private String getTargetClass(String editorSignature) {
		if (editorSignature == null)
			throw new IllegalStateException("no signature!");

		Matcher matcher = targetPattern.matcher(editorSignature);
		if (!matcher.matches())
			throw new IllegalStateException(editorSignature + " is an invalid signature!");

		return matcher.group(1);
	}

}
