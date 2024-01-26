/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.reflection;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.meta.ClassMeta;

/**
 * Package related reflection.
 */
class PackageReflection {

	/**
	 * @return the package's parent.
	 */
	static Package getParentPackage(Package pkg) {
		String name = pkg.getName();
		if (!name.contains("."))
			return null;

		Package parent = Package.getPackage(name.substring(0, name.lastIndexOf(".")));
		if (parent == null) {
			// Try to load package using file provider
			ClassMeta meta = FileProvider.getClassMeta(name.substring(0, name.lastIndexOf(".")).replace('.', '/') + "/package-info.class", false);
			if (meta != null)
				return meta.load().getPackage();
		}
		return parent;
	}

}
