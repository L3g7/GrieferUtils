package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.GuiBook;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

@Singleton
public class BookReader extends Feature {

	private final BooleanSetting enabled = new BooleanSetting()
			.name("BookReader")
			.description("Erlaubt das Lesen von Büchern.")
			.icon(Material.BOOK)
			.defaultValue(true)
			.config("tweaks.book_reader.active");

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	public BookReader() {
		super(Category.TWEAK);
	}

	@SubscribeEvent
	public void onMouse(MouseEvent event) {
		if (!isActive()
				|| !isOnGrieferGames()
				|| event.button != 1
				|| !event.buttonstate
				|| player() == null
				|| mc().currentScreen != null)
			return;

		if (openIfBook(player().getHeldItem()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onMouseGui(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (!isActive()
				|| !isOnGrieferGames()
				|| !Mouse.getEventButtonState()
				|| !(mc().currentScreen instanceof GuiContainer))
			return;

		if (Mouse.getEventButton() != 1) {
			event.setCanceled(true);
			return;
		}

		Slot slot = ((GuiContainer) mc().currentScreen).getSlotUnderMouse();
		if (slot != null)
			if (openIfBook(slot.getStack()))
				event.setCanceled(true);
	}

	private boolean openIfBook(ItemStack item) {
		if (item == null || !item.hasTagCompound() || (item.getItem() != Items.writable_book && item.getItem() != Items.written_book))
			return false;

		// Try to remove the "* Invalid book tag *" by making sure it has all the required tags
		NBTTagCompound tag = item.getTagCompound();
		if (!tag.hasKey("title"))
			tag.setString("title", "A book");
		if (!tag.hasKey("author"))
			tag.setString("author", "Me");
		if (!tag.hasKey("pages"))
			tag.setTag("pages", new NBTTagList());

		mc().displayGuiScreen(new GuiBook(item, false));
		return true;
	}

}
