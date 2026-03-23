package dev.l3g7.griefer_utils.features.gui.integrations.byte_and_bit.data;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.l3g7.griefer_utils.features.gui.integrations.byte_and_bit.data.BotSource.TrustLevel.LOW;

public class BotSource {

	private final String url;
	private final TrustLevel trustLevel;
	private final List<DataMember> members;

	public BotSource(String url, TrustLevel trustLevel, List<DataMember> members) {
		this.url = url;
		this.trustLevel = trustLevel;
		this.members = members;
	}

	public String getUrl() {
		return url;
	}

	public CompletableFuture<List<String>> get() {
		CompletableFuture<List<String>> result = new CompletableFuture<>();

		IOUtil.read(url + "scope/getBots").asJsonObject((res) -> {
			if (!res.get("success").getAsBoolean()) {
				result.complete(new ArrayList<>());
				return;
			}

			List<String> bots = new ArrayList<>();
			for (JsonElement entry : res.get("bots").getAsJsonArray()) {
				String uuid = entry.getAsString().replaceAll("-", "");
				if (isTrusted(uuid))
					bots.add(uuid);
			}

			result.complete(bots);
		});

		return result;
	}

	private boolean isTrusted(String member) {
		if (trustLevel != LOW)
			return true;

		var idx = members.indexOf(new DataMember(member, LOW));
		if (idx == -1)
			return false;

		return members.get(idx).trustLevel != LOW;
	}

	public static class DataMember {
		private final String uuid;
		private final TrustLevel trustLevel;

		public DataMember(String uuid, TrustLevel trustLevel) {
			this.uuid = uuid;
			this.trustLevel = trustLevel;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof DataMember member))
				return false;

			return uuid.equals(member.uuid);
		}
	}

	public enum TrustLevel {
		HIGH,
		MEDIUM,
		LOW
	}

}
