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

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.WindowClickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.misc.TickScheduler;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldSettings;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Mouse;

import java.util.*;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;
import static net.minecraft.network.play.server.S38PacketPlayerListItem.Action.ADD_PLAYER;

@Singleton
public class OrbSellerFix extends Feature {

	private final HashMap<String, Integer> cbToId = new HashMap<>();
	private Pair<Integer, Integer> mousePos = null;
	private UUID orbSellerUUID = null;
	private boolean adding = false;

	private final SwitchSetting restoreMousePos = SwitchSetting.create()
		.name("Maus-Position wiederherstellen")
		.description("Behebt, dass die Maus zur Mitte des Fensters bewegt wird, wenn etwas abgegeben wurde.")
		.icon("mouse");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Orb-Händler fixen")
		.description("Behebt, dass der Orb-Händler nicht sichtbar ist, wenn man sich mit einem Home zu ihm teleportiert."
			 + "\n§8(Der Orbhändler auf dem Citybuild muss dafür seit dem letzten Server-Neustart gesehen worden sein.)")
		.icon("orbseller")
		.subSettings(restoreMousePos);

	@Override
	public void init() {
		super.init();
		String key = getCategory().getConfigKey() + ".orb_seller_fix.";
		if (!Config.has(key + "ids"))
			return;

		JsonObject ids = Config.get(key + "ids").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : ids.entrySet())
			cbToId.put(entry.getKey(), entry.getValue().getAsInt());

		new Timer().schedule(new TimerTask() {
			public void run() {
				TickScheduler.runAfterRenderTicks(() -> {
					cbToId.clear();
					saveIds();
				}, 1);
			}
		}, new Date(Config.get(key + "reset").getAsLong()), 24 * 3600 * 1000);
	}

	@EventListener
	private void onGuiOpen(GuiOpenEvent<GuiChest> event) {
		if (mousePos == null)
			return;

		TickScheduler.runAfterRenderTicks(() -> {
			Mouse.setCursorPosition(mousePos.getLeft(), mousePos.getRight());
			mousePos = null;
		}, 1);
	}

	@EventListener
	private void onWindowClick(WindowClickEvent event) {
		if (!restoreMousePos.get() || !getGuiChestTitle().startsWith("§6Orbs - Verkauf "))
			return;

		if (event.mode > 4)
			return;

		if (!ItemUtil.getLastLore(event.itemStack).endsWith("Orbs zu verkaufen."))
			return;

		mousePos = Pair.of(Mouse.getX(), Mouse.getY());
	}

	@EventListener
	private void onPacket(PacketReceiveEvent<Packet<?>> event) {
		if (adding)
			return;

		// Detect going to the orb seller
		if (event.packet instanceof S0EPacketSpawnObject) {
			S0EPacketSpawnObject p = (S0EPacketSpawnObject) event.packet;
			// Check if it's a ArmorStand
			if (p.getType() != 78)
				return;

			// Check position
			if (p.getX() != 5443 || p.getY() != 787 || p.getZ() != -1194)
				return;

			// Check position
			if (p.getYaw() != 88 || p.getPitch() != 24)
				return;

			Integer orbSellerId = cbToId.get(getServerFromScoreboard());
			if (orbSellerId != null && world().getEntityByID(orbSellerId) == null)
				mc().addScheduledTask(() -> spawnOrbSeller(orbSellerId));

		}

		// Get the uuid of the original orb seller
		if (event.packet instanceof S38PacketPlayerListItem) {
			S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.packet;
			if (packet.getAction() != ADD_PLAYER)
				return;

			for (S38PacketPlayerListItem.AddPlayerData entry : packet.getEntries()) {
				if ("§6Händler".equals(entry.getProfile().getName())) {
					orbSellerUUID = entry.getProfile().getId();
					if (cbToId.containsKey(getServerFromScoreboard()))
						world().removeEntityFromWorld(cbToId.get(getServerFromScoreboard()));
					return;
				}
			}
		}

		// Get the entity id of the original orb seller
		if (event.packet instanceof S0CPacketSpawnPlayer) {
			S0CPacketSpawnPlayer packet = (S0CPacketSpawnPlayer) event.packet;
			if (packet.getPlayer().equals(orbSellerUUID)) {
				orbSellerUUID = null;
				cbToId.put(getServerFromScoreboard(), packet.getEntityID());
				saveIds();
			}
		}
	}

	private void spawnOrbSeller(int id) {
		adding = true;
		GameProfile gp = new GameProfile(UUID.randomUUID(), "§6Händler");
		Property texture = new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTU5OTY4NzI5NDUxNywKICAicHJvZmlsZUlkIiA6ICI4MmM2MDZjNWM2NTI0Yjc5OGI5MWExMmQzYTYxNjk3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3ROb3RvcmlvdXNOZW1vIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VmNjg2ZmJmMDE2ZTE0NThmN2Y5YTk4NTBmMzhlZjdmZDZhZTFkMjhiYzI0MWFjMGMwNjg2Zjc3ZmRiYjRiNjEiCiAgICB9CiAgfQp9", "T2RLCejF6IK/1AhIsSVIJWdPggI7p7Thg2tzi8VKxGtEvV4rTrZ+fu2o6J3vvPHt1JMZItouUumQzbiUsRF0gCF/xOJ+5O+sBl+iODrYVKbWp4ORcxSePKE3a5F0iBqalwGrQjAIXbLzARqW654Ma20FhlY4Th15RxVFJU/KFZ1Fv6bvKU0iW3c5GqxJy6Cb7bWjmg2sS1RnP3/44KvZtGEJOuW6WTQH1T12m3fGGbObsuxjVec3avtAu9nQ3vknPV6EYyVtv8VH/1wjEW2AkkMr0Zimy/3NpnUhUMOOpYcud4BHKAM/733ypfe+Qc4T6Fm8IYvopkvKNDxuZTd/ARlRHMlCUnTVScO+oox4HCEFnzgtLBBk3TPl6W8Ph3Rh55DabzWruf/WygVdFru1dfRs3hkmuEtNBlBfpH8FCjymz7b9ReQ3rHtZJbe+GBlIH8GuqZt1TrD1Xj2aSuOJwIku2GNnS0LUClTR5n17Nmr9yEzjsvdm0ac2ZJ2DOhLbUZufqcclo9eLdvzVO3dVo+lqOoWy/4yERRfUwc17cdGkLqhqFoWMnQftrYBFGDAi0nL3tmjd+2fTVSLhpFMgVl0fEu4aVN5C6t24oNwOUVvQi3hPTy2Rirs00sI8xrsU4Y1koRZBhZxOFg6Q77pePEQakeEHmhsIk1gW8JdrLDE=");
		gp.getProperties().put("textures", texture);

		// Add seller to tab list, required for the S0CPacketSpawnPlayer and the skin
		S38PacketPlayerListItem s38 = new S38PacketPlayerListItem();
		Reflection.set(s38, ADD_PLAYER, "action");
		s38.getEntries().add(s38.new AddPlayerData(gp, 0, WorldSettings.GameType.SURVIVAL, new ChatComponentText("§6Händler")));
		s38.processPacket(mc().getNetHandler());

		// Spawn the seller himself
		EntityOtherPlayerMP player = new EntityOtherPlayerMP(world(), gp);
		player.setEntityId(id);
		float yaw = (float) Math.toDegrees(Math.atan2(player().posZ + 41.5, player().posX - 172.5));
		yaw -= 90;
		player.setPositionAndRotation(172.5, 26, -41.5, yaw, 0);
		new S0CPacketSpawnPlayer(player).processPacket(mc().getNetHandler());
		world().getEntityByID(id).setRotationYawHead(yaw);

		// Remove player from tab list
		Reflection.set(s38, S38PacketPlayerListItem.Action.REMOVE_PLAYER, "action");
		s38.processPacket(mc().getNetHandler());
		adding = false;
	}

	private void saveIds() {
		String key = getCategory().getConfigKey() + ".orb_seller_fix.";
		Config.set(key + "reset", new JsonPrimitive(getNextServerRestart()));

		JsonObject ids = new JsonObject();
		for (Map.Entry<String, Integer> entry : cbToId.entrySet())
			ids.addProperty(entry.getKey(), entry.getValue());

		Config.set(key + "ids", ids);
		Config.save();
	}

}
