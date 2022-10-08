package dev.l3g7.griefer_utils.misc;

import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.input.Keyboard.*;

/**
 * Most of the code is copied from GuiBookScreen
 */
public class GuiBook extends GuiScreen {

	private static final int bookImageWidth = 192;
	private static final int bookImageHeight = 192;
	private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");

	private final GuiScreen previousScreen = Minecraft.getMinecraft().currentScreen;
	private final ItemStack bookObj;
	private final boolean bookCanBeEdited;
	private Consumer<List<String>> closeCallback = null;
	private int updateCount; // Passed ticks since the gui's creation
	private int bookTotalPages = 1;
	private int currPage;
	private NBTTagList bookPages;
	private List<IChatComponent> currentPageComponents;
	private int currentPage = -1;
	private NextPageButton buttonNextPage;
	private NextPageButton buttonPreviousPage;
	private Integer limit = null;

	public GuiBook(ItemStack book, boolean canBeEdited) {
		bookObj = book;
		bookCanBeEdited = canBeEdited;

		if (book.hasTagCompound()) {
			NBTTagCompound nbttagcompound = book.getTagCompound();
			bookPages = nbttagcompound.getTagList("pages", 8);

			if (bookPages != null) {
				bookPages = (NBTTagList) bookPages.copy();
				bookTotalPages = Math.min(bookPages.tagCount(), 1);
			}
		}

		if (bookPages == null && canBeEdited) {
			bookPages = new NBTTagList();
			bookPages.appendTag(new NBTTagString(""));
			bookTotalPages = 1;
		}
	}

	public GuiBook limit(Integer limit) {
		this.limit = limit;
		return this;
	}

	public GuiBook addCloseCallback(Consumer<List<String>> closeCallback) {
		this.closeCallback = closeCallback;
		return this;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {
		super.updateScreen();
		++updateCount;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui() {
		buttonList.clear();
		Keyboard.enableRepeatEvents(true);

		buttonList.add(new GuiButton(0, width / 2 + (bookCanBeEdited ? 2 : -100), 4 + bookImageHeight, bookCanBeEdited ? 98 : 200, 20, I18n.format("gui.done")));

		int middle = (width - bookImageWidth) / 2;
		buttonList.add(buttonNextPage = new NextPageButton(1, middle + 120, 156, true));
		buttonList.add(buttonPreviousPage = new NextPageButton(2, middle + 38, 156, false));
		updateButtons();
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);

		if (closeCallback != null)
			closeCallback.accept(getPagesAsList());
	}

	private List<String> getPagesAsList() {
		// Remove empty pages at the end
		NBTTagList bookPages = (NBTTagList) this.bookPages.copy();
		while (bookPages.tagCount() > 0) {

			int index = bookPages.tagCount() - 1;

			if (!bookPages.getStringTagAt(index).isEmpty())
				break;

			bookPages.removeTag(index);
		}

		List<String> pages = Lists.newArrayList();

		for (int i = 0; i < bookPages.tagCount(); i++)
			pages.add(bookPages.getStringTagAt(i));

		return pages;
	}

	private void updateButtons() {
		buttonNextPage.visible = currPage < bookTotalPages - 1 || bookCanBeEdited;
		buttonPreviousPage.visible = currPage > 0;
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
	 */
	protected void actionPerformed(GuiButton button) {
		if (!button.enabled)
			return;

		if (button.id == 0) {
			mc.displayGuiScreen(previousScreen);
		} else if (button.id == 1) {
			if (currPage < bookTotalPages - 1) {
				++currPage;
			} else if (bookCanBeEdited) {
				addNewPage();

				if (currPage < bookTotalPages - 1)
					++currPage;
			}
		} else if (button.id == 2 && currPage > 0) {
			--currPage;
		}

		updateButtons();
	}

	private void addNewPage() {
		if (bookPages != null && bookPages.tagCount() < 50) {
			bookPages.appendTag(new NBTTagString(""));
			++bookTotalPages;
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == 1)
			Minecraft.getMinecraft().displayGuiScreen(previousScreen);

		if (bookCanBeEdited)
			keyTypedInBook(typedChar, keyCode);
	}

	/**
	 * Processes keystrokes when editing the text of a book
	 */
	private void keyTypedInBook(char typedChar, int keyCode) {
		if (GuiScreen.isKeyComboCtrlV(keyCode)) {
			pageInsertIntoCurrent(GuiScreen.getClipboardString());
			return;
		}

		switch (keyCode) {
			case KEY_BACK:
				String s = pageGetCurrent();

				if (s.length() > 0)
					pageSetCurrent(s.substring(0, s.length() - 1));

				return;
			case KEY_RETURN:
			case KEY_NUMPADENTER:
				pageInsertIntoCurrent("\n");
				return;
			default:
				if (ChatAllowedCharacters.isAllowedCharacter(typedChar))
					pageInsertIntoCurrent(Character.toString(typedChar));
		}
	}

	/**
	 * Returns the entire text of the current page as determined by currPage
	 */
	private String pageGetCurrent() {
		return bookPages != null && currPage >= 0 && currPage < bookPages.tagCount() ? bookPages.getStringTagAt(currPage) : "";
	}

	/**
	 * Sets the text of the current page as determined by currPage
	 */
	private void pageSetCurrent(String newText) {
		if (bookPages == null || currPage < 0 || currPage >= bookPages.tagCount())
			return;

		String oldData = bookPages.getStringTagAt(currPage);
		bookPages.set(currPage, new NBTTagString(newText));

		// Reset if the limit was reached
		if (limit != null && String.join("\n\n", getPagesAsList()).length() > limit)
			bookPages.set(currPage, new NBTTagString(oldData));
	}

	/**
	 * Processes any text getting inserted into the current page, enforcing the page size limit
	 */
	private void pageInsertIntoCurrent(String addedText) {
		String newText = pageGetCurrent() + addedText;
		int width = fontRendererObj.splitStringWidth(newText + "" + EnumChatFormatting.BLACK + "_", 118);

		if (width <= 128 && newText.length() < 256)
			pageSetCurrent(newText);
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (mc.theWorld == null)
			drawBackground(0);

		GlStateManager.enableBlend();
		GlStateManager.color(1, 1, 1, 1);

		mc.getTextureManager().bindTexture(bookGuiTextures);
		int middle = (width - bookImageWidth) / 2;
		drawTexturedModalRect(middle, 2, 0, 0, bookImageWidth, bookImageHeight);

		String currentPageComponent = "";

		if (bookPages != null && currPage >= 0 && currPage < bookPages.tagCount())
			currentPageComponent = bookPages.getStringTagAt(currPage);

		if (bookCanBeEdited) {
			if (fontRendererObj.getBidiFlag())
				currentPageComponent += "_";
			else if (updateCount / 6 % 2 == 0)
				currentPageComponent += EnumChatFormatting.BLACK + "_";
			else
				currentPageComponent += EnumChatFormatting.GRAY + "_";

		} else if (currentPage != currPage) {
			if (ItemEditableBook.validBookTagContents(bookObj.getTagCompound())) {
				try {
					IChatComponent component = IChatComponent.Serializer.jsonToComponent(currentPageComponent);
					currentPageComponents = component == null ? null : GuiUtilRenderComponents.func_178908_a(component, 116, fontRendererObj, true, true);
				} catch (JsonParseException var13) {
					currentPageComponents = null;
				}
			} else {
				ChatComponentText chatcomponenttext = new ChatComponentText(EnumChatFormatting.DARK_RED + "* Invalid book tag *");
				currentPageComponents = Lists.newArrayList(chatcomponenttext);
			}

			currentPage = currPage;
		}

		String pageIndicator = I18n.format("book.pageIndicator", currPage + 1, bookTotalPages);
		fontRendererObj.drawString(pageIndicator, middle - fontRendererObj.getStringWidth(pageIndicator) + bookImageWidth - 44, 18, 0);

		if (currentPageComponents == null) {
			fontRendererObj.drawSplitString(currentPageComponent, middle + 36, 34, 116, 0);
		} else {
			int maxTextAmount = Math.min(128 / fontRendererObj.FONT_HEIGHT, currentPageComponents.size());
			for (int currentIndex = 0; currentIndex < maxTextAmount; ++currentIndex) {
				fontRendererObj.drawString(currentPageComponents.get(currentIndex).getUnformattedText(), middle + 36, 34 + currentIndex * fontRendererObj.FONT_HEIGHT, 0);
			}
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@SideOnly(Side.CLIENT)
	static class NextPageButton extends GuiButton {
		private final boolean rightButton;

		public NextPageButton(int buttonId, int x, int y, boolean rightButton) {
			super(buttonId, x, y, 23, 13, "");
			this.rightButton = rightButton;
		}

		/**
		 * Draws this button to the screen.
		 */
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			if (!visible)
				return;

			boolean flag = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
			GlStateManager.color(1, 1, 1, 1);
			mc.getTextureManager().bindTexture(bookGuiTextures);
			drawTexturedModalRect(xPosition, yPosition, flag ? 23 : 0, rightButton ? 192 : 205, 23, 13);
		}
	}

}