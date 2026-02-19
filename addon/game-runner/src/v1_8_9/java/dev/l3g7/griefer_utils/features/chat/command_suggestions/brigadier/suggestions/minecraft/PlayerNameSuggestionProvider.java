package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.suggestions.minecraft;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.nodes.minecraft.PlayerNameNode;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

import java.util.concurrent.CompletableFuture;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

/**
 * The suggestion provider for {@link PlayerNameNode}.
 */
public class PlayerNameSuggestionProvider {

	private static final Object LOCK = new Object();
	private static long requestTime = 0;
	private static CompletableFuture<String[]> results = CompletableFuture.completedFuture(new String[0]);

	public static CompletableFuture<String[]> request(String prefix) {
		if (!prefix.isEmpty())
			return results;

		long now = System.currentTimeMillis();
		synchronized (LOCK) {
			if ((now - requestTime) < 5000)
				return results;

			requestTime = System.currentTimeMillis();
			results = new CompletableFuture<>();
		}

		BlockPos target = null;
		if (mc().objectMouseOver != null && mc().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			target = mc().objectMouseOver.getBlockPos();

		player().sendQueue.addToSendQueue(new C14PacketTabComplete("", target));

		return results;
	}

	@EventListener
	public static void onComplete(PacketReceiveEvent<S3APacketTabComplete> event) {
		synchronized (LOCK) {
			if (!results.isDone())
				results.complete(event.packet.func_149630_c());
		}
	}

}
