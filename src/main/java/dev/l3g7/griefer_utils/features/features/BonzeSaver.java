package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketSendEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.DROP_ALL_ITEMS;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.DROP_ITEM;

@Singleton
public class BonzeSaver extends Feature {

	private final BooleanSetting enabled = new BooleanSetting()
			.name("Birth- / BonzeSaver")
			.description("Deaktiviert Linksklicks, wenn eine Birth- oder eine Bonzeklinge in der Hand gehalten wird.")
			.icon(new ItemBuilder(Items.diamond_sword).enchant())
			.defaultValue(true)
			.config("features.bonze_saver.active");

	public BonzeSaver() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@SubscribeEvent
	public void onMouse(MouseEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		// Only disable left clicks
		if (event.button != 0 || !event.buttonstate)
			return;

		if (player() == null)
			return;

		InventoryPlayer inv = player().inventory;
		if (inv.getCurrentItem() == null)
			return;

		if (shouldBeSaved(inv.getCurrentItem()))
			event.setCanceled(true);
	}

	@EventListener
	public void onPacketSend(PacketSendEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		if (!(event.getPacket() instanceof C07PacketPlayerDigging))
			return;

		C07PacketPlayerDigging.Action action = ((C07PacketPlayerDigging) event.getPacket()).getStatus();

		// Prevent dropping
		if ((action == DROP_ITEM || action == DROP_ALL_ITEMS) && shouldBeSaved(player().getHeldItem()))
			event.setCanceled(true);
	}

	private boolean shouldBeSaved(ItemStack itemStack) {
		if (itemStack == null)
			return false;

		if (itemStack.getItem() != Items.diamond_sword)
			return false;

		return EnchantmentHelper.getEnchantmentLevel(Enchantment.looting.effectId, itemStack) == 21;
	}

}
