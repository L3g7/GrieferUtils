/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.file_provider.meta;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * An interface for meta information containing annotations.
 */
public interface IMeta extends Opcodes {

	List<AnnotationMeta> annotations();
	int modifiers();

	/**
	 * @return whether an annotation with the given description exists.
	 */
	default boolean hasAnnotation(String annotationDesc) {
		return getAnnotation(annotationDesc) != null;
	}

	default boolean hasAnnotation(Class<? extends Annotation> type) {
		return getAnnotation(type) != null;
	}

	/**
	 * @return the annotation with the given description.
	 */
	default AnnotationMeta getAnnotation(String annotationDesc) {
		return annotations().stream().filter(meta -> meta.desc.equals(annotationDesc)).findFirst().orElse(null);
	}

	default AnnotationMeta getAnnotation(Class<? extends Annotation> type) {
		return getAnnotation(Type.getDescriptor(type));
	}


	default boolean isStatic() {
		return (modifiers() & ACC_STATIC) != 0;
	}

	default boolean isAbstract() {
		return (modifiers() & ACC_ABSTRACT) != 0;
	}

}
