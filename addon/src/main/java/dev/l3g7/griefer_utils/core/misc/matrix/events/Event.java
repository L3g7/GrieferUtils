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

package dev.l3g7.griefer_utils.core.misc.matrix.events;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.misc.matrix.MatrixUtil;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.sync.SyncResponse;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.misc.matrix.MatrixUtil.GSON;

@Retention(RetentionPolicy.RUNTIME)
public @interface Event {

	String key();

	boolean asContentType() default true;

	abstract class EventContent {

		public abstract void handle(Session session);

	}

	class EventRegistry {

		private static final Map<String, ClassMeta> contentClasses = new HashMap<>();
		static {
			for (ClassMeta event : FileProvider.getClassesWithSuperClass(EventContent.class)) {
				if (!event.hasAnnotation(Event.class))
					continue;

				String key = event.getAnnotation(Event.class).getValue("key", false);
				contentClasses.put(key, event);
			}
		}

		public static EventContent getContent(SyncResponse.RawEvent event) {
			ClassMeta contentClassMeta = contentClasses.entrySet().stream()
				.filter(k -> k.getKey().matches(event.type))
				.findFirst()
				.map(Map.Entry::getValue)
				.orElse(null);

			if (contentClassMeta == null)
				return null;

			Class<? extends EventContent> contentClass = contentClassMeta.load();
			Object content = contentClass.getAnnotation(Event.class).asContentType() ? event.content : event;
			return GSON.fromJson(GSON.toJson(content), contentClass);
		}

	}

}