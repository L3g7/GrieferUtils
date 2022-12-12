/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_menu;

import dev.l3g7.griefer_utils.settings.ElementBuilder;
import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.gui.GuiButton;

import java.util.ArrayList;
import java.util.List;

public class ButtonSetting extends ControlElement implements ElementBuilder<ButtonSetting> {

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