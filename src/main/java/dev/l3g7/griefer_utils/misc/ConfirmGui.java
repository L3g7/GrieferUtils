package dev.l3g7.griefer_utils.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ConfirmGui {

	private static final ItemStack pane = new ItemStack(Blocks.stained_glass_pane, 1, 7);
	private static final int ACCEPT_SLOT_ID = 12;
	private static final int DECLINE_SLOT_ID = 14;
	private static final InventoryBasic inv = new InventoryBasic(null, true, 27);

	public static void openGui(String title, Runnable onAccept) {
		inv.setCustomName(title);
		inv.setInventorySlotContents(0, pane); // A S2FPacketSetSlot is being sent by the server when closing the gui
		Minecraft.getMinecraft().displayGuiScreen(new GuiChest(Minecraft.getMinecraft().thePlayer.inventory, inv) {

			protected void handleMouseClick(Slot slot, int slotId, int btn, int type) {
				if (slot != null)
					slotId = slot.slotNumber;

				if (slotId == DECLINE_SLOT_ID)
					mc.thePlayer.closeScreenAndDropStack();

				if (slotId != ACCEPT_SLOT_ID)
					return;

				onAccept.run();
				mc.displayGuiScreen(null);
			}

		});
	}

	static {
		for (int i = 0; i < 27; i++)
			inv.setInventorySlotContents(i, pane);

		inv.setInventorySlotContents(ACCEPT_SLOT_ID,
			new ItemBuilder(Items.dye)
				.damage(10) // Lime
				.name("§aBestätigen")
				.build());

		inv.setInventorySlotContents(DECLINE_SLOT_ID,
			new ItemBuilder(Items.dye)
				.damage(1) // Red
				.name("§cAbbrechen")
				.build());
	}

}
