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

package dev.l3g7.griefer_utils.core.misc.matrix.jna.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.structures.OlmAccount;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.structures.OlmOutboundGroupSession;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.structures.OlmSession;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

public class StructureTypeAdapters {

	private static class StructureTypeAdapter<T> extends TypeAdapter<T> {

		private final Function<T, String> encryptionKeyIdSupplier;
		private final Function<T, String> serializeFunc;
		private final BiFunction<String, String, T> deserializeFunc;

		private StructureTypeAdapter(Function<T, String> encryptionKeyIdSupplier, Function<T, String> serializeFunc, BiFunction<String, String, T> deserializeFunc) {
			this.encryptionKeyIdSupplier = encryptionKeyIdSupplier;
			this.serializeFunc = serializeFunc;
			this.deserializeFunc = deserializeFunc;
		}

		@Override
		public void write(JsonWriter out, T value) throws IOException {
			String encryptionKeyId = encryptionKeyIdSupplier.apply(value);
			if (encryptionKeyId == null)
				throw new IllegalStateException("OlmAccount does not have an encryption key!");

			out.beginObject();
			out.name("keyId");
			out.value(encryptionKeyId);
			out.name("ciphertext");
			out.value(serializeFunc.apply(value));
			out.endObject();
		}

		@Override
		public T read(JsonReader in) throws IOException {
			in.beginObject();

			String keyId = null, ciphertext = null;
			while (in.hasNext()) {
				String name = in.nextName();
				if (name.equals("keyId"))
					keyId = in.nextString();
				else if (name.equals("ciphertext"))
					ciphertext = in.nextString();
			}

			in.endObject();

			return deserializeFunc.apply(keyId, ciphertext);
		}
	}

	public static final TypeAdapter<OlmAccount> OLM_ACCOUNT_ADAPTER = new StructureTypeAdapter<>(o -> o.encryptionKeyId, OlmAccount::serialize, OlmAccount::deserialize);
	public static final TypeAdapter<OlmSession> OLM_SESSION_ADAPTER = new StructureTypeAdapter<>(o -> o.encryptionKeyId, OlmSession::serialize, OlmSession::deserialize);
	public static final TypeAdapter<OlmOutboundGroupSession> OLM_OUTBOUND_GROUP_SESSION_ADAPTER = new StructureTypeAdapter<>(o -> o.encryptionKeyId, OlmOutboundGroupSession::serialize, OlmOutboundGroupSession::deserialize);

}