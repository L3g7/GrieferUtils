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

package dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log;

import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class LogEntry {

	private BooleanSetting setting;

	public BooleanSetting getSetting() {
		if (setting != null)
			return setting;

		Field[] mainElementFields = Reflection.getAnnotatedFields(getClass(), MainElement.class, false);
		if (mainElementFields.length != 1)
			throw new IllegalStateException("Found an invalid amount of main elements for " + getClass().getSimpleName());

		setting = Reflection.get(this, mainElementFields[0]);
		return setting;
	}

	public abstract void addEntry(ZipOutputStream zip) throws IOException;

	protected static <T> String nullSafeOp(T t, Function<T, String> op) {
		return t == null ? "<null>" : op.apply(t);
	}

	protected static void includeFile(ZipOutputStream zip, File file, String name) throws IOException {
		if (file == null || !file.exists() || !file.isFile())
			return;

		zip.putNextEntry(new ZipEntry(name));
		try (InputStream in = Files.newInputStream(file.toPath())) {
			IOUtils.copy(in, zip);
		}
		zip.closeEntry();
	}
}