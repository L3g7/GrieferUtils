package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
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
			.config("features.book_reader.active");

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	public BookReader() {
		super(Category.FEATURE);
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
				|| Mouse.getEventButton() != 1
				|| !(mc().currentScreen instanceof GuiContainer))
			return;

		Slot slot = ((GuiContainer) mc().currentScreen).getSlotUnderMouse();
		if (slot != null)
			if (openIfBook(slot.getStack()))
				event.setCanceled(true);
	}

	private boolean openIfBook(ItemStack item) {
		if (item == null || (item.getItem() != Items.writable_book && item.getItem() != Items.written_book))
			return false;

		// Try to remove the "* Invalid book tag *" by making sure it has all the required tags
		NBTTagCompound tag = item.getTagCompound();
		if (!tag.hasKey("title"))
			tag.setString("title", "A book");
		if (!tag.hasKey("author"))
			tag.setString("author", "Me");
		if (!tag.hasKey("pages"))
			tag.setTag("pages", new NBTTagList());

		mc().displayGuiScreen(new GuiBook(player(), item));
		return true;
	}

	// Required because GuiScreenBook escapes to the main game
	private static class GuiBook extends GuiScreenBook {
		private final GuiScreen previousScreen = Minecraft.getMinecraft().currentScreen;

		public GuiBook(EntityPlayer player, ItemStack book) {
			super(player, book, false);
		}

		protected void keyTyped(char typedChar, int keyCode) {
			if (keyCode == 1)
				mc.displayGuiScreen(previousScreen);
		}

		protected void actionPerformed(GuiButton button) {
			if (!button.enabled)
				return;

			int currPage_ = Reflection.get(this, "currPage", "field_146484_x", "x");
			int bookTotalPages = Reflection.get(this, "bookTotalPages", "field_146483_y", "y");

			if (button.id == 0)
				this.mc.displayGuiScreen(previousScreen);

			else if (button.id == 1 && currPage_ < bookTotalPages - 1)
				Reflection.set(this, ++currPage_, "currPage", "field_146484_x", "x");

			else if (button.id == 2 && currPage_ > 0)
				Reflection.set(this, --currPage_, "currPage", "field_146484_x", "x");

			Reflection.invoke(this, "updateButtons", "func_146464_h", "f");
		}
	}
}
