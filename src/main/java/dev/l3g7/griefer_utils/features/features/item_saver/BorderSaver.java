package dev.l3g7.griefer_utils.features.features.item_saver;

import dev.l3g7.griefer_utils.features.features.item_saver.ItemSaver.ItemSaverImpl;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.ConfirmGui;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;

@Singleton
public class BorderSaver extends ItemSaverImpl {

	private boolean clickedOnBlock = false;

	private final BooleanSetting enabled = new BooleanSetting()
		.name("RandSaver")
		.config("features.item_saver.border_saver.active")
		.description("Fragt beim Einlösen eines Randes nach einer Bestätigung.")
		.icon(new ItemBuilder(Blocks.obsidian).enchant())
		.defaultValue(true);

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		// For some reason, a RIGHT_CLICK_AIR PlayerInteractEvent is triggered right after the RIGHT_CLICK_BLOCK event
		if (clickedOnBlock) {
			clickedOnBlock = false;
			event.setCanceled(true);
			return;
		}

		if (event.action != RIGHT_CLICK_BLOCK)
			return;

		clickedOnBlock = true;

		ItemStack heldItem = player().getHeldItem();

		if (heldItem == null || !heldItem.hasTagCompound())
			return;

		// Check if it's a border
		if (!heldItem.getTagCompound().getBoolean("wall_effect"))
			return;

		event.setCanceled(true);
		TickScheduler.runNextTick(() -> ConfirmGui.openGui(
			"§6Möchtest du den Rand benutzen?",
			() -> mc().playerController.sendUseItem(player(), world(), player().getHeldItem())
		));
	}

}
