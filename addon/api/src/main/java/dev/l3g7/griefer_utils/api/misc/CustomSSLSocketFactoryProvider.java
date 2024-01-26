/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.api.misc;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.util.UUID;

/**
 * Provides a SSLSocketFactory which checks using certificates in resources/security/certificates/ in addition to the default ones provided by java.
 */
public class CustomSSLSocketFactoryProvider {

	private static final SSLSocketFactory customFactory;

	public static SSLSocketFactory getCustomFactory() {
		return customFactory;
	}

	static {
		try {
			KeyStore keyStore = getKeyStore();
			SSLContext sslContext = SSLContext.getInstance("SSL");
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keyStore);
			sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
			customFactory =  sslContext.getSocketFactory();
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static KeyStore getKeyStore() throws GeneralSecurityException, IOException {
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null);
		loadDefaultKeyStore(keyStore);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		for (String file : FileProvider.getFiles(f -> f.endsWith(".der") && f.startsWith("assets/minecraft/griefer_utils/security/certificates/")))
			addCertificate(keyStore, cf.generateCertificate(FileProvider.getData(file)));

		return keyStore;
	}

	private static void loadDefaultKeyStore(KeyStore keyStore) throws GeneralSecurityException, IOException {
		String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
		KeyStore defaultKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		defaultKeyStore.load(Files.newInputStream(Paths.get(filename)), "changeit".toCharArray());

		for (TrustAnchor ta : new PKIXParameters(defaultKeyStore).getTrustAnchors())
			addCertificate(keyStore, ta.getTrustedCert());
	}

	private static void addCertificate(KeyStore keyStore, Certificate certificate) throws KeyStoreException {
		keyStore.setCertificateEntry(UUID.randomUUID().toString(), certificate);
	}

}
