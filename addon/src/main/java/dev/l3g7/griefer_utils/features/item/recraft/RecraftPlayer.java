package dev.l3g7.griefer_utils.features.item.recraft;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.item.recraft.Recraft.Action;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.play.client.C0DPacketCloseWindow;

import java.util.Iterator;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;


public class RecraftPlayer {

	public static boolean playing = false;

	private static Iterator<Action> iterator;
	private static Action current;
	private static boolean closeNext = false;

	/**
	 * starts the player
	 */
	public static void play() {
		if (iterator == null)
			reset();

		if (world() == null || !mc().inGameHasFocus || playing)
			return;

		reset();
		if (next()) {
			playing = true;
			MinecraftUtil.send("/rezepte");
		} else {
			MinecraftUtil.display(Constants.ADDON_PREFIX + " §cEs wurde kein \"/rezepte\"-Aufruf aufgezeichnet.");
		}

	}

	private static void reset() {
		iterator = RecraftRecorder.actions.iterator();
		playing = false;
	}

	private static boolean next() {
		boolean b = iterator.hasNext();
		if (b)
			current = iterator.next();
		return b;
	}


	@EventListener
	private static void onGuiOpen(GuiScreenEvent.GuiOpenEvent<GuiChest> event) {
		if (closeNext) {
			sendClosePacket();
			closeNext = false;
		}

		if (!playing)
			return;

		if (current.guiNameID != Recraft.getMenuID(event.gui)) {
			reset();
			return;
		}

		TickScheduler.runAfterRenderTicks(() -> {
			current.execute(event.gui);
			if (next())
				return;

			reset();
			closeNext = true;
			sendClosePacket();
		}, 1);
	}

	@EventListener
	private static void onCloseWindow(PacketEvent.PacketSendEvent<C0DPacketCloseWindow> event) {
		reset();
	}

	private static void sendClosePacket() {
		TickScheduler.runAfterClientTicks(() -> player().closeScreen(), 2);
	}

}