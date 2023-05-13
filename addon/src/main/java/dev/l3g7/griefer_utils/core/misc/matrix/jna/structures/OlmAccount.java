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

package dev.l3g7.griefer_utils.core.misc.matrix.jna.structures;

import com.sun.jna.Memory;
import com.sun.jna.PointerType;
import dev.l3g7.griefer_utils.core.misc.matrix.MatrixUtil;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.LibOlm;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.Buffer;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.JNAUtil;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.size_t;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Curve25519Keys;
import dev.l3g7.griefer_utils.core.misc.matrix.types.IdentityKeys;

import static dev.l3g7.griefer_utils.core.misc.matrix.jna.LibOlm.LIB_OLM;
import static dev.l3g7.griefer_utils.core.misc.matrix.jna.util.JNAUtil.malloc;
import static java.nio.charset.StandardCharsets.UTF_8;

public class OlmAccount extends PointerType {

	public String encryptionKeyId = null;

	/**
	 * Has no references once set, only used to prevent the Memory object from being garbage
	 * -collected and freeing the underlying memory.
	 */
	@SuppressWarnings("unused")
	private Memory allocatedMemory;

	private static OlmAccount allocateAccount() {
		// Allocate memory
		size_t structureSize = LIB_OLM.olm_account_size();
		Memory accountBuffer = malloc(structureSize);

		// Initialize account
		OlmAccount account = LIB_OLM.olm_account(accountBuffer);
		account.allocatedMemory = accountBuffer;
		return account;
	}

	public static OlmAccount create(String encryptionKeyId) {
		OlmAccount account = allocateAccount();

		// Create random data
		size_t randomLength = LIB_OLM.olm_create_account_random_length(account);
		Buffer randomBuffer = JNAUtil.random(randomLength);

		// Create account
		size_t errorCode = LIB_OLM.olm_create_account(account, randomBuffer, randomLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm account creation", LIB_OLM.olm_account_last_error(account));

		account.encryptionKeyId = encryptionKeyId;
		return account;
	}

	public static OlmAccount deserialize(String encryptionKeyId, String data) {
		OlmAccount account = allocateAccount();
		String key = MatrixUtil.ENCRYPTION_KEYS.get(encryptionKeyId);

		// Allocate memory
		Buffer keyBuffer = malloc(key.getBytes(UTF_8));
		Buffer dataBuffer = malloc(data.getBytes(UTF_8));

		// Deserialize account
		size_t errorCode = LIB_OLM.olm_unpickle_account(account, keyBuffer, keyBuffer.size(), dataBuffer, dataBuffer.size());
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_unpickle_account", LIB_OLM.olm_account_last_error(account));

		account.encryptionKeyId = encryptionKeyId;
		return account;
	}

	public String serialize() {
		String encryptionKey = MatrixUtil.ENCRYPTION_KEYS.get(encryptionKeyId);

		// Allocate memory
		size_t pickledLength = LIB_OLM.olm_pickle_account_length(this);
		Memory pickledBuffer = malloc(pickledLength);
		Buffer keyBuffer = malloc(encryptionKey.getBytes(UTF_8));

		// serialize account
		size_t errorCode = LIB_OLM.olm_pickle_account(this, keyBuffer, keyBuffer.size(), pickledBuffer, pickledLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_pickle_account", LIB_OLM.olm_account_last_error(this));

		return JNAUtil.getString(pickledBuffer);
	}

	public IdentityKeys getIdentityKeys() {
		// Allocate memory
		size_t keyLength = LIB_OLM.olm_account_identity_keys_length(this);
		Memory keyBuffer = malloc(keyLength);

		// Get identity keys
		size_t errorCode = LIB_OLM.olm_account_identity_keys(this, keyBuffer, keyLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_account_identity_keys", LIB_OLM.olm_account_last_error(this));

		// Parse response
		return MatrixUtil.GSON.fromJson(JNAUtil.getString(keyBuffer), IdentityKeys.class);
	}

	public int getMaxOneTimeKeys() {
		return LIB_OLM.olm_account_max_number_of_one_time_keys(this).intValue();
	}

	public void generateOneTimeKeys(int amount) {
		if (amount <= 0)
			return;

		// Allocate memory
		size_t numberOfKeys = new size_t(amount);
		size_t randomLength = LIB_OLM.olm_account_generate_one_time_keys_random_length(this, numberOfKeys);
		Buffer randomBuffer = JNAUtil.random(randomLength);

		// Generate one time keys
		size_t errorCode = LIB_OLM.olm_account_generate_one_time_keys(this, numberOfKeys, randomBuffer, randomLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_account_generate_one_time_keys", LIB_OLM.olm_account_last_error(this));
	}

	public Curve25519Keys getOneTimeKeys() {
		// Allocate memory
		size_t keyLength = LIB_OLM.olm_account_one_time_keys_length(this);
		Memory keyBuffer = malloc(keyLength);

		// Get one time keys
		size_t errorCode = LIB_OLM.olm_account_one_time_keys(this, keyBuffer, keyLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_account_one_time_keys", LIB_OLM.olm_account_last_error(this));

		String jsonBuffer = JNAUtil.getString(keyBuffer);
		System.out.println(jsonBuffer);
		return MatrixUtil.GSON.fromJson(jsonBuffer, Curve25519Keys.class);
	}

	public void generateFallbackKey() {
		// Allocate memory
		size_t randomLength = LIB_OLM.olm_account_generate_fallback_key_random_length(this);
		Buffer randomBuffer = JNAUtil.random(randomLength);

		// Generate fallback key
		size_t errorCode = LIB_OLM.olm_account_generate_fallback_key(this, randomBuffer, randomLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_account_generate_fallback_key", LIB_OLM.olm_account_last_error(this));
	}

	public Curve25519Keys getFallbackKey() {
		// Allocate memory
		size_t keyLength = LIB_OLM.olm_account_unpublished_fallback_key_length(this);
		Memory keyBuffer = malloc(keyLength);

		// Get fallback key
		size_t errorCode = LIB_OLM.olm_account_unpublished_fallback_key(this, keyBuffer, keyLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_account_unpublished_fallback_key", LIB_OLM.olm_account_last_error(this));

		return MatrixUtil.GSON.fromJson(JNAUtil.getString(keyBuffer), Curve25519Keys.class);
	}

	public void markKeysAsPublished() {
		LIB_OLM.olm_account_mark_keys_as_published(this);
	}

	public String sign(byte[] message) {
		// Allocate memory
		size_t signatureLength = LIB_OLM.olm_account_signature_length(this);
		Buffer messageBuffer = malloc(message);
		Memory signatureBuffer = malloc(signatureLength);

		// Sign message
		size_t errorCode = LIB_OLM.olm_account_sign(this, messageBuffer, messageBuffer.size(), signatureBuffer, signatureLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_account_sign", LIB_OLM.olm_account_last_error(this));

		return JNAUtil.getString(signatureBuffer);
	}

	public OlmSession createOutboundSession(String theirIdentityKey, String theirOneTimeKey) {
		return OlmSession.createOutbound(this, theirIdentityKey, theirOneTimeKey);
	}

	@Override
	protected void finalize() throws Throwable {
		LIB_OLM.olm_clear_account(this);
		super.finalize();
	}

}