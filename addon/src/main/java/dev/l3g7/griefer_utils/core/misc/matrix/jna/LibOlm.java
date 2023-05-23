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

package dev.l3g7.griefer_utils.core.misc.matrix.jna;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.structures.OlmAccount;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.structures.OlmOutboundGroupSession;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.structures.OlmSession;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.LibOlmLoader;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.size_t;

public interface LibOlm extends Library { // TODO add refs to include files | Docs? Using Pointer for in, Memory for out | Sort

	LibOlm LIB_OLM = (LibOlm) Native.loadLibrary(LibOlmLoader.getPath(), LibOlm.class);

	/**
	 * @return The value that olm will return from a function if there was an error.
	 */
	size_t olm_error();

	/* * * * * * * * * *
	 * Account methods *
	 * * * * * * * * * */

	/**
	 * @return A null terminated string describing the most recent error to happen to an account.
	 */
	Pointer olm_account_last_error(OlmAccount account);

	// Account creation

	/**
	 * Initialises an account object using the supplied memory.
	 *
	 * @param memory must be at least {@link LibOlm#olm_account_size} bytes.
	 */
	OlmAccount olm_account(Memory memory);
	size_t olm_account_size();

	/**
	 * Creates a new account.
	 *
	 * @return {@link LibOlm#olm_error} on failure. If there weren't enough random bytes then
	 * {@link LibOlm#olm_account_last_error} will be {@code "NOT_ENOUGH_RANDOM"}.
	 */
	size_t olm_create_account(OlmAccount account, Pointer random, size_t randomLength);
	size_t olm_create_account_random_length(OlmAccount account);

	// Account identity keys

	/**
	 * Writes the public parts of the identity keys for the account into the {@code identityKeys} output buffer.
	 *
	 * @return {@link LibOlm#olm_error} on failure. If the identity_keys buffer was too small then
	 * {@link LibOlm#olm_account_last_error} will be {@code "OUTPUT_BUFFER_TOO_SMALL"}.
	 */
	size_t olm_account_identity_keys(OlmAccount account, Memory identityKeys, size_t identityKeyLength);
	size_t olm_account_identity_keys_length(OlmAccount account);

	// Account signing

	/**
	 * Signs a message with the ed25519 key for this account.
	 *
	 * @return {@link LibOlm#olm_error} on failure. If the signature buffer was too small then
	 * {@link LibOlm#olm_account_last_error} will be {@code "OUTPUT_BUFFER_TOO_SMALL"}.
	 */
	size_t olm_account_sign(OlmAccount account, Pointer message, size_t messageLength, Memory signature, size_t signatureLength);
	size_t olm_account_signature_length(OlmAccount account);

	// Account one-time keys

	/**
	 * @return The largest number of one time keys this account can store.
	 */
	size_t olm_account_max_number_of_one_time_keys(OlmAccount account);

	/**
	 * Writes the public parts of the unpublished one time keys for the account into the {@code oneTimeKeys} output buffer.
	 * <p>
	 * The returned data is a JSON-formatted object with the single property {@code curve25519}, which is itself
	 * an object mapping key id to base64-encoded Curve25519 key. For example:
	 * <pre>
	 * {
	 *     curve25519: {
	 *         "AAAAAA": "wo76WcYtb0Vk/pBOdmduiGJ0wIEjW4IBMbbQn7aSnTo",
	 *         "AAAAAB": "LRvjo46L1X2vx69sS9QNFD29HWulxrmW11Up5AfAjgU"
	 *     }
	 * }
	 * </pre>
	 *
	 * @return {@link LibOlm#olm_error} on failure.
	 * <p>
	 * If the one_time_keys buffer was too small then {@link LibOlm#olm_account_last_error}
	 * will be {@code "OUTPUT_BUFFER_TOO_SMALL"}.
	 */
	size_t olm_account_one_time_keys(OlmAccount account, Memory oneTimeKeys, size_t oneTimeKeysLength);
	size_t olm_account_one_time_keys_length(OlmAccount account);

	/**
	 * Generates a number of new one time keys. If the total number of keys stored by this account
	 * exceeds {@link LibOlm#olm_account_max_number_of_one_time_keys} then the old keys are discarded.
	 *
	 * @return {@link LibOlm#olm_error} on error. If the number of random bytes is too small then
	 * {@link LibOlm#olm_account_last_error} will be {@code "NOT_ENOUGH_RANDOM"}.
	 */
	size_t olm_account_generate_one_time_keys(OlmAccount account, size_t numberOfKeys, Pointer random, size_t randomLength);
	size_t olm_account_generate_one_time_keys_random_length(OlmAccount account, size_t numberOfKeys);

	// Account fallback key

	/**
	 * Returns the fallback key (if present, and if unpublished) into the {@code fallbackKey} buffer.
	 */
	size_t olm_account_unpublished_fallback_key(OlmAccount account, Memory fallbackKey, size_t fallbackKeyLength);
	size_t olm_account_unpublished_fallback_key_length(OlmAccount account);

	/**
	 * Generates a new fallback key. Only one previous fallback key is stored.
	 *
	 * @return {@link LibOlm#olm_error} on error. If the number of random bytes is too small then
	 * {@link LibOlm#olm_account_last_error} will be {@code "NOT_ENOUGH_RANDOM"}.
	 */
	size_t olm_account_generate_fallback_key(OlmAccount account, Pointer random, size_t randomLength);
	size_t olm_account_generate_fallback_key_random_length(OlmAccount account);

	/**
	 * Marks the current set of one time keys and fallback key as being published. Once marked as
	 * published, the one time keys will no longer be returned by {@link LibOlm#olm_account_one_time_keys}, and the
	 * fallback key will no longer be returned by {@link LibOlm#olm_account_unpublished_fallback_key}.
	 */
	void olm_account_mark_keys_as_published(OlmAccount account);

	/* * * * * * * * * * * * * * * * * * *
	 * Outbound Session (Olm v1) methods *
	 * * * * * * * * * * * * * * * * * * */

	/**
	 * @return A null terminated string describing the most recent error to happen to a session.
	 */
	Pointer olm_session_last_error(OlmSession session);

	// Session creation

	/**
	 * Initialises a session object using the supplied memory.
	 *
	 * @param memory must be at least {@link LibOlm#olm_session_size} bytes.
	 */
	OlmSession olm_session(Memory memory);
	size_t olm_session_size();

	/**
	 * Creates a new out-bound session for sending messages to a given identity key and one time key.
	 *
	 * @return {@link LibOlm#olm_error} on failure. If the keys couldn't be decoded as base64 then
	 * {@link LibOlm#olm_session_last_error} will be {@code "INVALID_BASE64"}. If there weren't enough random
	 * bytes then {@link LibOlm#olm_session_last_error} will be {@code "NOT_ENOUGH_RANDOM"}.
	 */
	size_t olm_create_outbound_session(OlmSession session, OlmAccount account, Pointer theirIdentityKey, size_t theirIdentityKeyLength, Pointer theirOneTimeKey, size_t theirOneTimeKeyLength, Pointer random, size_t randomLength);
	size_t olm_create_outbound_session_random_length(OlmSession session);

	// Message encryption

	/**
	 * The type of the next message that {@link LibOlm#olm_encrypt} will return.
	 *
	 * @return {@code OLM_MESSAGE_TYPE_PRE_KEY} if the message will be a {@code PRE_KEY} message. Returns
	 * {@code OLM_MESSAGE_TYPE_MESSAGE} if the message will be a normal message. Returns {@link LibOlm#olm_error}
	 * on failure.
	 */
	size_t olm_encrypt_message_type(OlmSession session);

	/**
	 * Encrypts a message using the session.
	 *
	 * @return the length of the message in bytes on success. Writes the message as base64 into the
	 * message buffer. Returns {@link LibOlm#olm_error} on failure. If the message buffer is too small then
	 * {@link LibOlm#olm_session_last_error} will be {@code "OUTPUT_BUFFER_TOO_SMALL"}. If there weren't enough
	 * random bytes then {@link LibOlm#olm_session_last_error} will be {@code "NOT_ENOUGH_RANDOM"}.
	 */
	size_t olm_encrypt(OlmSession session, Pointer plaintext, size_t plaintextLength, Pointer random, size_t randomLength, Memory message, size_t messageLength);
	size_t olm_encrypt_random_length(OlmSession session);
	size_t olm_encrypt_message_length(OlmSession session, size_t plaintextLength);

	/* * * * * * * * * * * * * * * * * * * * * *
	 * Outbound Group Session (Megolm) methods *
	 * * * * * * * * * * * * * * * * * * * * * */

	/**
	 * @return A null terminated string describing the most recent error to happen to a group session.
	 */
	Pointer olm_outbound_group_session_last_error(OlmOutboundGroupSession session);

	// Session creation

	/**
	 * Initialises an outbound group session object using the supplied memory.
	 *
	 * @param memory should be at least {@link LibOlm#olm_outbound_group_session_size} bytes.
	 */
	OlmOutboundGroupSession olm_outbound_group_session(Memory memory);
	size_t olm_outbound_group_session_size();

	/**
	 * Start a new outbound group session.
	 *
	 * @return {@link LibOlm#olm_error} on failure. On failure {@code last_error} will be set with an error code. The
	 * {@code last_error} will be {@code "NOT_ENOUGH_RANDOM"} if the number of random bytes was too small.
	 */
	size_t olm_init_outbound_group_session(OlmOutboundGroupSession session, Pointer random, size_t randomLength);
	size_t olm_init_outbound_group_session_random_length(OlmOutboundGroupSession session);

	// Session id

	/**
	 * Get a base64-encoded identifier for this session.
	 *
	 * @return The length of the session id on success or {@link LibOlm#olm_error} on failure. On failure {@code last_error}
	 * will be set with an error code. The {@code last_error} will be {@code "OUTPUT_BUFFER_TOO_SMALL"} if the
	 * id buffer was too small.
	 */
	size_t olm_outbound_group_session_id(OlmOutboundGroupSession session, Memory id, size_t idLength);
	size_t olm_outbound_group_session_id_length(OlmOutboundGroupSession session);

	// Session key

	/**
	 * Get the base64-encoded current ratchet key for this session.
	 * <p>
	 * Each message is sent with a different ratchet key. This function returns the ratchet key that will
	 * be used for the next message.
	 *
	 * @return The length of the ratchet key on success or {@link LibOlm#olm_error} on failure. On failure
	 * {@code last_error} will be set with an error code. The {@code last_error} will be
	 * {@code "OUTPUT_BUFFER_TOO_SMALL"} if the buffer was too small.
	 */
	size_t olm_outbound_group_session_key(OlmOutboundGroupSession session, Memory key, size_t keyLength);
	size_t olm_outbound_group_session_key_length(OlmOutboundGroupSession session);

	// Message encryption

	/**
	 * Encrypt some plain-text.
	 *
	 * @return The length of the encrypted message or {@link LibOlm#olm_error} on failure. On failure {@code last_error} will
	 * be set with an error code. The {@code last_error} will be {@code OUTPUT_BUFFER_TOO_SMALL} if the
	 * output buffer is too small.
	 */
	size_t olm_group_encrypt(OlmOutboundGroupSession session, Pointer plaintext, size_t plaintextLength, Memory message, size_t messageLength);
	size_t olm_group_encrypt_message_length(OlmOutboundGroupSession session, size_t plaintextLength);

	class OlmInvokationException extends IllegalArgumentException {

		public OlmInvokationException(String method, String error) {
			super("Error while invoking " + method + ": " + error);
		}

		public OlmInvokationException(String method, Pointer error) {
			this(method, error.getString(0));
		}

	}
}