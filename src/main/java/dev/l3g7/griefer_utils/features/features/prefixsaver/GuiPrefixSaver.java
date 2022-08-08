package dev.l3g7.griefer_utils.features.features.prefixsaver;

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
		ItemStack pane = new ItemStack(Blocks.stained_glass_pane);
		pane.setItemDamage(7);
		for (int i = 0; i < 27; i++)
			inv.setInventorySlotContents(i, pane);

		ItemStack accept = new ItemStack(Items.dye);
		accept.setItemDamage(10); // Hellgrüne Farbe
		accept.setStackDisplayName("§aBestätigen");
		inv.setInventorySlotContents(ACCEPT_SLOT_ID, accept);


		ItemStack decline = new ItemStack(Items.dye);
		decline.setItemDamage(1); // Rote Farbe
		decline.setStackDisplayName("§cAbbrechen");
		inv.setInventorySlotContents(DECLINE_SLOT_ID, decline);
	}
}
