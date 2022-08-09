package dev.l3g7.griefer_utils.features.features.prefixsaver;

import dev.l3g7.griefer_utils.misc.ItemBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class GuiPrefixSaver extends GuiChest {

	private static final int ACCEPT_SLOT_ID = 12;
	private static final int DECLINE_SLOT_ID = 14;
	public static final IInventory inv = new InventoryBasic("§6Möchtest du den Prefix benutzen?", false, 27);

	public GuiPrefixSaver(Minecraft mc) {
		super(mc.thePlayer.inventory, inv);
	}

	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
		if (slotIn != null)
			slotId = slotIn.slotNumber;

		if (slotId == DECLINE_SLOT_ID)
			mc.thePlayer.closeScreenAndDropStack();

		if (slotId != ACCEPT_SLOT_ID)
			return;

		mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
		mc.displayGuiScreen(null);
	}

	static {
		ItemStack pane = new ItemBuilder(Blocks.stained_glass_pane)
		 .damage(7)
		 .build();

		for (int i = 0; i < 27; i++)
			inv.setInventorySlotContents(i, pane);

		inv.setInventorySlotContents(ACCEPT_SLOT_ID,
		 new ItemBuilder(Items.dye)
		 .damage(10) // Lime
		 .name("§aBestätigen")
		 .build()
		);


		inv.setInventorySlotContents(DECLINE_SLOT_ID,
		 new ItemBuilder(Items.dye)
		  .damage(1) // Red
		  .name("§cAbbrechen")
		  .build()
		);
	}
}
