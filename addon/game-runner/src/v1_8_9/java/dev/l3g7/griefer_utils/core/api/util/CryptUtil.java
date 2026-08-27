/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class CryptUtil {

	/**
	 * @param targetHash base64-encoded SHA256
	 */
	public static boolean checkHashFail(Path libPath, String targetHash) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] fileHash = md.digest(Files.readAllBytes(libPath));
			return !Arrays.equals(fileHash, Base64.getDecoder().decode(targetHash));
		} catch (NoSuchAlgorithmException e) {
			throw Util.elevate(e);
		}
	}

}