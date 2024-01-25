/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.CustomSSLSocketFactoryProvider;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.accountmanager.authentication.microsoft.MicrosoftAuthentication;
import net.labymod.accountmanager.storage.loader.microsoft.model.LauncherAccount;
import net.labymod.accountmanager.utils.RestUtil;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

@Singleton
public class LabyModSwitcherFix extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("LabyMod-Switcher fixen")
		.description("Behebt, dass LabyMod Account-Sitzungen als gültig anzeigt, das Betreten eines Servers mit diesem Account jedoch aufgrund einer ungültigen Sitzung fehltschlägt, und dass das Hinzufügen von Accounts aufgrund nicht anerkannter Zertifikate fehlschlägt.")
		.icon("labymod:labymod_logo");

	@Mixin(LauncherAccount.class)
	private static class MixinLauncherAccount {

		@Inject(method = "getAccessToken", at = @At("RETURN"), cancellable = true, remap = false)
		public void injectGetAccessToken(CallbackInfoReturnable<String> cir) {
			String accessToken = cir.getReturnValue();
			if (accessToken == null || accessToken.startsWith("ey") || !FileProvider.getSingleton(LabyModSwitcherFix.class).isEnabled())
				return;

			if (accessToken.startsWith("8E184B2C-7E2D-4517-A905-623B1BE84B5700000001ffffffffffffffffey")) {
				cir.setReturnValue(accessToken.substring(60));
				return;
			}

			cir.setReturnValue(accessToken.split("\\.")[0]);
		}

	}

	@Mixin(value = MicrosoftAuthentication.class, remap = false)
	private static class MixinRestUtil {

	    @Redirect(method = "getXBoxProfile", at = @At(value = "INVOKE", target = "Lnet/labymod/accountmanager/utils/RestUtil;performGetContract(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"))
	    private <T> T injectGetXBoxProfile(String url, String hash, String token, Class<T> response) throws Exception {
			if (!FileProvider.getSingleton(LabyModSwitcherFix.class).isEnabled())
				return RestUtil.performGetContract(url, hash, token, response);

		    HttpsURLConnection connection = (HttpsURLConnection)(new URL(url)).openConnection();
		    connection.setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

		    connection.addRequestProperty("Authorization", "XBL3.0 x=" + hash + ";" + token);
		    connection.addRequestProperty("x-xbl-contract-version", "2");
		    connection.addRequestProperty("Accept", "application/json");

		    InputStream inputStream = connection.getInputStream();
		    String json = IOUtils.toString(new InputStreamReader(inputStream));
		    return IOUtil.gson.fromJson(json, response);
	    }

	}

}
