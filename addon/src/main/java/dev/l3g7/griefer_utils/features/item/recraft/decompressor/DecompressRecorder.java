package dev.l3g7.griefer_utils.features.item.recraft.decompressor;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.event.events.WindowClickEvent;
import dev.l3g7.griefer_utils.features.item.recraft.Recraft;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftAction.Ingredient;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftRecording;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

public class DecompressRecorder {

	private static RecraftRecording recording = Recraft.tempRecording;

	private static GuiScreen previousScreen = null;

	public static void startRecording(RecraftRecording recording) {
		if (!ServerCheck.isOnCitybuild()) {
			displayAchievement("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild gestartet werden.");
			return;
		}

		DecompressRecorder.recording = recording;
		previousScreen = mc().currentScreen;

		display(ADDON_PREFIX + "Bitte klicke das Item an, das du dekomprimieren möchtest.");
		mc().displayGuiScreen(new GuiInventory(player()));
	}

	@EventListener
	private static void onGuiOpenEvent(GuiOpenEvent<?> event) {
	    if (previousScreen == null)
			return;

		if (event.gui instanceof GuiInventory || event.gui == previousScreen)
			return;

		event.cancel();
		mc().displayGuiScreen(previousScreen);
		previousScreen = null;
	}

	@EventListener
	private static void onAddItem(WindowClickEvent event) {
		if (previousScreen == null || event.itemStack == null)
			return;

		ItemStack stack = event.itemStack.copy();
		stack.stackSize = 0;
		recording.mainSetting.icon(stack);

		recording.actions.add(new DecompressAction(new Ingredient(stack, 0)));
		mc().displayGuiScreen(previousScreen);
		previousScreen = null;
		event.cancel();
	}

}
