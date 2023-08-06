package dev.l3g7.griefer_utils.misc.gui.elements;

import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.misc.Citybuild;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.main.LabyMod;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DropDown<T> extends DropDownMenu<T> implements Drawable, Clickable {

	protected static final DrawUtils drawUtils = MinecraftUtil.drawUtils();
	private static final double LABEL_HEIGHT = 9 * 1.2;

	private final String label;

	private final int screenWidth;
	private int screenHeight = MinecraftUtil.screenHeight();
	private int y;
	private int renderGroup = 0, menuRenderGroup = 0;

	private DropDown(String label, int screenWidth) {
		super("", 0, 0, 0, 20);

		this.label = label;
		this.screenWidth = screenWidth;
	}

	static <T extends Enum<T>> DropDown<T> fromEnum(T placeholder, String label, int screenWidth) {
		DropDown<T> dropDown = new DropDown<>(label, screenWidth);

		dropDown.setSelected(placeholder);
		dropDown.setEntryDrawer((e, x, y, trimmed) -> drawUtils.drawString(Reflection.get(e, "name"), x, y));

		for (T value : placeholder.getDeclaringClass().getEnumConstants())
			dropDown.addOption(value);

		if (placeholder instanceof ItemEnum) {
			dropDown.setEntryDrawer((o, x, y, trimmedEntry) -> {
				boolean isSelected = (dropDown.getY() + dropDown.getHeight() / 2 - 4) == y;

				drawUtils.drawString(((Citybuild) o).getDisplayName(), x + 9, y);
				dropDown.drawItemScaled(x, y, isSelected, ((ItemEnum) o).getItem());
			});
		}
		return dropDown;
	}

	static <T extends ItemStack> DropDown<T> fromItemStack(T placeholder, List<T> options, String label, int screenWidth) {
		DropDown<T> dropDown = new DropDown<>(label, screenWidth);

		dropDown.setSelected(placeholder);
		dropDown.setEntryDrawer((e, x, y, trimmed) -> drawUtils.drawString(Reflection.get(e, "name"), x, y));
		for (T option : options)
			dropDown.addOption(option);

		dropDown.setEntryDrawer((o, x, y, trimmedEntry) -> {
			ItemStack stack = (ItemStack) o;
			boolean isSelected = (dropDown.getY() + dropDown.getHeight() / 2 - 4) == y;

			drawUtils.drawString(stack.getDisplayName(), x + 9, y);
			dropDown.drawItemScaled(x, y, isSelected, stack);
		});

		return dropDown;
	}

	public DropDown<T> width(double guiWidth) {
		setX((int) ((screenWidth - guiWidth) / 2) + 1); // rendering of menu is offset by 1px
		setWidth((int) guiWidth);
		return this;
	}

	public DropDown<T> y(double y) {
		this.y = (int) y;
		setY((int) (y + LABEL_HEIGHT + 5));
		return this;
	}

	public DropDown<T> renderGroup(int renderGroup) {
		this.renderGroup = renderGroup;
		return this;
	}

	public DropDown<T> menuRenderGroup(int menuRenderGroup) {
		this.menuRenderGroup = menuRenderGroup;
		return this;
	}

	public double bottom() {
		return y + LABEL_HEIGHT + 6 + getHeight();
	}

	public void setScreenHeight(double height) {
		this.screenHeight = (int) height;
	}

	private void drawItemScaled(int x, int y, boolean isSelected, ItemStack stack) {
		GlStateManager.pushMatrix();

		double scale = 10 / 16d;
		double inverseScale = 1 / scale;
		double scaledX = (x - 3) * inverseScale;
		double scaledY = (y - (isSelected ? 1 : 2)) * inverseScale;
		GlStateManager.scale(scale, scale, scale);

		drawUtils.drawItem(stack, scaledX, scaledY, null);

		GlStateManager.popMatrix();
	}

	@Override
	public void draw(int mouseX, int mouseY, int renderGroup) {
		if (this.renderGroup != renderGroup) {
			if (menuRenderGroup == renderGroup)
				drawMenu(mouseX, mouseY);
			return;
		}

		drawUtils.drawString(label, getX(), y, 1.2);

		boolean open = this.isOpen();
		setOpen(false);
		super.draw(mouseX, mouseY);
		setOpen(open);

		if (menuRenderGroup == renderGroup)
			drawMenu(mouseX, mouseY);
	}

	public void drawMenu(int mouseX, int mouseY) {
		if (!isOpen())
			return;

		// Extract data
		int height = getHeight();
		ArrayList<T> list = Reflection.get(this, "list");
		int x = getX();
		int y = getY();
		int width = getWidth();
		Scrollbar scrollbar = Reflection.get(this, "scrollbar");
		DropDownEntryDrawer defaultDrawer = Reflection.get(this, "defaultDrawer");

		// Update scrollbar
		int entryHeight = 13;
		int entryY = y + height + 1;
		if (scrollbar != null) {
			entryY = (int) (entryY + scrollbar.getScrollY());

			int begin = y + height + 15;
			int maxEntries = (screenHeight - begin) / entryHeight;

			scrollbar.setPosBottom(scrollbar.getPosTop() + maxEntries * entryHeight);
			scrollbar.calc();
		}

		// Draw entries
		for (T option : list) {
			if (scrollbar == null || entryY > y + 8 && entryY + entryHeight < scrollbar.getPosBottom() + 2) {
				boolean hover = mouseX > x && mouseX < x + width && mouseY > entryY && mouseY <= entryY + entryHeight;
				if (hover)
					setHoverSelected(option);

				// Draw background
				drawRect(x - 1, entryY, x + width + 1, entryY + entryHeight, 0xFA001E46);
				drawRect(x, entryY, x + width, entryY + entryHeight - 1, hover ? 0xD737379B : 0xFA000A0A);

				// Draw entry
				String trimmedEntry = LabyMod.getInstance().getDrawUtils().trimStringToWidth(ModColor.cl("f") + option, width - 5);
				(getEntryDrawer() == null ? defaultDrawer : getEntryDrawer()).draw(option, x + 5, entryY + 3, trimmedEntry);
			}
			entryY += entryHeight;
		}

		// Draw scrollbar
		if (scrollbar != null)
			scrollbar.draw();
	}

	@Override
	public void mousePressed(int mouseX, int mouseY, int mouseButton) {
		super.onClick(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseRelease(int mouseX, int mouseY, int mouseButton) {
		super.onRelease(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
		super.onDrag(mouseX, mouseY, mouseButton);
	}

	public interface ItemEnum {

		ItemStack getItem();

	}
}