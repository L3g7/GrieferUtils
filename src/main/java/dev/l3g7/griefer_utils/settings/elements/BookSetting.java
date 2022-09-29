package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.misc.GuiBook;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.List;

public class BookSetting extends SmallButtonSetting {

	private List<String> pages = new ArrayList<>();

	public BookSetting() {

		callback(() -> {
			ItemStack book = new ItemStack(Items.writable_book);
			NBTTagCompound tag = new NBTTagCompound();
			book.setTagCompound(tag);

			tag.setString("title", "A book");
			tag.setString("author", "Me");
			NBTTagList list = new NBTTagList();

			// Add existing pages
			if (pages.isEmpty())
				list.appendTag(new NBTTagString(""));
			else
				for (String page : pages)
					list.appendTag(new NBTTagString(page));

			tag.setTag("pages", list);

			// Display gui
			Minecraft.getMinecraft().displayGuiScreen(new GuiBook(book, true).addCloseCallback(bookPages -> {
				pages = bookPages;
				buttonIcon(new IconData(pages.isEmpty() ? Material.BOOK_AND_QUILL : Material.BOOK));
			}));
		});

		buttonIcon(new IconData(Material.BOOK_AND_QUILL));
	}

	public List<String> getPages() {
		return pages;
	}

	public void clearPages() {
		pages.clear();
	}

}
