package dev.l3g7.griefer_utils.features.item.recraft;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import java.util.LinkedList;


@Singleton
public class RecraftRecorder
{

	protected LinkedList<Recraft.Action> actions = new LinkedList<>();

	int currentID = -1;

	public void reset()
	{
		actions.clear();
	}

	@EventListener
	public void onGuiOpen(GuiScreenEvent.GuiOpenEvent<?> event)
	{
		if (Recraft.player.playing) return;

		if (event.gui instanceof GuiChest) {
			GuiChest chest = (GuiChest) event.gui;
			int id = Recraft.recraft.getMenuID(Recraft.recraft.getChestName(chest));
			if (id == 0) {
				reset();
			}
			currentID = id;
		} else {
			currentID = -1;
		}
	}

	@EventListener
	public void onSendClick(PacketEvent.PacketSendEvent<C0EPacketClickWindow> event)
	{
		if (Recraft.player.playing) return;
		if (currentID == -1) return;
		C0EPacketClickWindow packet = event.packet;
		actions.add(new Recraft.Action(currentID, packet.getSlotId(), packet.getClickedItem(), packet.getMode(), packet.getUsedButton()));
	}
}