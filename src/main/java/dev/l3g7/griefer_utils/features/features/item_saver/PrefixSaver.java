package dev.l3g7.griefer_utils.features.features.item_saver;

import dev.l3g7.griefer_utils.features.features.item_saver.ItemSaver.ItemSaverImpl;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.ConfirmGui;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.LEFT_CLICK_BLOCK;

@Singleton
public class PrefixSaver extends ItemSaverImpl {

	private final BooleanSetting enabled = new BooleanSetting()
			.name("PrefixSaver")
			.config("features.item_saver.prefix_saver.active")
			.description("Fragt beim Einlösen eines Prefixes nach einer Bestätigung.")
			.icon(new ItemBuilder(Blocks.redstone_ore).enchant())
			.defaultValue(true);

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		if (event.action == LEFT_CLICK_BLOCK)
			return;

		if (!"§fVergibt §aein Farbrecht§f! (Rechtsklick)".equals(ItemUtil.getLastLore(mc().thePlayer.getHeldItem())))
			return;

		event.setCanceled(true);
		TickScheduler.runNextTick(() -> ConfirmGui.openGui(
			"§6Möchtest du den Prefix benutzen?",
			() -> mc().playerController.sendUseItem(player(), world(), player().getHeldItem())
		));
	}

}
