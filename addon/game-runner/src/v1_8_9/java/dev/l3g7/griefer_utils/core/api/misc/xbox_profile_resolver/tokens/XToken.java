/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.tokens;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.util.DateTime;

public class XToken {

	@SerializedName("NotAfter")
	public DateTime expireDate;
	@SerializedName("Token")
	public String token;
	@SerializedName("DisplayClaims")
	public DisplayClaims displayClaims;

	public XToken(DateTime expireDate, String token, String uhs) {
		this.expireDate = expireDate;
		this.token = token;
		this.displayClaims = new DisplayClaims();
		displayClaims.xui[0].uhs = uhs;
	}

	public boolean isValid() {
		return expireDate.after(DateTime.now());
	}

	public String getUHS() {
		return displayClaims.xui[0].uhs;
	}

	private static class DisplayClaims {
		XUI[] xui = new XUI[]{new XUI()};

		private static class XUI {
			String uhs;
		}
	}
}