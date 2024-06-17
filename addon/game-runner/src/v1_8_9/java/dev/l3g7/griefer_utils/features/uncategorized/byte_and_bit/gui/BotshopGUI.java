package dev.l3g7.griefer_utils.features.uncategorized.byte_and_bit.gui;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.ModTextField;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.uncategorized.byte_and_bit.data.BABBot;
import dev.l3g7.griefer_utils.features.uncategorized.byte_and_bit.data.BABItem;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiBigChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.DoubleStream;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

/**
 * Class implementing the BotShop-GUI for a specific {@link BABBot}
 * Currently very spaghet... May be cleaned up one day
 */
public class BotshopGUI extends GuiBigChest {
	private static final ResourceLocation SEARCH_TAB_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
	private static final ResourceLocation SCROLLBAR_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	protected final ModTextField searchField;
	private final int searchFieldWidth = 88;
	protected int entryCount = 0;
	protected int scrollStartRow = 0;
	private float currentScroll = 0;
	private boolean wasClicking = false;
	private boolean isScrolling = false;
	private int backspaceSpeed = 0;
	private int backspaceTimer = 0;
	private final String botname;

	public TreeSet<BABItem> itemsDisplayed;
	final List<BABItem> boughtItems;
	final List<BABItem> items;
	final DecimalFormat priceFormat = new DecimalFormat("###,###,###.#");
	List<Float> prices = new ArrayList<>();

	private float price() {
		return (float) prices.stream().flatMapToDouble(DoubleStream::of).sum();
	}

	private String priceStr() {
		return priceFormat.format(price()) + "$";
	}

	public BotshopGUI(BABBot bot) {
		super("", 7);
		this.botname = bot.getName();
		searchField = new ModTextField(0, mc().fontRendererObj, guiLeft + 170 - searchFieldWidth, guiTop + 6, searchFieldWidth - 7, mc().fontRendererObj.FONT_HEIGHT);
		searchField.setPlaceHolder("§oSuchen");
		searchField.setTextColor(0xFFFFFF);
		searchField.setMaxStringLength(Integer.MAX_VALUE);
		searchField.setEnableBackgroundDrawing(false);
		this.itemsDisplayed = new TreeSet<>(BABItem::compareTo);
		this.items = bot.items;
		this.boughtItems = Collections.synchronizedList(new ArrayList<>());
		this.searchField.setFocused(true);
		this.updatePage();
		this.open();
		EventRegisterer.register(this);
	}

	@Override
	public void onGuiClosed() {
		EventRegisterer.unregister(this);
	}

	@EventListener
	public void onTick(TickEvent.ClientTickEvent event) {
		if (backspaceSpeed != 0) {
			backspaceSpeed += 10;
			if (backspaceTimer > 50) {
				searchField.textboxKeyTyped('\0', Keyboard.KEY_BACK);
				backspaceTimer = 0;
			} else {
				backspaceTimer += backspaceSpeed;
			}
		}
	}

	private Iterable<BABItem> getAllEntries() {
		return items;
	}

	private void setBoughtItem(BABItem entry, int slot) {
		if (entry == null) {
			super.addItem(slot * 9, null, null);
			return;
		}
		super.addItem(slot * 9, entry.getStack(), () -> {
			for (BABItem i : items)
				if (i.getPrice() == entry.getPrice()) {
					boughtItems.remove(i);
				}

			for (int i = 0; i < prices.size(); i++) {
				if (prices.get(i) == entry.getPrice()) {
					prices.remove(i);
					break;
				}
			}
			updatePage();
		});
	}

	private void setMenuItem(BABItem entry, int slot) {
		int diff = (slot / 7 + 1) * (9 - 7); // 9 is the row length, 7 is the amount of slots filled in a row
		if (entry == null) {
			super.addItem(slot + diff, null, null);
			return;
		}

		super.addItem(slot + diff, entry.getStack(), () -> {
			List<BABItem> addedItems = new ArrayList<>(8);
			for (BABItem i : items)
				if (i.getPrice() == entry.getPrice())
					addedItems.add(i);

			if (boughtItems.size() + addedItems.size() > 7)
				return;
			prices.add(entry.getPrice());
			boughtItems.addAll(addedItems);
			updatePage();
		});
	}

	private void setMenuDisabled() {
		for (int i = 0; i < 7 * 7; i++) {
			int diff = (i / 7 + 1) * (9 - 7); // 9 is the row length, 7 is the amount of slots filled in a row
			addItem(i + diff, ItemUtil.createItem(Blocks.stained_glass_pane, 15, "§6§lEinkaufsliste voll!"), () -> {});
		}
	}

	private boolean matchesSearch(BABItem entry, String search) {
		search = search.toLowerCase();

		// Check display name
		if (entry.getStack().getDisplayName().replaceAll("§.", "").toLowerCase().contains(search))
			return true;

		// Check original name
		if (entry.getStack().getItem().getItemStackDisplayName(entry.getStack()).replaceAll("§.", "").toLowerCase().contains(search))
			return true;

		// Check i18n name
		if (entry.getStack().getUnlocalizedName().replaceAll("§.", "").toLowerCase().contains(search))
			return true;

		// Check lore
		for (String line : ItemUtil.getLore(entry.getStack()))
			if (line.replaceAll("§.", "").toLowerCase().contains(search.toLowerCase()))
				return true;

		return String.valueOf(entry.getPrice()).contains(search);
	}

	@Override
	public void open() {
		super.open();
		clearSearch();
	}

	public void clearSearch() {
		searchField.setText("");
		currentScroll = 0;
		entryCount = onSearch("");
	}

	protected void clearItems() {
		for (int i = 0; i < 25; i++)
			addItem(i, null, null);
	}

	private double bankBal() {
		double balance = 0;
		try {
			balance = Double.parseDouble(world().getScoreboard().getTeam("money_value").getColorPrefix().replaceAll("[$.]", "").replace(",", "."));
		} catch (NumberFormatException ignored) {}
		return balance;
	}

	protected void updatePage() {
		clearItems();

		Iterator<BABItem> boughtIterator = boughtItems.iterator();
		for (int i = 0; i < 7; i++) {
			if (!boughtIterator.hasNext()) {
				setBoughtItem(null, i);
				continue;
			}
			BABItem item = boughtIterator.next();
			setBoughtItem(item, i);
		}
		if (price() > bankBal()) {
			this.setGuiTitle("§4§l" + priceStr());
		} else {
			this.setGuiTitle("§0" + priceStr());
		}

		boolean full = boughtItems.size() >= 7;
		for (int i = 0; i < 7; i++) {
			addItem(9 * i + 1, ItemUtil.createItem(Blocks.stained_glass_pane, 15, "§f§l⬅ Einkaufsliste"), () -> {});
		}
		if (price() > bankBal()) {
			addTextureItem(28, new TextureItem("coin_pile_crossed_out", "§4§lGesperrt", "§fNicht genügend Guthaben"), null);
		} else if (!boughtItems.isEmpty()) {
			addTextureItem(28, new TextureItem("coin_pile", "§a§lKaufen (" + priceStr() + ")", "§fBestätige deinen Einkauf"), () -> {
				Minecraft.getMinecraft().thePlayer.closeScreen();
				if (botname == null) return;

				for (float price : prices) {
					ChatQueue.send("/pay " + botname + " " + price);
				}
			});
		}
		if (full) {
			setMenuDisabled();
			return;
		}

		onScroll(currentScroll); // NOTE cleanup
	}

	private int onSearch(String searchString) {
		itemsDisplayed.clear();
		clearItems();

		// Without filter
		if (searchString.isEmpty())
			itemsDisplayed.addAll(items);
		else for (BABItem entry : getAllEntries())
			if (matchesSearch(entry, searchString.toLowerCase()))
				itemsDisplayed.add(entry);

		updatePage();
		return this.itemsDisplayed.size();
	}

	private void onScroll(float scroll) {
		int i = (itemsDisplayed.size() + 7 - 1) / 7 - 7;
		scrollStartRow = Math.max((int) (scroll * i + 0.5), 0);

		List<BABItem> entriesAsList = new ArrayList<>(itemsDisplayed);

		for (int y = 0; y < 7; ++y) {
			for (int x = 0; x < 7; ++x) {
				int index = x + (y + scrollStartRow) * 7;
				int slotId = x + y * 7;

				if (index >= 0 && index < itemsDisplayed.size())
					setMenuItem(entriesAsList.get(index), slotId);
				else
					setMenuItem(null, slotId);
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		searchField.xPosition = guiLeft + 170 - searchFieldWidth;
		searchField.yPosition = guiTop + 6;
	}


	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (searchField.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey())) {
			clearItems();
			entryCount = onSearch(searchField.getText());
			return;
		}

		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.isKeyDown(Keyboard.KEY_BACK)) {
			backspaceSpeed = 1;
		} else {
			if (backspaceSpeed > 0) {
				backspaceTimer = 0;
				entryCount = onSearch(searchField.getText());
			}
			backspaceSpeed = 0;
		}
		super.handleKeyboardInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton != -1)
			searchField.mouseClicked(mouseX, mouseY, Mouse.getEventButton());
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		boolean isLeftClicking = Mouse.isButtonDown(0);
		int left = guiLeft + 175;
		int top = guiTop + 18;
		int right = left + 14;
		int bottom = top + 112;

		if (!wasClicking && isLeftClicking && mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom)
			isScrolling = entryCount > 25;

		if (!isLeftClicking)
			isScrolling = false;

		wasClicking = isLeftClicking;

		if (isScrolling) {
			currentScroll = ((mouseY - top) - 7.5f) / ((bottom - top) - 15f);
			currentScroll = MathHelper.clamp_float(currentScroll, 0, 1);
			onScroll(currentScroll);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		GlStateManager.color(1, 1, 1, 1);

		GlStateManager.translate(-guiLeft, -guiTop, 0);
		searchField.drawTextBox();
		GlStateManager.translate(guiLeft, guiTop, 0);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		GlStateManager.enableAlpha();
		mc().getTextureManager().bindTexture(SEARCH_TAB_TEXTURE);
		drawTexturedModalRect(guiLeft + 167 - searchFieldWidth, guiTop, 80, 0, searchFieldWidth + 1, 17);
		drawTexturedModalRect(guiLeft + 168, guiTop, 169, 0, 26, 17);
		drawTexturedModalRect(guiLeft + 169, guiTop + 17, 170, 17, 25, 18);
		drawTexturedModalRect(guiLeft + 169, guiTop + ySize - 25, 170, 111, 25, 25);

		for (int i = 0; i < rows - 2; i++)
			drawTexturedModalRect(guiLeft + 169, guiTop + 35 + i * 18, 170, 35, 25, 18);

		int sbStart = guiTop + 18;
		int sbEnd = guiTop + ySize - 6;
		mc.getTextureManager().bindTexture(SCROLLBAR_TEXTURE);
		drawTexturedModalRect(guiLeft + 174, sbStart + ((sbEnd - sbStart - 17) * currentScroll), entryCount > 25 ? 232 : 244, 0, 12, 15);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		int dWheel = Mouse.getEventDWheel();

		if (dWheel == 0 || entryCount <= 25)
			return;

		float invisibleRows = entryCount / 5f - 5;

		if (dWheel > 0)
			dWheel = 1;

		if (dWheel < 0)
			dWheel = -1;

		currentScroll = currentScroll - dWheel / invisibleRows;
		currentScroll = MathHelper.clamp_float(currentScroll, 0.0F, 1.0F);
		onScroll(currentScroll);
	}

	public void onEntryData() {
		onSearch(searchField.getText());
	}

}