package dev.l3g7.griefer_utils.features.uncategorized.byte_and_bit;


import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.bridges.laby3.settings.KeySettingImpl;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.byte_and_bit.data.BABBot;
import dev.l3g7.griefer_utils.features.uncategorized.byte_and_bit.gui.BotshopGUI;
import net.labymod.api.client.gui.screen.key.Key;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

/**
 * Implements any <a href="byteandbitstudio.de">ByteAndBit-Studio</a> features
 * This currently only includes functionality regarding bot "menus".
 */
@Singleton
public class ByteAndBit extends Feature {

	public static final String BAB_URL = "https://api.velociraptor-bot.de/api/";

	protected static final Map<String, BABBot> allBots = new ConcurrentHashMap<>();
	Map<String, BABBot> renderedBots = new ConcurrentHashMap<>();

	@MainElement
	private final KeySetting keybind = KeySetting.create()
		.name("Botshop-GUI")
		.description("Öffnet das BotShop-GUI von unterstützten BotShops.")
		.icon("byte_and_bit")
		.defaultValue(new HashSet<>(Collections.singletonList(Keyboard.KEY_RETURN)))
		.pressCallback(this::onKeyPress);

	@OnEnable
	private void onEnable() {
		this.syncBots();
		TickScheduler.runAfterClientTicks(this::syncBots, 60 * 20 * 5);
	}

	private void syncBots() {
		IOUtil.read(BAB_URL + "scope/getBots").asJsonObject((res) -> {
			if (!res.get("success").getAsBoolean())
				return;

			for (JsonElement entry : res.get("bots").getAsJsonArray()) {
				String uuid = entry.getAsString().replaceAll("-", "");
				allBots.put(uuid, new BABBot(uuid));
			}
		});
	}

	@EventListener(triggerWhenDisabled = true)
	public void onCBLeave(CitybuildJoinEvent event) {
		for (BABBot value : allBots.values()) {
			value.invalidateCache();
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onNewPlayerDetect(PacketReceiveEvent<S0CPacketSpawnPlayer> p) {
		String shortUuid = p.packet.getPlayer().toString().replaceAll("-", "");
		BABBot bot = allBots.get(shortUuid);
		if (bot == null) return;
		bot.sync().whenComplete((dataPresent, ex) -> {
			if (ex != null) {
				ex.printStackTrace();
				return;
			}
			if (!dataPresent) return;
			renderedBots.put(shortUuid, bot);
		});
	}

	@EventListener
	public void onEntityYeet(PacketReceiveEvent<S13PacketDestroyEntities> p) {
		if (world() == null || player() == null)
			return;

		for (int id : p.packet.getEntityIDs()) {
			Entity e = world().getEntityByID(id);
			if (!(e instanceof EntityPlayer player)) continue;

			String uuid = player.getUniqueID().toString().replaceAll("-", "");
			if (!renderedBots.containsKey(uuid)) continue;

			renderedBots.get(uuid).invalidateCache();
			renderedBots.remove(uuid);
		}
	}

	@EventListener
	public void onRenderTick(RenderWorldLastEvent e) {
		for (BABBot bot : renderedBots.values()) {
			if (bot.botZone == null)
				continue;

			GlStateManager.disableDepth();
			GlStateManager.enableDepth();

			if (bot.isVecInsideOrTouching(player().getPositionVector())) {
				drawTooltip();
				bot.sync();
			}
		}
	}

	void drawTooltip() {
		String keys;
		if (LABY_4.isActive()) {
			keys = Key.concat(keybind.get().stream().map(Key::get).collect(Collectors.toSet()));
		} else {
			keys = KeySettingImpl.formatKeys(keybind.get());
		}
		BossStatus.bossName = "BotShop-GUI verfügbar! [" + keys + "]";
		BossStatus.statusBarTime = 1;
		BossStatus.healthScale = 0f;
	}

	public void onKeyPress(boolean b) {
		if (!b) return;
		for (BABBot bot : renderedBots.values()) {
			if (bot.botZone == null)
				continue;

			if (bot.isVecInsideOrTouching(player().getPositionVector())) {
				bot.sync().whenComplete((dataPresent, ex) -> {
					if (ex == null)
						mc().addScheduledTask(() -> mc().displayGuiScreen(new BotshopGUI(bot)));
				});
			}
		}
	}
}