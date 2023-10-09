package dev.l3g7.griefer_utils.features.item.recraft;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import java.util.Objects;

/**
 * Purpose of this class is to automate the tedious gui-navigation of GrieferGames'
 * /rezepte. The functionality currently implemented allows the player to quickly recraft the last
 * crafted item using the server's crafting command.
 */
@Singleton
public class Recraft extends Feature
{
	public static RecraftPlayer player = FileProvider.getSingleton(RecraftPlayer.class);
	public static RecraftRecorder recorder = FileProvider.getSingleton(RecraftRecorder.class);
	public static Recraft recraft = FileProvider.getSingleton(Recraft.class);

	static String[] menuNames = new String[]{
		"§6Custom-Kategorien",
		"§6Möbel-Kategorien",
		"§6Möbel-Liste",
		"§6Bauanleitung",
		"§6Item-Komprimierung-Bauanleitung",
		"§6Item-Komprimierung",
		"§6Minecraft Rezepte",
		"§6Vanilla Bauanleitung",
		"§6Custom-Liste",
		"§6Custom-Bauanleitung"
	};

	int getMenuID(String name)
	{
		for (int i = 0; i < menuNames.length; i++) {
			if (Objects.equals(menuNames[i], name)) return i;
		}
		return -1;
	}

	public static class Action
	{
		public int guiNameID;
		public int slotClicked;
		public ItemStack stack;
		int mode;
		int button;

		public Action(int guiNameID, int slotClicked, ItemStack stack, int mode, int button)
		{
			this.guiNameID = guiNameID;
			this.slotClicked = slotClicked;
			this.stack = stack;
			this.mode = mode;
			this.button = button;
		}

		public void execute(GuiChest chest)
		{
			Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(chest.inventorySlots.windowId, this.slotClicked, this.button, this.mode, this.stack, (short) 0));
		}
	}

	@ElementBuilder.MainElement
	private final KeySetting key = new KeySetting()
		.name("Recraft")
		.description("Wiederholt den letzten /rezepte - Aufruf.")
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.pressCallback((k) -> {
			if(k) player.play();
		});

	/**
	 * @return the GuiChest's "name", as displayed to the player at the top of the GUI.
	 **/
	String getChestName(GuiChest c)
	{
		Class<?> clazz = GuiChest.class;
		IInventory inv = Reflection.get(c, "lowerChestInventory");
		return inv.getDisplayName().getUnformattedText();
	}



	void sendClosePacket()
	{
		TickScheduler.runAfterClientTicks(() -> {
			Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0DPacketCloseWindow());
			Minecraft.getMinecraft().thePlayer.closeScreenAndDropStack();
		}, 2);
	}
}
