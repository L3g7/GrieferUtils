package dev.l3g7.griefer_utils.features.item.recraft;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import java.util.Objects;
import java.util.UUID;

/**
 * Purpose of this class is to automate the tedious gui-navigation of GrieferGames'
 * /rezepte. The functionality currently implemented allows the player to quickly recraft the last
 * crafted item using the server's crafting command.
 * @author Pleezon
 */
@Singleton
public class Recraft extends Feature {

	private static final String[] MENU_NAMES = new String[]{
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

	public static int getMenuID(GuiChest c) {
		IInventory inv = Reflection.get(c, "lowerChestInventory");
		String name = inv.getDisplayName().getUnformattedText();

		for (int i = 0; i < MENU_NAMES.length; i++)
			if (Objects.equals(MENU_NAMES[i], name))
				return i;

		return -1;
	}

	@MainElement
	private final KeySetting key = new KeySetting() {

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			GlStateManager.color(1, 1, 1, 1);
			LabyMod.getInstance().getDrawUtils().drawPlayerHead(UUID.fromString("7bfe775c-b12c-4282-8bf4-1b1d67101c1e"), x + 12, y + 12, 8);
		}

	}
		.name("Recraft")
		.description("Wiederholt den letzten \"/rezepte\" Aufruf.\n\n§oErstellt von Pleezon")
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.pressCallback((k) -> {
			if(k)
				RecraftPlayer.play();
		});

	static class Action {

		public final int guiNameID;
		public final int slotClicked;
		public final ItemStack stack;
		int mode;
		int button;

		public Action(int guiNameID, int slotClicked, ItemStack stack, int mode, int button) {
			this.guiNameID = guiNameID;
			this.slotClicked = slotClicked;
			this.stack = stack;
			this.mode = mode;
			this.button = button;
		}

		public void execute(GuiChest chest) {
			Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(chest.inventorySlots.windowId, this.slotClicked, this.button, this.mode, this.stack, (short) 0));
		}

	}

}
