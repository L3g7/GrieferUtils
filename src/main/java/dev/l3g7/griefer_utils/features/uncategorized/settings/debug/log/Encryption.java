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

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static dev.l3g7.griefer_utils.util.Util.elevate;

public class Encryption {

	public static final Encryption INSTANCE = new Encryption();

	private static final String RSA_PUBLIC_KEY = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAqy5nH6VUCv6aUr+VbtpB0Y/Fui6tV4em+OWA3TzOCnSpR++zVjvU+cSLgWaSXhiwJBDXXWjANonv68mU3eUEaoPIQFIzNxolGjveJGl7QyyCJuEZYQH22zbsT1HPZiCO8PqdxubDBRyRjRgGua8rn37a1T8GUdw9aG1I+T2KFloDMWy2zVaAoRoankSHhsn79tJ97VDQ3BJhuXbdDzwisz/0hhnpzPAvcvzvc+z907PfOSBbqe+gRAF/yLV1d/ypqtWEFII7zdYyB2F52+2ZmQYBGlZpK0fqoawWqZC6qMX2exKFyduhNmE0xBnifBDStcnHAv4TveOEwQ7OyM49Kumy0QCQMkzkLhJE7CNlTCxvtd+/ssxT4ZWejE/TDw40g2AcBT/WRaOnCF9nvLXivZVemMrfs5PQVg33lIH5ntp5dS/V8wLVdbtTS8FPKzCqEo18XpUqnSwIbIZZDhw819peGXaNrkOmR30Ai+v2ZOQxO2p/hrpp+XJI3/Q8Mz66K0cYqC7L4uEE29NRz4uUrB34xW8rYiCxxV6wMXirIrMaZkuzxScBbzJbJfec/m/bf+nrbqCKt71DC/lONiHRN3c2iKhF6r2EYARHUj3wtewwILxzQLqvPY73sxUB6yocdU0Fu7v9ZwBi2QaHFFIHW7hyL0KjB/bRJqZiyoVRYbcCAwEAAQ==";
	private final Cipher rsaCipher;

	private Encryption() {
		try {
			rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			rsaCipher.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(RSA_PUBLIC_KEY))));
		} catch (GeneralSecurityException e) {
			throw elevate(e);
		}
	}

	public void encrypt(byte[] in, OutputStream out) throws GeneralSecurityException, IOException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(256);
		SecretKey aesKey = keyGenerator.generateKey();
		Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);

		out.write(rsaCipher.doFinal(aesKey.getEncoded()));
		out.write(aesCipher.doFinal(in));
	}

}