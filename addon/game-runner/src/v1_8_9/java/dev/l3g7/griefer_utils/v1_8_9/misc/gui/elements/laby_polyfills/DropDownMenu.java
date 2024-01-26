/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills;

import dev.l3g7.griefer_utils.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.Scrollbar.EnumMouseAction;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Polyfill for LabyMod 3's DropDownMenu.
 * TODO: remove?
 */
public class DropDownMenu<T> extends Gui {
    private static final DropDownEntryDrawer defaultDrawer = new DropDownEntryDrawer() {
        public void draw(Object object, int x, int y, String trimmedEntry) {
            DrawUtils.drawString(trimmedEntry, (double)x, (double)y);
        }
    };
    private String title;
    private T selected = null;
    private boolean enabled = true;
    private boolean open;
    private T hoverSelected = null;
    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;
    private int maxY = Integer.MAX_VALUE;
    private ArrayList<T> list = new ArrayList();
    private DropDownEntryDrawer entryDrawer = null;
    private Scrollbar scrollbar;
    private Consumer<T> hoverCallback;

    public DropDownMenu(String title, int x, int y, int width, int height) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public DropDownMenu<T> fill(T[] values) {
        int var3 = values.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            T value = values[var4];
            this.list.add(value);
        }

        return this;
    }

    public void onScroll() {
        if (this.scrollbar != null) {
            this.scrollbar.mouseInput();
            this.scrollbar.setScrollY((double)((int)(this.scrollbar.getScrollY() / (double)this.scrollbar.getSpeed()) * this.scrollbar.getSpeed()));
        }

    }

    public void onDrag(int mouseX, int mouseY, int mouseButton) {
        if (this.scrollbar != null) {
            this.scrollbar.mouseAction(mouseX, mouseY, EnumMouseAction.DRAGGING);
        }

    }

    public void onRelease(int mouseX, int mouseY, int mouseButton) {
        if (this.scrollbar != null) {
            this.scrollbar.mouseAction(mouseX, mouseY, EnumMouseAction.RELEASED);
        }

    }

    public boolean onClick(int mouseX, int mouseY, int mouseButton) {
        if (this.scrollbar != null && this.scrollbar.isHoverTotalScrollbar(mouseX, mouseY)) {
            this.scrollbar.mouseAction(mouseX, mouseY, EnumMouseAction.CLICKED);
            return true;
        } else if (this.enabled && !this.list.isEmpty()) {
            if (mouseX > this.x - 1 && mouseX < this.x + this.width + 1 && mouseY > this.y - 1 && mouseY < this.y + this.height + 1) {
                this.open = !this.open;
                if (this.open && this.list.size() > 10) {
                    if (this.scrollbar == null) {
                        this.scrollbar = new Scrollbar(13);
                    }

                    this.scrollbar.setSpeed(13);
                    this.scrollbar.setListSize(this.list.size());
                    this.scrollbar.setPosition(this.x + this.width - 5, this.y + this.height + 1, this.x + this.width, this.maxY == Integer.MAX_VALUE ? this.y + this.height + 1 + 130 - 1 : this.maxY);
                }

                return true;
            } else if (this.hoverSelected != null) {
                this.selected = this.hoverSelected;
                this.open = false;
                return true;
            } else {
                this.open = false;
                if (!this.open && this.hoverCallback != null) {
                    this.hoverCallback.accept(null);
                }

                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX > this.x - 1 && mouseX < this.x + this.width + 1 && mouseY > this.y - 1 && mouseY < this.y + this.height + 1;
    }

    public void draw(int mouseX, int mouseY) {
        T prevHover = this.hoverSelected;
        this.hoverSelected = null;
        drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, Color.GRAY.getRGB());
        drawRect(this.x, this.y, this.x + this.width, this.y + this.height, Color.BLACK.getRGB());
        if (this.selected != null) {
            String trimmedEntry = DrawUtils.trimStringToWidth(("§f") + this.selected, this.width - 5);
            (this.entryDrawer == null ? defaultDrawer : this.entryDrawer).draw(this.selected, this.x + 5, this.y + this.height / 2 - 4, trimmedEntry);
        }

        if (this.enabled && !this.list.isEmpty()) {
            for(int i = 0; i <= 5; ++i) {
                drawRect(this.x + this.width - 16 + i, this.y + this.height / 2 - 2 + i, this.x + this.width - 5 - i, this.y + this.height / 2 + 1 - 2 + i, Color.LIGHT_GRAY.getRGB());
            }
        }

        if (this.title != null) {
            DrawUtils.drawString(DrawUtils.trimStringToWidth(this.title, this.width), (double)this.x, (double)(this.y - 13));
        }

        if (this.open) {
            this.drawMenuDirect(this.x, this.y, mouseX, mouseY);
        }

        if ((this.hoverSelected != null && prevHover == null || prevHover != null && !prevHover.equals(this.hoverSelected)) && this.hoverCallback != null) {
            this.hoverCallback.accept(this.hoverSelected);
        }

    }

    public void drawMenuDirect(int x, int y, int mouseX, int mouseY) {
        int entryHeight = 13;
        int maxPointY = y + this.height + 2 + 13 * this.list.size();
        boolean buildUp = maxPointY > this.maxY;
        int yPositionList = buildUp ? y - 13 - 1 : y + this.height + 1;
        if (this.scrollbar != null) {
            yPositionList = (int)((double)yPositionList + this.scrollbar.getScrollY());
        }

        for(Iterator<T> var9 = this.list.iterator(); var9.hasNext(); yPositionList += buildUp ? -entryHeight : entryHeight) {
            T option = var9.next();
            if (this.scrollbar == null || yPositionList > y + 8 && yPositionList + entryHeight < this.scrollbar.getPosBottom() + 2) {
                boolean hover = mouseX > x && mouseX < x + this.width && mouseY > yPositionList && mouseY < yPositionList + entryHeight;
                if (hover) {
                    this.hoverSelected = option;
                }

                drawRect(x - 1, yPositionList, x + this.width + 1, yPositionList + entryHeight, DrawUtils.toRGB(0, 30, 70, 250));
                drawRect(x, yPositionList + (buildUp ? 1 : 0), x + this.width, yPositionList + entryHeight - 1 + (buildUp ? 1 : 0), hover ? DrawUtils.toRGB(55, 55, 155, 215) : DrawUtils.toRGB(0, 10, 10, 250));
                String trimmedEntry = DrawUtils.trimStringToWidth(("§f") + option, this.width - 5);
                (this.entryDrawer == null ? defaultDrawer : this.entryDrawer).draw(option, x + 5, yPositionList + 3, trimmedEntry);
            }
        }

        if (this.scrollbar != null) {
            this.scrollbar.draw();
        }

    }

    public void clear() {
        this.open = false;
        this.selected = null;
        this.list.clear();
        this.setSelected(null);
    }

    public void remove(T type) {
        this.list.remove(type);
    }

    public void addOption(T option) {
        this.list.add(option);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public T getSelected() {
        return this.selected;
    }

    public void setSelected(T selected) {
        this.selected = selected;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isOpen() {
        return this.open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public T getHoverSelected() {
        return this.hoverSelected;
    }

    public void setHoverSelected(T hoverSelected) {
        this.hoverSelected = hoverSelected;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public DropDownEntryDrawer getEntryDrawer() {
        return this.entryDrawer;
    }

    public void setEntryDrawer(DropDownEntryDrawer entryDrawer) {
        this.entryDrawer = entryDrawer;
    }

    public void setHoverCallback(Consumer<T> hoverCallback) {
        this.hoverCallback = hoverCallback;
    }

    public interface DropDownEntryDrawer {
        void draw(Object var1, int var2, int var3, String var4);
    }
}
