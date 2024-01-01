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

package dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.token_providers;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.core.Authorization;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.core.XboxProfileResolver;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.tokens.OAuth2Token;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.tokens.XToken;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.util.DateTime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MultiMCTokenProvider implements TokenProvider {

	public boolean loadWithException() throws IOException {
		return load(Paths.get("..", "..", "..", "accounts.json"))
			|| load(Paths.get(System.getenv("AppData"), "PrismLauncher", "accounts.json"));
	}

	private static boolean load(Path path) throws IOException {
		if (!Files.exists(path))
			return false;

		List<Account> accounts = XboxProfileResolver.GSON.fromJson(Files.newBufferedReader(path), Accounts.class).accounts;
		for (Account account : accounts) {

			Authorization.set(new Authorization(
					new OAuth2Token(0, account.msa.token,account.msa.refreshToken, "00000000402b5328", new DateTime(account.msa.exp)),
					new XToken(new DateTime(account.utoken.exp), account.utoken.token, account.utoken.extra.uhs),
					new XToken(new DateTime(account.xrpMain.exp), account.xrpMain.token, account.xrpMain.extra.uhs)
			));

			if (Authorization.get().validate())
				return true;
		}
		return false;
	}

	private static class Accounts {
		List<Account> accounts;
	}

	private static class Account {
		MSAToken msa;
		UToken utoken;
		@SerializedName("xrp-main")
		UToken xrpMain;
	}

	private static class MSAToken {
		long exp;
		@SerializedName("refresh_token")
		String refreshToken;
		String token;
	}

	private static class UToken {
		long exp;
		String token;
		DisplayClaims extra;
	}

	private static class DisplayClaims {
		String uhs;
	}

}