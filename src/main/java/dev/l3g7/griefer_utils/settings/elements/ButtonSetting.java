package dev.l3g7.griefer_utils.settings.elements;

import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.gui.GuiButton;

import java.util.ArrayList;
import java.util.List;

public class ButtonSetting extends ControlElement implements SettingElementBuilder<ButtonSetting> {

    private final GuiButton guiButton = new GuiButton(-2, 0, 0, 0, 20, "");
    private final List<Runnable> callbacks = new ArrayList<>();

    public ButtonSetting() {
        super("§cNo name set", null);
        setSettingEnabled(false);
    }

    @Override
    public int getObjectWidth() {
        return 0;
    }

    public ButtonSetting callback(Runnable callback) {
        callbacks.add(callback);
        return this;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (guiButton.mousePressed(mc, mouseX, mouseY)) {
            callbacks.forEach(Runnable::run);
            guiButton.playPressSound(mc.getSoundHandler());
        }
    }

    @Override
    public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
        mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

        guiButton.enabled = true;
        LabyModCore.getMinecraft().setButtonXPosition(guiButton, x + 1);
        LabyModCore.getMinecraft().setButtonYPosition(guiButton, y + 1);
        guiButton.setWidth(maxX - x - 2);

        LabyModCore.getMinecraft().drawButton(guiButton, mouseX, mouseY);
        LabyMod.getInstance().getDrawUtils().drawCenteredString(displayName, LabyModCore.getMinecraft().getXPosition(guiButton) + guiButton.getButtonWidth() / 2f, LabyModCore.getMinecraft().getYPosition(guiButton) + 6);
    }
}
