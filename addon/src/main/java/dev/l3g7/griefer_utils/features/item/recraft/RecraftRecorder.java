package dev.l3g7.griefer_utils.features.item.recraft;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import java.util.LinkedList;

public class RecraftRecorder {

	static LinkedList<Recraft.Action> actions = new LinkedList<>();

	private static int currentID = -1;

	@EventListener
	private static void onGuiOpen(GuiScreenEvent.GuiOpenEvent<?> event) {
		if (RecraftPlayer.playing)
			return;

		if (!(event.gui instanceof GuiChest)) {
			currentID = -1;
			return;
		}

		GuiChest chest = (GuiChest) event.gui;
		int id = Recraft.getMenuID(chest);
		if (id == 0)
			actions.clear();

		currentID = id;
	}

	@EventListener
	private static void onSendClick(PacketEvent.PacketSendEvent<C0EPacketClickWindow> event) {
		if (RecraftPlayer.playing || currentID == -1)
			return;

		C0EPacketClickWindow packet = event.packet;
		if (packet.getClickedItem() == null || packet.getClickedItem().getDisplayName().equals("§7"))
			return;

		actions.add(new Recraft.Action(currentID, packet.getSlotId(), packet.getClickedItem(), packet.getMode(), packet.getUsedButton()));
	}

}