package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.HeaderElement;

public class HeaderSetting extends HeaderElement implements SettingElementBuilder<HeaderSetting> {

    private int entryHeight = super.getEntryHeight();

    public HeaderSetting() {
        super("§c");
    }

    public HeaderSetting(String name) {
        super(name);
    }

    public HeaderSetting scale(double scale) {
        Reflection.set(this, scale, "textSize");
        return this;
    }

    public HeaderSetting entryHeight(int entryHeight) {
        this.entryHeight = entryHeight;
        return this;
    }

    @Override
    public int getEntryHeight() {
        return entryHeight;
    }

}
