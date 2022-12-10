package dev.l3g7.griefer_utils.misc;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.event_bus.EventPriority;
import dev.l3g7.griefer_utils.event.events.network.PacketReceiveEvent;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NameCache {

	private static final Map<UUID, String> uuidToUser = new HashMap<>();
	private static final Map<String, UUID> nickToUuidCache = new HashMap<>();

	public static String getName(S38PacketPlayerListItem.AddPlayerData data) {
		return getName(data.getProfile().getId());
	}

	public static String getName(UUID uuid) {
		return uuidToUser.get(uuid);
	}

	public static String getName(String nick) {
		return getName(getUUID(nick));
	}

	public static String ensureRealName(String nick) {
		return nick.contains("~") ? getName(nick) : nick;
	}

	public static UUID getUUID(String nick) {
		return nickToUuidCache.get(nick);
	}

	public static boolean hasUUID(UUID uuid) {
		return uuidToUser.containsKey(uuid);
	}

	@EventListener(priority = EventPriority.HIGH)
	public static void onPacket(PacketReceiveEvent event) {
		if (!(event.getPacket() instanceof S38PacketPlayerListItem))
			return;

		S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.getPacket();

		switch (packet.func_179768_b()) {
			case ADD_PLAYER:
				processAddPacket(packet);
				break;
			case UPDATE_DISPLAY_NAME:
				processUpdatePacket(packet);
				break;
			case REMOVE_PLAYER:
				processRemovePacket(packet);
				break;
		}
	}

	private static void processAddPacket(S38PacketPlayerListItem packet) {
		for (S38PacketPlayerListItem.AddPlayerData data : packet.func_179767_a()) {
			if (data.getProfile().getName() != null)
				uuidToUser.put(data.getProfile().getId(), data.getProfile().getName());

			checkForNick(data);
		}
	}

	private static void processUpdatePacket(S38PacketPlayerListItem packet) {
		packet.func_179767_a().forEach(NameCache::checkForNick);
	}

	private static void processRemovePacket(S38PacketPlayerListItem packet) {
		for (S38PacketPlayerListItem.AddPlayerData data : packet.func_179767_a()) {

			UUID uuid = data.getProfile().getId();

			if (nickToUuidCache.containsValue(data.getProfile().getId()))
				nickToUuidCache.values().removeIf(uuid::equals);

			uuidToUser.remove(uuid);
		}
	}

	private static void checkForNick(S38PacketPlayerListItem.AddPlayerData data) {
		if (data.getDisplayName() == null)
			return;

		String name = data.getDisplayName().getUnformattedText();
		if (name.contains("~"))
			nickToUuidCache.put(name.substring(name.indexOf('~')), data.getProfile().getId());
	}
}
