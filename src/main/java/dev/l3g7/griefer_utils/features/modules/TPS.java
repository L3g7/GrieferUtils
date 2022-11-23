package dev.l3g7.griefer_utils.features.modules;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketReceiveEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Material;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S05PacketSpawnPosition;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.features.Feature.player;
import static dev.l3g7.griefer_utils.features.Feature.uuid;

/**
 * Concept by <a href="https://github.com/Pleezon/ServerTPS/blob/main/src/main/java/de/techgamez/pleezon/Main.java">Pleezon</a>
 */
@Singleton
public class TPS extends Module {

	private Double currentTPS = null;
	private Long lastWorldTime = null;
	private final List<Double> tps = new ArrayList<>();
	private long lastMillis = 0;
	private int lastTripTime = 0;

	public TPS() {
		super("Server-TPS", "Zeigt eine (relativ genaue) Schätzung der aktuellen Server-TPS an.", "server-tps", new ControlElement.IconData(Material.COMMAND));
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"?"};
	}

	@Override
	public String[] getValues() {
		if (currentTPS == null)
			return getDefaultValues();

		return new String[] {Math.round(currentTPS / .002) / 100 + "%"};
	}

	@EventListener
	public void onPacket(PacketReceiveEvent event) {
		Packet<?> packet = event.getPacket();

		if (packet instanceof S03PacketTimeUpdate) {
			calcTps(((S03PacketTimeUpdate) packet));
		} else if (packet instanceof S05PacketSpawnPosition) {
			lastWorldTime = null;
			tps.clear();
		}
	}

	private void calcTps(S03PacketTimeUpdate packet) {
		if (player() == null || mc.getNetHandler() == null)
			return;

		// Time it takes for the packet to reach the client
		NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(uuid());
		if (playerInfo == null)
			return;

		int tripTime = playerInfo.getResponseTime() / 2;

		long currentWorldTime = packet.getTotalWorldTime();
		long currentMillis = System.currentTimeMillis();

		if (lastWorldTime == null) {
			lastWorldTime = currentWorldTime;
			lastMillis = currentMillis;
			lastTripTime = tripTime;
			return;
		}

		int tripTimeDiff = (tripTime - lastTripTime) / 2;
		long ageDiff = currentWorldTime - lastWorldTime;
		long timeDiff = currentMillis - (lastMillis + tripTimeDiff);
		double currentTps = ageDiff / (timeDiff / 1000d);

		tps.add(Math.min(currentTps, 20));

		double averageTps = tps.stream().reduce(Double::sum).orElse(0d) / tps.size();
		currentTPS = Math.round(averageTps * 100) / 100d; // Round to two decimals

		if (tps.size() > 25)
			tps.remove(0);

		lastWorldTime = currentWorldTime;
		lastTripTime = tripTime;
		lastMillis = currentMillis;
	}

}
