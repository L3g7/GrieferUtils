/*
 * A slim version of https://github.com/Litarvan/OpenAuth.
 *
 * Copyright 2015-2021 Adrien 'Litarvan' Navratil
 */
package dev.l3g7.griefer_utils.misc.mysterymod_connection.util;

import com.google.gson.Gson;
import net.minecraft.util.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MicrosoftAuth {

	private static final Gson gson = new Gson();

	public static Session loginWithCredentials(String email, String password) throws IllegalStateException {
		CookieHandler currentHandler = CookieHandler.getDefault();
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

		HttpURLConnection result;

		try {
			String authRes = readResponse(createConnection("https://login.live.com/oauth20_authorize.srf" + '?' + buildParams("client_id", "000000004C12AE6F", "redirect_uri", "https://login.live.com/oauth20_desktop.srf", "scope", "service::user.auth.xboxlive.com::MBI_SSL", "response_type", "token", "display", "touch", "locale", "en")));
			result = followRedirects(post(match("urlPost: ?'(.+?(?='))", authRes), "application/x-www-form-urlencoded", "*/*", buildParams("login", email, "loginfmt", email, "passwd", password, "PPFT", match("sFTTag:'.*value=\"([^\"]*)\"", authRes))));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			CookieHandler.setDefault(currentHandler);
		}

		try {
			String matched = match("access_token=([^&]*)", result.getURL().toString());
			if (matched == null)
				throw new IllegalStateException("Invalid credentials or tokens");

			XboxLoginResponse xboxResponse = gson.fromJson(readResponse(post("https://xsts.auth.xboxlive.com/xsts/authorize", "application/json", "application/json", gson.toJson(new XboxLoginRequest(new XboxLoginRequest.XSTSAuthorizationProperties("RETAIL", new String[]{gson.fromJson(readResponse(post("https://user.auth.xboxlive.com/user/authenticate", "application/json", "application/json", gson.toJson(new XboxLoginRequest(new XboxLoginRequest.XboxLiveLoginProperties("RPS", "user.auth.xboxlive.com", URLDecoder.decode(matched, "UTF-8")), "http://auth.xboxlive.com", "JWT")))), XboxLoginResponse.class).Token}), "rp://api.minecraftservices.com/", "JWT")))), XboxLoginResponse.class);
			MinecraftLoginResponse minecraftResponse = gson.fromJson(readResponse(post("https://api.minecraftservices.com/authentication/login_with_xbox", "application/json", "application/json", gson.toJson(new MinecraftLoginRequest(String.format("XBL3.0 x=%s;%s", xboxResponse.DisplayClaims.xui[0].uhs, xboxResponse.Token))))), MinecraftLoginResponse.class);
			HttpURLConnection connection = createConnection("https://api.minecraftservices.com/minecraft/profile");
			connection.addRequestProperty("Authorization", "Bearer " + minecraftResponse.access_token);
			connection.addRequestProperty("Accept", "application/json");
			MinecraftProfileResponse profile =  gson.fromJson(readResponse(connection), MinecraftProfileResponse.class);
			return new Session(profile.name, profile.id, minecraftResponse.access_token, "mojang");
		} catch (IOException e) {
			try {
				if (match("identity/confirm", readResponse(result)) != null)
					throw new IllegalStateException("User has enabled double-authentication or must allow sign-in on https://account.live.com/activity");
			} catch (IOException ex) {
				throw new IllegalStateException(e);
			}
			throw new IllegalStateException(e);
		}
	}

	private static String match(String regex, String content) {
		Matcher matcher = Pattern.compile(regex).matcher(content);
		if (!matcher.find())
			return null;

		return matcher.group(1);
	}

	private static HttpURLConnection post(String url, String contentType, String accept, String data) throws IOException {
		HttpURLConnection connection = createConnection(url);
		connection.setDoOutput(true);
		connection.addRequestProperty("Content-Type", contentType);
		connection.addRequestProperty("Accept", accept);
		connection.setRequestMethod("POST");
		connection.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
		return connection;
	}

	private static String readResponse(HttpURLConnection connection) throws IOException {
		String redirection = connection.getHeaderField("Location");
		if (redirection != null)
			return readResponse(createConnection(redirection));

		StringBuilder response = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			String line;
			while ((line = br.readLine()) != null)
				response.append(line).append('\n');
		}

		return response.toString();
	}

	private static HttpURLConnection followRedirects(HttpURLConnection connection) throws IOException {
		String redirection = connection.getHeaderField("Location");
		if (redirection != null)
			connection = followRedirects(createConnection(redirection));

		return connection;
	}

	private static String buildParams(String... params) {
		StringBuilder query = new StringBuilder();
		for (int i = 0; i < params.length; i += 2) {
			if (query.length() > 0)
				query.append('&');

			try {
				query.append(params[i]).append('=').append(URLEncoder.encode(params[i + 1], "UTF-8"));
			} catch (UnsupportedEncodingException ignored) {}
		}

		return query.toString();
	}

	private static HttpURLConnection createConnection(String url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("Accept-Language", "en-US");
		connection.setRequestProperty("Accept-Charset", "UTF-8");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (XboxReplay; XboxLiveAuth/3.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
		return connection;
	}

	private static final class MinecraftLoginRequest {
		public final String identityToken;

		private MinecraftLoginRequest(String identityToken) {
			this.identityToken = identityToken;
		}

	}

	private static class XboxLoginRequest {

		public final Object Properties;
		public final String RelyingParty;
		public final String TokenType;

		public XboxLoginRequest(Object properties, String relyingParty, String tokenType) {
			this.Properties = properties;
			this.RelyingParty = relyingParty;
			this.TokenType = tokenType;
		}

		public static class XboxLiveLoginProperties {

			public final String AuthMethod;
			public final String SiteName;
			public final String RpsTicket;

			public XboxLiveLoginProperties(String authMethod, String siteName, String rpsTicket) {
				this.AuthMethod = authMethod;
				this.SiteName = siteName;
				this.RpsTicket = rpsTicket;
			}

		}

		public static class XSTSAuthorizationProperties {

			public final String SandboxId;
			public final String[] UserTokens;

			public XSTSAuthorizationProperties(String sandboxId, String[] userTokens) {
				this.SandboxId = sandboxId;
				this.UserTokens = userTokens;
			}

		}
	}

	private static class MinecraftLoginResponse {
		public final String access_token;

		public MinecraftLoginResponse(String access_token) {
			this.access_token = access_token;
		}

	}

	private static final class MinecraftProfileResponse {
		public final String id;
		public final String name;

		public MinecraftProfileResponse(String id, String name) {
			this.id = id;
			this.name = name;
		}

	}

	private static class XboxLoginResponse {
		public final String Token;
		public final XboxLiveLoginResponseClaims DisplayClaims;

		public XboxLoginResponse(String Token, XboxLiveLoginResponseClaims DisplayClaims) {
			this.Token = Token;
			this.DisplayClaims = DisplayClaims;
		}

		public static class XboxLiveLoginResponseClaims {
			public final XboxLiveUserInfo[] xui;

			public XboxLiveLoginResponseClaims(XboxLiveUserInfo[] xui) {
				this.xui = xui;
			}
		}

		public static class XboxLiveUserInfo {
			public final String uhs;

			public XboxLiveUserInfo(String uhs) {
				this.uhs = uhs;
			}
		}
	}

}
