/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.item;

import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.Priority;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.MouseClickEvent;
import dev.l3g7.griefer_utils.event.events.TickEvent.ClientTickEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.ItemDisplaySetting;
import dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.ItemSaver;
import dev.l3g7.griefer_utils.features.item.item_saver.tool_saver.ToolSaver;
import dev.l3g7.griefer_utils.features.modules.MissingAdventurerBlocks;
import dev.l3g7.griefer_utils.features.uncategorized.settings.BugReporter;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.utils.Material;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.ImmutableList.of;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.minecraft.enchantment.Enchantment.*;

/**
 * Allowed on GrieferGames, as per <a href="https://forum.griefergames.de/faq/85-modifikationen/#entry-85">the list of recommended modifications</a>.<br>
 * (Inventory modifications, AutoSwitch)
 */
@Singleton
public class AutoTool extends Feature {

	private static final Pattern CUTTER_PATTERN = Pattern.compile("§7Klick §e(?<block>[^ ]+) §7abbauen\\.");
	private static final Map<String, List<Block>> CUTTERS = ImmutableMap.of(
		"Leuchtfeuer", of(Blocks.beacon),
		"Dracheneier", of(Blocks.dragon_egg),
		"Glasblöcke", of(Blocks.glass, Blocks.stained_glass, Blocks.glass_pane, Blocks.stained_glass),
		"Köpfe", of(Blocks.skull)
	);

	private static final Map<String, Block> RENAMED_BLOCKS = ImmutableMap.of(
		"stained_clay", Blocks.stained_hardened_clay,
		"hard_clay", Blocks.hardened_clay,
		"leaves_2", Blocks.leaves2,
		"mycel", Blocks.mycelium
	);

	private final ToolSaver toolSaver = FileProvider.getSingleton(ToolSaver.class);

	private final DropDownSetting<EnchantPreference> preference = new DropDownSetting<>(EnchantPreference.class)
		.name("Bevorzugte Verzauberung")
		.description("Ob Glück oder Behutsamkeit bevorzugt werden soll.")
		.stringProvider(EnchantPreference::getName)
		.icon(Material.ENCHANTED_BOOK)
		.defaultValue(EnchantPreference.FORTUNE);

	private final BooleanSetting switchBack = new BooleanSetting()
		.name("Zurück wechseln")
		.description("Ob nach dem Abbauen auf den ursprünglichen Slot zurück gewechselt werden soll.")
		.icon(Material.WOOD_PICKAXE)
		.defaultValue(true);

	private final BooleanSetting enforceSilkTouch = new BooleanSetting()
		.name("Behutsamkeit erzwingen")
		.description("Wenn Behutsamkeit einen Effekt auf den abgebaute Block hat, werden §nimmer§r Items mit Behutsamkeit bevorzugt.")
		.icon(Material.GRASS);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Automatische Werkzeugauswahl")
		.description("Wechselt beim Abbauen eines Blocks automatisch auf das beste Werkzeug in der Hotbar.")
		.icon(ItemUtil.createItem(Items.diamond_pickaxe, 0, true))
		.defaultValue(false)
		.subSettings(preference, switchBack, enforceSilkTouch);

	private int previousSlot = -1;


	@EventListener
	public void onTick(ClientTickEvent event) {
		if (world() == null || player() == null)
			return;

		if (previousSlot == -1 || mc().gameSettings.keyBindAttack.isKeyDown())
			return;

		switchToSlot(previousSlot);
		previousSlot = -1;
	}

	/**
	 * Required for compatability with {@link ToolSaver}
	 */
	@EventListener(priority = Priority.HIGH)
	public void onMouse(MouseClickEvent.LeftClickEvent event) {
		if (mc().objectMouseOver == null || mc().objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
			return;

		switchToTool(mc().objectMouseOver.getBlockPos());
	}

	@EventListener
	public void onPacket(PacketEvent.PacketSendEvent<C07PacketPlayerDigging> event) {
		if (event.packet.getStatus() != C07PacketPlayerDigging.Action.START_DESTROY_BLOCK)
			return;

		switchToTool(event.packet.getPosition());
	}

	private void switchToTool(BlockPos targetedBlock) {
		if (!ServerCheck.isOnGrieferGames() && player().getHeldItem() != null && player().getHeldItem().getItem() == Items.wooden_axe)
			return;

		IBlockState state = world().getBlockState(targetedBlock);

		if (state.getBlock().getBlockHardness(world(), targetedBlock) < 0) // Block can't be broken
			return;

		double bestScore = -1;
		int bestSlot = -1;

		// Get best slot
		for (int i = 0; i < 9; i++) {
			ItemStack stack = player().inventory.getStackInSlot(i);
			double currentScore = getScore(stack, state);

			if (bestScore < currentScore) {
				bestScore = currentScore;
				bestSlot = i;
			}
		}

		// Switch to the best slot, if it isn't the current one
		if (bestSlot != -1 && bestScore > getScore(player().inventory.getCurrentItem(), state)) {

			if (switchBack.get() && previousSlot == -1)
				previousSlot = player().inventory.currentItem;

			switchToSlot(bestSlot);
		}
	}

	public double getScore(ItemStack itemStack, IBlockState state) {
		ItemDisplaySetting ids = ItemSaver.getSetting(itemStack);
		if (ids != null && ids.leftclick.get())
			return Integer.MIN_VALUE;

		if (toolSaver.isEnabled())
			if (toolSaver.shouldCancel(itemStack))
				return Integer.MIN_VALUE;

		if (!ServerCheck.isOnGrieferGames() && player().getHeldItem() != null && player().getHeldItem().getItem() == Items.wooden_axe)
			return Integer.MIN_VALUE;

		if (isAdventureToolApplicable(itemStack, state))
			return Integer.MAX_VALUE;

		if (!isTool(itemStack)) {
			if (itemStack == null || !itemStack.isItemStackDamageable())
				return 1000.1; // If no good tool was found, something without damage should be chosen

			return 1000;
		}

		double score = 0;

		score += itemStack.getItem().getStrVsBlock(itemStack, state.getBlock()) * 1000; // Main mining speed
		if (itemStack.getItem() == Items.shears && state.getBlock() == Blocks.vine)
			score = 2000;

		if (isValidCutter(itemStack, state.getBlock()))
			score += 2500;

		if (score != 1000) { // Only test for these enchantments if the tool actually is fast
			score += EnchantmentHelper.getEnchantmentLevel(efficiency.effectId, itemStack);
			score += EnchantmentHelper.getEnchantmentLevel(unbreaking.effectId, itemStack);

			score += EnchantmentHelper.getEnchantmentLevel(fortune.effectId, itemStack) * (preference.get() != EnchantPreference.SILK_TOUCH ? 10 : 1);
			score += EnchantmentHelper.getEnchantmentLevel(silkTouch.effectId, itemStack) * (preference.get() != EnchantPreference.FORTUNE ? 10 : 1);
		}

		if (enforceSilkTouch.get() && state.getBlock().canSilkHarvest(world(), null, state, player()))
			score += EnchantmentHelper.getEnchantmentLevel(silkTouch.effectId, itemStack) * 10000;

		return score;
	}

	private static boolean isValidCutter(ItemStack stack, Block block) {
		if (stack.getItem() != Items.shears)
			return false;

		List<String> lore = ItemUtil.getLore(stack);
		if (lore.size() < 2)
			return false;

		if (stack.getTagCompound().getInteger("current") == 0)
			return false;

		String secondLine = lore.get(1);
		Matcher matcher = CUTTER_PATTERN.matcher(secondLine);
		if (!matcher.matches())
			return false;

		List<Block> blocks = CUTTERS.getOrDefault(matcher.group("block"), of());
		return blocks.contains(block);
	}

	public static boolean isTool(ItemStack itemStack) {
		if (itemStack == null)
			return false;

		return itemStack.getItem() instanceof ItemTool || itemStack.getItem() instanceof ItemShears;
	}

	private boolean isAdventureToolApplicable(ItemStack stack, IBlockState state) {
		if (stack == null || !stack.hasTagCompound())
			return false;

		// Not an adventure tool
		NBTTagCompound adv = stack.getTagCompound().getCompoundTag("adventure");
		if (adv.hasNoTags())
			return false;

		// Finished
		if (MissingAdventurerBlocks.getMissingBlocks(stack) <= 0)
			return false;

		// Not owned by the player
		if (!player().getUniqueID().equals(UUID.fromString(adv.getString("adventure.player"))))
			return false;

		String material = adv.getString("adventure.material").toLowerCase();
		int data = adv.getInteger("adventure.data");
		Block block = RENAMED_BLOCKS.getOrDefault(material, Block.getBlockFromName(material));
		if (block == null) {
			BugReporter.reportError(new Throwable("Adventure tool breaks unknown block: " + material));
			return false;
		}

		if (block != state.getBlock())
			return false;

		return data == -1 || data == block.getMetaFromState(state);
	}

	private void switchToSlot(int id) {
		player().inventory.currentItem = id;
		Reflection.invoke(mc().playerController, "syncCurrentPlayItem"); // Send switch packet
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