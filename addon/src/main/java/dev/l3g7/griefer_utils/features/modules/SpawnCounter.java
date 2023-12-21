/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.modules;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.core.misc.functions.Consumer;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.misc.Named;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import net.labymod.main.LabyMod;
import net.labymod.utils.Material;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.List;

import static dev.l3g7.griefer_utils.core.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.core.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.minecraft.util.EnumFacing.*;

@Singleton
public class SpawnCounter extends Module {

	private static final EnumFacing[] HORIZONTALS = new EnumFacing[] {SOUTH, WEST, NORTH, EAST};
	private static final List<String> excludedCitybuilds = ImmutableList.of("Nature", "Extreme", "Lava", "Wasser", "CBE");
	private static final byte[] quadrantData = new byte[] {
		-1, -1,
		-1, +1,
		+1, -1,
		+1, +1
	};

	private World spawnWorld;
	private AxisAlignedBB spawnBox;
	private BlockPos spawnMiddle;
	private byte startQuadrant = -1;
	private byte visitedQuadrants = 0;

	private final String configKey;
	private int roundsFlown;
	private int roundsRan;

	private boolean hasFlown;
	private boolean accountForStartBonus;
	private long startTime = 0;

	private final DropDownSetting<NotificationType> notificationType = new DropDownSetting<>(NotificationType.class)
		.name("Nachricht")
		.description("Wie die Benachrichtung aussehen soll, wenn eine Runde abgeschlossen wurde.")
		.icon(Material.WATCH)
		.defaultValue(NotificationType.ACTIONBAR);

	private final DropDownSetting<DisplayType> displayType = new DropDownSetting<>(DisplayType.class)
		.name("Rundenart")
		.description("Welche Arten von Runden angezeigt werden sollen.")
		.icon("speed")
		.defaultValue(DisplayType.BOTH);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Spawn-Runden Zähler")
		.description("Zählt, wie viele Runden um den Spawn gelaufen wurden.")
		.icon("speed")
		.subSettings(notificationType, displayType);

	public SpawnCounter() {
		configKey = "modules.spawn_counter.rounds_";

		if (Config.has(configKey + "flown"))
			roundsFlown = Config.get(configKey + "flown").getAsInt();
		if (Config.has(configKey + "ran"))
			roundsRan = Config.get(configKey + "ran").getAsInt();
	}

	@Override
	public String[] getValues() {
		return getDefaultValues();
	}

	@Override
	public String[] getDefaultValues() {
		StringBuilder value = new StringBuilder();

		if (displayType.get() != DisplayType.FLOWN)
			value.append("  ").append(DECIMAL_FORMAT_98.format(roundsRan));
		if (displayType.get() == DisplayType.BOTH)
			value.append(" ");
		if (displayType.get() != DisplayType.RAN)
			value.append("  ").append(DECIMAL_FORMAT_98.format(roundsFlown));

		return new String[] { value.toString() };
	}

	@Override
	public void draw(double x, double y, double rightX) {
		super.draw(x, y, rightX);

		double xDiff = mc.fontRendererObj.getStringWidth(new Text(getKeys()[0], 0, bold, italic, underline).getText());

		// Add padding
		y += padding;
		if (rightX == -1)
			xDiff += padding;

		if (isKeyVisible()) {
			switch (getDisplayFormatting()) {
				case SQUARE_BRACKETS:
					xDiff += getStringWidth("[]");
					break;
				case BRACKETS:
					xDiff += getStringWidth(">");
					break;
				case COLON:
					xDiff += getStringWidth(":");
					break;
				case HYPHEN:
					xDiff += getStringWidth(" -");
					break;
			}

			xDiff += getStringWidth(" ");
		}

		if (displayType.get() != DisplayType.FLOWN) {
			mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/speed.png"));
			LabyMod.getInstance().getDrawUtils().drawTexture(x + xDiff, y, 256, 256, 7, 7);
			xDiff += getStringWidth("  " + roundsRan);
		}

		if (displayType.get() == DisplayType.BOTH)
			xDiff += getStringWidth(" ");

		if (displayType.get() != DisplayType.RAN) {
			mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/booster/fly.png"));
			LabyMod.getInstance().getDrawUtils().drawTexture(x + xDiff, y, 256, 256, 7, 7);
		}

	}

	@EventListener
	private void onTick(TickEvent.ClientTickEvent event) {
		if (player() == null || spawnWorld == null)
			return;

		boolean isInSpawnBox = player().posX > spawnBox.minX && player().posX < spawnBox.maxX
			&& player().posZ > spawnBox.minZ && player().posZ < spawnBox.maxZ;

		// Rough check if the player is at spawn (and not in another world)
		if (isInSpawnBox || world() != spawnWorld) {
			visitedQuadrants = 0;
			startQuadrant = -1;
			return;
		}

		if (player().capabilities.isFlying)
			hasFlown = true;

		BlockPos playerPos = player().getPosition().subtract(spawnMiddle);

		for (byte i = 0; i < 4; i++) {
			byte x = quadrantData[i * 2];
			byte z = quadrantData[i * 2 + 1];

			if (Math.signum(playerPos.getX()) != x || Math.signum(playerPos.getZ()) != z)
				continue;

			if (startQuadrant == -1) {
				startQuadrant = i;
				accountForStartBonus = true;
				startTime = System.currentTimeMillis();
			}

			visitedQuadrants |= 1 << i;

			// Check if player has completed a round
			if (visitedQuadrants != 15 || startQuadrant != i)
				return;

			visitedQuadrants = 0;
			long delta = System.currentTimeMillis() - startTime;
			startTime = System.currentTimeMillis();

			if (accountForStartBonus) {
				delta *= 1.25d;
				accountForStartBonus = false;
			}

			if (hasFlown) {
				roundsFlown++;
				notificationType.get().notifier.accept(String.format("§fDu bist deine §e%dte§f Runde in §e%.1f§f Sekunden abgeflogen!", roundsFlown, Math.round(delta / 100d) / 10d));
				Config.set(configKey + "flown", new JsonPrimitive(roundsFlown));
				hasFlown = false;
			} else {
				roundsRan++;
				notificationType.get().notifier.accept(String.format("§fDu bist deine §e%dte§f Runde in §e%.1f§f Sekunden abgelaufen!", roundsRan, Math.round(delta / 100d) / 10d));
				Config.set(configKey + "ran", new JsonPrimitive(roundsRan));
			}
			Config.save();
		}
	}

	@EventListener(triggerWhenDisabled = true)
	private void onMessageReceive(MessageEvent.MessageReceiveEvent event) {
		if (!ServerCheck.isOnGrieferGames() || player() == null)
			return;

		if (!event.message.getFormattedText().equals("§r§2§l[Switcher] §r§eLade Daten herunter!§r"))
			return;

		visitedQuadrants = 0;
		startQuadrant = -1;
		spawnWorld = null;
		determineSpawn(player().getPosition().down());
	}

	@EventListener
	private void onPlayerTeleport(PacketReceiveEvent<S08PacketPlayerPosLook> event) {
		visitedQuadrants = 0;
		startQuadrant = -1;
	}

	private void determineSpawn(BlockPos pos) {
		if (excludedCitybuilds.contains(getServerFromScoreboard()))
			return;

		for (EnumFacing f : HORIZONTALS) {
			BlockPos a = pos.offset(f);
			BlockPos b = pos.offset(f.rotateY());
			if (!isSpawnMiddle(a) || !isSpawnMiddle(b))
				continue;

			spawnMiddle = new BlockPos(Math.max(a.getX(), b.getX()), pos.getY(), Math.max(a.getZ(), b.getZ()));

			spawnBox = new AxisAlignedBB(spawnMiddle, spawnMiddle).expand(18, 0, 18);
			spawnWorld = world();

			return;
		}
	}

	private boolean isSpawnMiddle(BlockPos pos) {
		Block targetBlock = getServerFromScoreboard().equals("Event") ? Blocks.quartz_block : Blocks.stonebrick;
		return world().getBlockState(pos).getBlock() == targetBlock;
	}

	private int getStringWidth(String text) {
		return mc.fontRendererObj.getStringWidth(new Text(text, 0, bold, italic, underline).getText());
	}

	private enum NotificationType implements Named {

		NONE("Keine", s -> {}),
		TOAST("Erfolg", s -> displayAchievement("§aSpawn-Runden Zähler", s)),
		ACTIONBAR("Aktionsleiste", s -> mc().ingameGUI.setRecordPlaying(s, true)),
		MESSAGE("Chatnachricht", s -> display(ADDON_PREFIX + s));

		private final String name;
		private final Consumer<String> notifier;

		NotificationType(String name, Consumer<String> notifier) {
			this.name = name;
			this.notifier = notifier;
		}

		@Override
		public String getName() {
			return name;
		}

	}

	private enum DisplayType implements Named {

		RAN("Laufen"),
		FLOWN("Fliegen"),
		BOTH("Beides");

		private final String name;

		DisplayType(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
