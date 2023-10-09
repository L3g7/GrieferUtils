package dev.l3g7.griefer_utils.features.item.recraft;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.play.client.C0DPacketCloseWindow;

import java.util.Iterator;


@Singleton
public class RecraftPlayer
{
	Iterator<Recraft.Action> iterator;
	Recraft.Action current;
	public boolean playing = false;

	private void reset()
	{
		iterator = Recraft.recorder.actions.iterator();
		playing = false;
	}

	/**
	 * starts the player
	 */
	public void play()
	{
		if (iterator == null) reset();
		if (Minecraft.getMinecraft().theWorld == null) return;
		if (!Minecraft.getMinecraft().inGameHasFocus) return;
		if (playing) return;
		reset();
		if (next()) {
			playing = true;
			MinecraftUtil.send("/rezepte");
		} else {
			MinecraftUtil.display("Keine aufgezeichneten rezepte");
		}
	}

	private boolean next()
	{
		boolean b = iterator.hasNext();
		if (b) {
			current = iterator.next();
		}
		return b;
	}

	boolean closeNext = false;

	@EventListener
	public void onGuiOpen(GuiScreenEvent.GuiOpenEvent<GuiChest> event)
	{
		if (closeNext) {
			Recraft.recraft.sendClosePacket();
			closeNext = false;
		}
		if (!playing) return;
		GuiChest chest = event.gui;
		int id = Recraft.recraft.getMenuID(Recraft.recraft.getChestName(chest));
		if (current.guiNameID == id) {
			TickScheduler.runAfterRenderTicks(() -> {
				current.execute(chest);
				if (!next()) {
					reset();
					closeNext = true;
					Recraft.recraft.sendClosePacket();
				}
			}, 1);
		} else {
			reset();
			System.err.printf("Expected %d, got %d\n", current.guiNameID, id);
		}
	}

	@EventListener
	public void onCloseWindow(PacketEvent.PacketSendEvent<C0DPacketCloseWindow> event)
	{
		reset();
	}
}