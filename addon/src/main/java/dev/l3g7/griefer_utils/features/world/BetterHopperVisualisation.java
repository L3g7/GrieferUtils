package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.labymod.utils.Material;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.display;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;

@Singleton
public class BetterHopperVisualisation extends Feature {

	private BlockPos hopper;
	private BlockPos target;
	private int borderSize;
	private long displayEnd = -1;

	private final BooleanSetting fillBoxes = new BooleanSetting()
		.name("Anzeigeboxen füllen")
		.description("Ob die Boxen der Anzeige gefüllt werden sollen.")
		.defaultValue(true)
		.icon(ItemUtil.createItem(Blocks.wool, 14, false));

	private final NumberSetting displayTime = new NumberSetting()
		.name("Anzeigedauer")
		.description("Wie lange wie optische Anzeige aktiv bleiben soll, in Sekunden.")
		.icon("hourglass")
		.defaultValue(10);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Bessere optische Trichter Anzeige")
		.description("Ersetzt die Partikel der optischen Trichter Anzeige durch Boxen / Linien.")
		.icon(Material.HOPPER)
		.subSettings(displayTime, fillBoxes);

	@EventListener
	public void onPacketSend(PacketEvent.PacketSendEvent event) {
		if (!(event.packet instanceof C0EPacketClickWindow) || !(mc().currentScreen instanceof GuiChest))
			return;

		C0EPacketClickWindow packet = (C0EPacketClickWindow) event.packet;
		if (packet.getSlotId() != 16 || packet.getMode() == 3)
			return;

		Container slots = ((GuiChest) mc().currentScreen).inventorySlots;
		borderSize = slots.getSlot(31).getStack().stackSize;
		hopper = getBlockPos(slots.getSlot(13).getStack());

		ItemStack targetStack = slots.getSlot(34).getStack();
		target = EnchantmentHelper.getEnchantments(targetStack).isEmpty() ? null : getBlockPos(targetStack);
		displayEnd = System.currentTimeMillis() + displayTime.get() * 1000;

		if (target == null && borderSize < 2)
			display(Constants.ADDON_PREFIX + "Es gibt nichts zum Anzeigen...");

		mc().displayGuiScreen(null);
		event.setCanceled(true);
	}

	private BlockPos getBlockPos(ItemStack stack) {
		String line = ItemUtil.getLore(stack).get(0);
		String blockPos = line.substring(line.indexOf("§e") + 2);
		String[] coords = blockPos.split(";");
		return new BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
	}

	@EventListener
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if (displayEnd < System.currentTimeMillis())
			return;

		GL11.glDisable(GL_DEPTH_TEST);

		if (borderSize > 1) {
			AxisAlignedBB collectionBox = new AxisAlignedBB(hopper.add(borderSize, borderSize / 2, borderSize), hopper.add(-borderSize, borderSize / -2, -borderSize));
			if (fillBoxes.get())
				RenderUtil.drawFilledBox(collectionBox, new Color(0x1A880088, true));
			RenderUtil.drawBoxOutlines(collectionBox, new Color(0x880088), 1.5f);
		}

		if (target != null) {
			AxisAlignedBB targetBox = new AxisAlignedBB(target, target.add(1, 1, 1));
			if (fillBoxes.get())
				RenderUtil.drawFilledBox(targetBox, new Color(0x1AFF0000, true));
			RenderUtil.drawBoxOutlines(targetBox, new Color(0xFF0000), 1.5f);
			RenderUtil.drawLine(hopper.getX() + 0.5f, hopper.getY() + 0.5f, hopper.getZ() + 0.5f, target.getX() + 0.5f, target.getY() + 0.5f, target.getZ() + 0.5f, new Color(0xFF0000), 1.5f);
		}

		GL11.glEnable(GL_DEPTH_TEST);
	}

}
