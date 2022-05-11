package dev.l3g7.griefer_utils.settings.elements;

import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.item.ItemStack;

public class CategorySetting extends ControlElement implements SettingElementBuilder<CategorySetting> {

    public CategorySetting() {
        super("§cNo name set", null);
        setSettingEnabled(true);
        setHoverable(true);
    }


    @Override
    public int getObjectWidth() {
        return 0;
    }

    private ItemStack itemIcon = null;

    @Override
    public void itemIcon(ItemStack item) {
        itemIcon = item;
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        if(itemIcon != null) {
            LabyMod.getInstance().getDrawUtils().drawItem(itemIcon, x + 3, y + 2, null);
        }
        super.draw(x, y, maxX, maxY, mouseX, mouseY);
    }
}
