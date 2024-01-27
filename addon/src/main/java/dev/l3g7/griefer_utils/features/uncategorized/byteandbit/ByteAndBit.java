package dev.l3g7.griefer_utils.features.uncategorized.byteandbit;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.event.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.byteandbit.data.BABBot;
import dev.l3g7.griefer_utils.features.uncategorized.byteandbit.gui.BotshopGUI;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S13PacketDestroyEntities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.pos;

/**
 * Implements any <a href="byteandbitstudio.de">ByteAndBit-Studio</a> features
 * This currently only includes functionality regarding bot "menus".
 */
@Singleton
public class ByteAndBit extends Feature {

	public final String BAB_URL = "https://velociraptor-service.byteandbit.cloud/microservice/velociraptorService/api/";

	final Map<UUID, BABBot> allBots = new ConcurrentHashMap<>();
	Map<UUID, BABBot> botsRendered = new ConcurrentHashMap<>();

	int ticksNotMoving = 0;
	int openGuiDebounce = 20;

	@ElementBuilder.MainElement
	private final BooleanSetting autoOpenGuiSetting =
		new BooleanSetting()
			.name("Botshop-GUI")
			.description("Öffnet automatisch die BotShop-GUI von unterstützten BotShops.")
			.icon(Material.PAPER);


	@OnEnable
	private void onEnable() {
		BABBot.initBots(allBots);
	}

	@EventListener
	public void onCBLeave(CitybuildJoinEvent event) {
		for (BABBot value : this.allBots.values()) {
			value.invalidateCache();
		}
	}

	@EventListener
	public void onNewPlayerDetect(PacketEvent.PacketReceiveEvent<S0CPacketSpawnPlayer> p) {
		BABBot bot = allBots.get(p.packet.getPlayer());
		if (bot == null) return;
		bot.requestIfNotCached().whenComplete((dataPresent, ex) -> {
			if (ex != null) {
				ex.printStackTrace();
				return;
			}
			if (!dataPresent) return;
			botsRendered.put(p.packet.getPlayer(), bot);
		});
	}

	@EventListener
	public void onEntityYeet(PacketEvent.PacketReceiveEvent<S13PacketDestroyEntities> p) {
		for (int id : p.packet.getEntityIDs()) {
			Entity e = Minecraft.getMinecraft().theWorld.getEntityByID(id);
			if (!(e instanceof EntityPlayer)) continue;
			EntityPlayer player = (EntityPlayer) e;
			if (!botsRendered.containsKey(player.getPersistentID())) continue;

			botsRendered.get(player.getPersistentID()).invalidateCache();
			botsRendered.remove(player.getPersistentID());
		}
	}

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {
		if (Minecraft.getMinecraft().theWorld == null) return;
		if (!autoOpenGuiSetting.get()) return;
		if (Minecraft.getMinecraft().currentScreen instanceof BotshopGUI) ticksNotMoving = -40;
		for (BABBot b : botsRendered.values()) {
			if (b.getBotzone() == null) continue;
			if (!b.getBotzone().isVecInside(pos(player())) || isPlayerMoving()) {
				ticksNotMoving = 0;
				continue;
			}
			if (ticksNotMoving <= openGuiDebounce) {
				ticksNotMoving++;
				continue;
			}
			if (Minecraft.getMinecraft().currentScreen != null) return;
			if (b.getName() == null) return;

			Minecraft.getMinecraft().displayGuiScreen(new BotshopGUI(b));
		}
	}

	private boolean isPlayerMoving() {
		return (player().motionX != 0) || (player().motionZ != 0);
	}
}
