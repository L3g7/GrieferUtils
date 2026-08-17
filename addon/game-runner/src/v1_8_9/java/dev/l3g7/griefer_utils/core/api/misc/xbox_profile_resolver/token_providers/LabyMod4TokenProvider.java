/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.token_providers;

import dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.core.Authorization;
import dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.tokens.OAuth2Token;
import dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.tokens.XToken;
import dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.util.DateTime;
import net.labymod.accountmanager.storage.account.Account;
import net.labymod.accountmanager.storage.loader.external.model.ExternalMicrosoftAccount;
import net.labymod.api.Laby;

public class LabyMod4TokenProvider implements TokenProvider {

	@Override
	public boolean load() {
		for (Account account : Laby.labyAPI().getAccountManager().getAccounts()) {
			if (account instanceof ExternalMicrosoftAccount ms) {
				long expiresAt = ms.getTokens().getMicrosoftToken().getExpiresAt();
				DateTime now = DateTime.now();
				Authorization.set(new Authorization(
					new OAuth2Token(
						expiresAt - now.getUnixTime(),
						ms.getTokens().getMicrosoftToken().getToken(),
						ms.getTokens().getMicrosoftToken().getData("refresh_token"),
						ms.getTokens().getAzure().getClientId(),
						now
					),
					null,
					new XToken(
						new DateTime(ms.getTokens().getXstsToken().getExpiresAt()),
						ms.getTokens().getXstsToken().getToken(),
						ms.getTokens().getXstsToken().getData("user_hash")
					)

				));
				if (Authorization.get().validate())
					return true;
			}
		}

		return false;
	}

}
