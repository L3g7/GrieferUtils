package dev.l3g7.griefer_utils.features.item.recraft.decompressor;

import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction.Ingredient;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftRecording;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftAction;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftPlayer;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

public class DecompressPlayer {

	private static final RecraftRecording craftRecording = new RecraftRecording();
	private static RecraftRecording recording;

	public static void play(RecraftRecording recording) {
		if (world() == null || !mc().inGameHasFocus)
			return;

		if (!ServerCheck.isOnCitybuild()) {
			displayAchievement("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild abgespielt werden.");
			return;
		}

		if (recording.actions.isEmpty()) {
			displayAchievement("§e§lFehler \u26A0", "§eDiese Aufzeichnung ist leer!");
			return;
		}

		DecompressPlayer.recording = recording;
		Ingredient ingredient = ((DecompressAction) recording.actions.get(0)).ingredient;
		craft(ingredient, true);
	}

	private static boolean craft(Ingredient ingredient, boolean firstExecute) {
		ItemStack[] inv = player().inventory.mainInventory;

		int freeSlots = 0;

		for (ItemStack stack : inv)
			if (stack == null)
				freeSlots++;

		int slot = getSlotWithLowestCompression(ingredient, freeSlots);
		if (slot < 0) {
			if (slot == -1)
				displayAchievement("§e§lFehler \u26A0", "Du hast nicht genügend Platz im Inventar!");

			recording.playSuccessor();
			return true;
		}

		ItemStack stack = player().inventory.mainInventory[slot];
		startCrafting(new PredeterminedIngredient(stack, slot), firstExecute);
		return false;
	}

	private static void startCrafting(PredeterminedIngredient ingredient, boolean reset) {
		Ingredient[] ingredients = new Ingredient[9];
		ingredients[0] = ingredient;

		craftRecording.actions.clear();
		craftRecording.actions.add(new CraftAction(ingredients));

		CraftPlayer.play(craftRecording, () -> craft(ingredient, false), reset);
	}

	private static int getSlotWithLowestCompression(Ingredient ingredient, int freeSlots) {
		int compression = 8;
		int slot = -1;
		int placeChecksFailed = 0;
		ItemStack[] inv = player().inventory.mainInventory;

		for (int i = 0; i < inv.length; i++) {
			Ingredient slotIngredient = Ingredient.fromItemStack(inv[i]);
			if (!ingredient.itemEquals(slotIngredient))
				continue;

			if (slotIngredient.compression >= compression)
				continue;

			if (slotIngredient.compression == 0)
				continue;

			if (getAdditionalRequiredSlots(slotIngredient.compression, inv[i].stackSize) > freeSlots) {
				placeChecksFailed++;
				continue;
			}

			compression = slotIngredient.compression;
			slot = i;
		}

		if (slot == -1 && placeChecksFailed == 0)
			return -2;

		return slot;
	}

	private static int getAdditionalRequiredSlots(int compression, int amount) {
		if (compression == 0)
			return 0;

		int items = 9 * amount;
		int stacks = (int) Math.ceil(items / 64d) - 1;
		int rest = items - stacks * 64;

		return stacks + getAdditionalRequiredSlots(compression - 1, rest);
	}

	private static class PredeterminedIngredient extends Ingredient {

		private final int slot;

		public PredeterminedIngredient(ItemStack stack, int slot) {
			super(stack, -1);
			this.slot = slot;
		}

		@Override
		public int getSlot(int[] excludedSlots) {
			return slot;
		}

		@Override
		protected boolean equals(Ingredient other) {
			if (other == null)
				return false;

			return meta == other.meta
				&& itemId == other.itemId;
		}

	}

}
