package dev.l3g7.griefer_utils.features.item;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.world.ChestSearch;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import static dev.l3g7.griefer_utils.util.ItemUtil.createItem;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR;

@Singleton
public class ParticleSaver extends Feature {

	private static final int ACCEPT_SLOT_ID = 12, DECLINE_SLOT_ID = 14;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("PartikelSaver")
		.description("Fragt beim Einlösen eines Partikel-Effekts nach einer Bestätigung.")
		.icon(createItem(Items.dye, 10, true));

	private final IInventory inv = new InventoryBasic(ChestSearch.marker + "§0Willst du den Effekt einlösen?", false, 27);

	public ParticleSaver() {
		ItemStack grayGlassPane = createItem(Blocks.stained_glass_pane, 7, "§8");

		// Fill inventory with gray glass panes
		for (int slot = 0; slot < 27; slot++)
			inv.setInventorySlotContents(slot, grayGlassPane);

		inv.setInventorySlotContents(ACCEPT_SLOT_ID, createItem(Items.dye, 10, "§aEinlösen"));
		inv.setInventorySlotContents(DECLINE_SLOT_ID, createItem(Items.dye, 1, "§cAbbrechen"));
	}

	@EventListener
	public void onPacket(PacketEvent.PacketSendEvent event) {
		if (event.packet instanceof C07PacketPlayerDigging) {
			C07PacketPlayerDigging.Action action = ((C07PacketPlayerDigging) event.packet).getStatus();
			if (action == C07PacketPlayerDigging.Action.DROP_ITEM || action == C07PacketPlayerDigging.Action.DROP_ALL_ITEMS)
				return;

			if (isHoldingParticle()) {
				event.setCanceled(true);
				displayScreen();
			}
		}
	}

	@EventListener
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!isHoldingParticle())
			return;

		event.setCanceled(true);
		if (event.action != RIGHT_CLICK_AIR)
			displayScreen();
	}

	private boolean isHoldingParticle() {
		return "§7Wird mit §e/deleteparticle §7zerstört.".equals(ItemUtil.getLastLore(MinecraftUtil.mc().thePlayer.getHeldItem()));
	}

	private void displayScreen() {
		MinecraftUtil.mc().displayGuiScreen(new GuiChest(player().inventory, inv) {


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

		});
	}

}
