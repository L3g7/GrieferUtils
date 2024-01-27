package dev.l3g7.griefer_utils.features.uncategorized.byteandbit.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.features.uncategorized.byteandbit.ByteAndBit;
import dev.l3g7.griefer_utils.misc.NameCache;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * description missing.
 */
public class BABBot {
	private List<BABItem> items;
	private AxisAlignedBB botzone;
	final UUID uuid;

	public BABBot(UUID name) {
		this.uuid = name;
	}

	public List<BABItem> getItems() {
		return items;
	}

	public AxisAlignedBB getBotzone() {
		return botzone;
	}

	public UUID getUuid() {
		return this.uuid;
	}

	public String getName() {
		return NameCache.getName(this.uuid);
	}

	public void invalidateCache() {
		this.botzone = null;
	}

	public CompletableFuture<Boolean> requestIfNotCached() {
		if (botzone != null) return CompletableFuture.supplyAsync(() -> true);
		return requestData();
	}

	public static void initBots(Map<UUID, BABBot> fillList) {
		IOUtil.read(FileProvider.getSingleton(ByteAndBit.class).BAB_URL + "scope/getBots").asJsonObject((res) -> {
			if (!res.get("success").getAsBoolean()) return;
			JsonArray bots = res.get("bots").getAsJsonArray();
			for (JsonElement e : bots) {
				UUID botUUID = UUID.fromString(e.getAsString().replaceAll("-", "").replaceAll("^(.{8})(.{4})(.{4})(.{4})(.{12})$", "$1-$2-$3-$4-$5"));
				fillList.put(botUUID, new BABBot(botUUID));
			}
		});
	}

	private CompletableFuture<Boolean> requestData() {
		return CompletableFuture.supplyAsync(() -> {
			Optional<JsonObject> res = IOUtil.read(FileProvider.getSingleton(ByteAndBit.class).BAB_URL + "item/getItems/" + this.uuid.toString().replaceAll("-", "")).asJsonObject();
			if (!res.isPresent()) return false;
			if (!res.get().get("success").getAsBoolean()) return false;
			JsonArray array = res.get().get("items").getAsJsonArray();
			JsonObject aabb = res.get().get("zone").getAsJsonObject();
			this.botzone = AxisAlignedBB.fromBounds(aabb.get("x1").getAsInt(), aabb.get("y1").getAsInt(), aabb.get("z1").getAsInt(), aabb.get("x2").getAsInt(), aabb.get("y2").getAsInt(), aabb.get("z2").getAsInt());
			this.items = BABItem.parse(array);
			System.out.println("Finished fetching items for " + this.uuid + "!");
			return true;
		});
	}
}
