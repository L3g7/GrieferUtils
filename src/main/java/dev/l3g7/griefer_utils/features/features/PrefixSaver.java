package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Singleton
public class PrefixSaver extends Feature {

	private final BooleanSetting enabled = new BooleanSetting()
			.name("PrefixSaver")
			.config("features.prefix_saver.active")
			.description("Fragt beim Einlösen eines Prefixes nach einer Bestätigung.")
			.icon(new ItemBuilder(Blocks.redstone_ore).enchant())
			.defaultValue(false);

	private static final int ACCEPT_SLOT_ID = 12;
	private static final int DECLINE_SLOT_ID = 14;
	private static final IInventory inv = new InventoryBasic("§6Möchtest du den Prefix benutzen?", false, 27);

	public PrefixSaver() {
		super(Category.FEATURE);
		ItemStack pane = new ItemStack(Blocks.stained_glass_pane, 1, 7);

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

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@SubscribeEvent
	public void onMouseClick(MouseEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		if (event.button != 1 || !event.buttonstate)
			return;

		if (!"§fVergibt §aein Farbrecht§f! (Rechtsklick)".equals(ItemUtil.getLastLore(mc().thePlayer.getHeldItem())))
			return;

		event.setCanceled(true);
		TickScheduler.runNextTick(() -> mc().displayGuiScreen(new GuiChest(player().inventory, inv) {

			protected void handleMouseClick(Slot slot, int slotId, int btn, int type) {
				if (slot != null)
					slotId = slot.slotNumber;

				if (slotId == DECLINE_SLOT_ID)
					mc.thePlayer.closeScreenAndDropStack();

				if (slotId != ACCEPT_SLOT_ID)
					return;

				mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
				mc.displayGuiScreen(null);
			}

		}));
	}

}
