package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.gui.elements.ModTextField;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.DropDownElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// Has to be a DropDownElement, otherwise onClickDropDown won't be triggered
public class ItemSetting extends DropDownElement<ItemSetting.DummyEnum> implements SettingElementBuilder<ItemSetting> {

	private static final List<ItemStack> allItems = new ArrayList<>();

	private final DropDownMenu<ItemStack> menu = new DropDownMenu<>(null, 0, 0, 0, 0);
	private final ModTextField textField = new ModTextField(-2, LabyModCore.getMinecraft().getFontRenderer(), 50, 0, 116, 20);
	private final ArrayList<ItemStack> items = Reflection.get(menu, "list");
	private final List<Consumer<ItemStack>> callbacks = new ArrayList<>();
	private String configKey = null;
	private ItemStack currentValue = null;
	private ItemStack itemIcon;

	private boolean initialized = false;


	public ItemSetting() {
		super("§cNo name set", new DropDownMenu<DummyEnum>(null, 0, 0, 0, 0) {
			@Override
			public void draw(int mouseX, int mouseY) {}
		});

		icon("transparent");
		itemIcon = ItemUtil.MISSING_TEXTURE;
		textField.setEnableBackgroundDrawing(false);
		textField.setMaxStringLength(Integer.MAX_VALUE);
	}

	@Override
	public ItemSetting name(String name) {
		return SettingElementBuilder.super.name(name);
	}

	@Override
	public void init() {
		menu.setOpen(false);

		if (initialized)
			return;

		if (textField.getText().isEmpty())
			items.addAll(allItems);

		menu.setEntryDrawer((o, x, y, trimmedEntry) -> {
			boolean isSelected = (menu.getY() + menu.getHeight() / 2 - 4) == y;

			if (isSelected && menu.isOpen()) // Skip the selected entry
				return;

			DrawUtils draw = new DrawUtils();
			ItemStack stack = ((ItemStack) o);

			int maxWidth = menu.getWidth() - (items.size() > 10 ? 19 : 14);
			String displayedString = mc.fontRendererObj.trimStringToWidth(stack.getDisplayName(), maxWidth);
			draw.drawString(displayedString, x + 9, y + (isSelected ? 1 : 0));

			GlStateManager.pushMatrix();
			double scale = 10 / 16d;
			double inverseScale = 1 / scale;
			double scaledX = (x - 3) * inverseScale;
			double scaledY = (y - (isSelected ? 0.5 : 2)) * inverseScale;
			GlStateManager.scale(scale, scale, scale);
			draw.drawItem(stack, scaledX, scaledY, null);
			GlStateManager.popMatrix();
		});

		initialized = true;
	}

	public ItemStack get() {
		return menu.getSelected();
	}

	public ItemSetting set(ItemStack stack) {
		currentValue = stack;
		menu.setSelected(stack);
		textField.setText(stack.getDisplayName());
		textField.setCursorPositionEnd();
		filterItems();

		callbacks.forEach(c -> c.accept(stack));
		itemIcon = stack;

		if (configKey != null) {
			Config.set(configKey, stack.serializeNBT().toString());
			Config.save();
		}
		return this;
	}

	public ItemSetting defaultValue(ItemStack defaultValue) {
		if (currentValue == null) {
			set(defaultValue);
		}
		return this;
	}

	public ItemSetting config(String configKey) {
		this.configKey = configKey;

		if (!Config.has(configKey))
			return this;

		NBTTagCompound tag;
		try {
			tag = JsonToNBT.getTagFromJson(Config.get(configKey).getAsString());
		} catch (NBTException e) {
			throw new RuntimeException(e);
		}

		set(ItemStack.loadItemStackFromNBT(tag));

		return this;
	}

	public ItemSetting callback(Consumer<ItemStack> callback) {
		callbacks.add(callback);
		return this;
	}

	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		mouseOver = menu.isMouseOver(mouseX, mouseY);
		LabyMod.getInstance().getDrawUtils().drawItem(itemIcon, x + 5, y + 3, null);

		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		LabyMod.getInstance().getDrawUtils().drawRectangle(x - 1, y, x, maxY, ModColor.toRGB(120, 120, 120, 120));
		int width = 125;
		menu.setX(maxX - width - 5);
		menu.setY(y + 3);
		menu.setWidth(width);
		menu.setHeight(maxY - y - 6);
		menu.draw(mouseX, mouseY);

		if (!textField.isFocused())
			return;

		textField.xPosition = menu.getX() + 5;
		textField.yPosition = menu.getY() + 5;
		textField.width = width - 9;
		textField.drawTextBox();
	}

	public int getEntryHeight() {
		return iconData == null ? 35 : 23;
	}

	public int getObjectWidth() {
		return menu.getWidth() + 5;
	}

	public boolean onClickDropDown(int mouseX, int mouseY, int mouseButton) {
		if (menu.onClick(mouseX, mouseY, mouseButton)) {
			textField.setFocused(menu.isOpen());
			if (menu.getSelected() != currentValue)
				set(menu.getSelected());
			return true;
		}

		textField.setFocused(menu.isOpen());

		return false;
	}

	public void mouseRelease(int mouseX, int mouseY, int mouseButton) {
		super.mouseRelease(mouseX, mouseY, mouseButton);
		menu.onRelease(mouseX, mouseY, mouseButton);
	}

	public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
		super.mouseClickMove(mouseX, mouseY, mouseButton);
		menu.onDrag(mouseX, mouseY, mouseButton);
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.textField.mouseClicked(mouseX, mouseY, 0);
	}

	public void keyTyped(char typedChar, int keyCode) {
		if (textField.textboxKeyTyped(typedChar, keyCode))
			filterItems();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		textField.updateCursorCounter();
	}

	public void filterItems() {
		List<ItemStack> filteredItems = allItems.stream()
			.filter(i -> i.getDisplayName().toLowerCase().contains(textField.getText().toLowerCase()))
			.sorted(Comparator.comparing(ItemStack::getDisplayName))
			.collect(Collectors.toList());
		items.clear();
		items.addAll(filteredItems);

		Scrollbar scrollbar = null;

		if (items.size() > 10) {
			scrollbar = new Scrollbar(13);
			scrollbar.setSpeed(13);
			scrollbar.setListSize(items.size());
			scrollbar.setPosition(menu.getX() + menu.getWidth() - 5, menu.getY() + menu.getHeight() + 1, menu.getX() + menu.getWidth(), menu.getY() + menu.getHeight() + 1 + 130 - 1);
		}

		Reflection.set(menu, scrollbar, "scrollbar");
	}

	public void onScrollDropDown() {
		menu.onScroll();
	}

	@Override
	public ItemSetting setCallback(net.labymod.utils.Consumer<DummyEnum> consumer) {
		return this;
	}

	@Override
	public DropDownMenu<ItemStack> getDropDownMenu() {
		return menu;
	}

	@Override
	public void setChangeListener(net.labymod.utils.Consumer<DummyEnum> changeListener) {}

	public enum DummyEnum {}

	static {
		// Register all items
		for (Item item : Item.itemRegistry) {
			if (item == null
				|| item == Item.getItemFromBlock(Blocks.farmland) // Has no model
				|| item == Item.getItemFromBlock(Blocks.lit_furnace)) // Has no model
				continue;

			item.getSubItems(item, CreativeTabs.tabAllSearch, allItems);
		}

		allItems.add(new ItemStack(Items.potionitem));
		allItems.sort(Comparator.comparing(ItemStack::getDisplayName));
	}

}
