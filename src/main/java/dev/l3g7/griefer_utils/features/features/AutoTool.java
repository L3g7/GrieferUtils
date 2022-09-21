package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketSendEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.START_DESTROY_BLOCK;

@Singleton
public class AutoTool extends Feature {

	private final DropDownSetting<EnchantPreference> preference = new DropDownSetting<>(EnchantPreference.class)
		.name("Bevorzugte Verzauberung")
		.description("Ob Glück oder Behutsamkeit bevorzugt werden soll.")
		.config("features.auto_tool.preference")
		.stringProvider(EnchantPreference::getName)
		.icon(Material.ENCHANTED_BOOK)
		.defaultValue(EnchantPreference.FORTUNE);

	private final BooleanSetting switchBack = new BooleanSetting()
		.name("Zurück wechseln")
		.description("Ob nach dem Abbauen auf den ürsprünglichen Slot zurück gewechselt werden soll.")
		.config("features.auto_tool.switch_back")
		.icon(Material.WOOD_PICKAXE)
		.defaultValue(true);

	private final BooleanSetting enabled = new BooleanSetting()
		.name("AutoTool")
		.description("Wechselt beim Abbauen eines Blocks automatisch auf das beste Werkzeug in der Hotbar.")
		.config("features.auto_tool.active")
		.icon(new ItemBuilder(Items.diamond_pickaxe).enchant())
		.defaultValue(false)
		.subSettingsWithHeader("AutoTool", preference, switchBack);

	private int previousSlot = -1;

	public AutoTool() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@SubscribeEvent
	public void onTick(TickEvent.PlayerTickEvent event) {
		if (!isActive() || world() == null || player() == null)
			return;

		if (previousSlot == -1 || mc().gameSettings.keyBindAttack.isKeyDown())
			return;

		switchToSlot(previousSlot);
		previousSlot = -1;
	}

	@EventListener
	public void onPacketSend(PacketSendEvent event) {
		if (!isActive() || !isOnGrieferGames() || world() == null || player() == null)
			return;

		if (!(event.getPacket() instanceof C07PacketPlayerDigging))
			return;

		C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.getPacket();

		if (packet.getStatus() != START_DESTROY_BLOCK)
			return;

		BlockPos pos = packet.getPosition();
		Block block = world().getBlockState(pos).getBlock();

		if (block.getBlockHardness(world(), pos) < 0) // Block can't be broken
			return;

		double bestScore = -1;
		int bestSlot = -1;

		// Get best slot
		for (int i = 0; i < 9; i++) {
			ItemStack stack = player().inventory.getStackInSlot(i);
			double currentScore = getScore(stack, block);

			if (bestScore < currentScore) {
				bestScore = currentScore;
				bestSlot = i;
			}
		}

		// Switch to the best slot, if it isn't the current one
		if (bestSlot != -1 && bestScore > getScore(player().inventory.getCurrentItem(), block)) {

			if (switchBack.get() && previousSlot == -1)
				previousSlot = player().inventory.currentItem;

			switchToSlot(bestSlot);
		}
	}

	public double getScore(ItemStack itemStack, Block block) {
		if (!isTool(itemStack)) {

			if (itemStack == null || !itemStack.isItemStackDamageable())
				return 1000.1; // If no good tool was found, something without damage should be chosen

			return 1000;
		}

		double score = 0;

		score += itemStack.getItem().getStrVsBlock(itemStack, block) * 1000; // Main mining speed

		if (score != 1000) { // Only test for these enchantments if the tool actually is fast
			score += EnchantmentHelper.getEnchantmentLevel(32, itemStack); // Efficiency
			score += EnchantmentHelper.getEnchantmentLevel(34, itemStack); // Unbreaking
		}

		if (preference.get() == EnchantPreference.FORTUNE)
			score += EnchantmentHelper.getEnchantmentLevel(35, itemStack);
		if (preference.get() == EnchantPreference.SILK_TOUCH)
			score += EnchantmentHelper.getEnchantmentLevel(33, itemStack);

		return score;
	}

	private boolean isTool(ItemStack itemStack) {
		if (itemStack == null)
			return false;

		return itemStack.getItem() instanceof ItemTool || itemStack.getItem() instanceof ItemShears;
	}

	private void switchToSlot(int id) {
		player().inventory.currentItem = id;
		Reflection.invoke(mc().playerController, new String[] {"syncCurrentPlayItem", "func_78750_j", "n"}); // Send switch packet
	}

	private enum EnchantPreference {

		NONE("Egal"),
		FORTUNE("Glück"),
		SILK_TOUCH("Behutsamkeit");

		final String name;

		EnchantPreference(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

}
