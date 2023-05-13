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

package dev.l3g7.griefer_utils.core.misc.matrix.modules.uiaa;

import java.security.PrivateKey;

public class PlayerKeyPair {

	public final PrivateKey privateKey;
	public final String publicKey;
	public final String keySignature;
	public final long expirationTime;

	public PlayerKeyPair(PrivateKey privateKey, String publicKey, String keySignature, long expirationTime) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.keySignature = keySignature;
		this.expirationTime = expirationTime;
	}

}