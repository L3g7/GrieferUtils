/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;

/**
 * Polyfill for LabyMod 3's scrollbar
 * TODO: remove?
 */
public class Scrollbar {
    private int listSize;
    private double entryHeight;
    private double scrollY;
    private double barLength;
    private double backLength;
    private int posTop;
    private int posBottom;
    private double left;
    private double top;
    private double right;
    private int speed = 10;
    private double clickY;
    private boolean hold;
    private boolean requestBottom;
    private int spaceBelow = 0;

    public void reset() {
        this.scrollY = 0.0;
    }

    public void init() {
        this.mouseInput();
    }

    public Scrollbar(int entryHeight) {
        this.entryHeight = (double)entryHeight;
        this.setDefaultPosition();
    }

    public void update(int listSize) {
        if (this.listSize != listSize) {
            this.listSize = listSize;
            if (this.requestBottom) {
                this.scrollY = -2.147483648E9;
                this.requestBottom = false;
                this.checkOutOfBorders();
            }

        }
    }

    public void setPosition(int left, int top, int right, int bottom) {
        this.left = (double)left;
        this.posTop = top;
        this.right = (double)right;
        this.posBottom = bottom;
        this.calc();
    }

    public void calc() {
        double totalPixels = (double)this.listSize * this.entryHeight + (double)this.spaceBelow;
        double backLength = (double)(this.posBottom - this.posTop);
        if (!(backLength >= totalPixels)) {
            double scale = backLength / totalPixels;
            double barLength = scale * backLength;
            double scroll = this.scrollY / scale * scale * scale;
            this.top = -scroll + (double)this.posTop;
            this.barLength = barLength;
            this.backLength = backLength;
        }
    }

    public void setDefaultPosition() {
        this.setPosition(DrawUtils.getWidth() / 2 + 150, 40, DrawUtils.getWidth() / 2 + 156, DrawUtils.getHeight() - 40);
    }

    public boolean isHidden() {
        if (this.listSize == 0) {
            return true;
        } else {
            return (double)(this.posBottom - this.posTop) >= (double)this.listSize * this.entryHeight + (double)this.spaceBelow;
        }
    }

    public void draw(int mouseX, int mouseY) {
        this.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.DRAGGING);
        this.draw();
    }

    public void draw() {
        this.checkOutOfBorders();
        if (!this.isHidden()) {
            this.calc();
            Tessellator tessellator = Tessellator.getInstance();
	        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableTexture2D();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos(this.left, (double)this.posBottom, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(this.right, (double)this.posBottom, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(this.right, (double)this.posTop, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos(this.left, (double)this.posTop, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos(this.left, this.top + this.barLength, 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
            worldrenderer.pos(this.right, this.top + this.barLength, 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
            worldrenderer.pos(this.right, this.top, 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
            worldrenderer.pos(this.left, this.top, 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos(this.left, this.top + this.barLength - 1.0, 0.0).tex(0.0, 1.0).color(192, 192, 192, 255).endVertex();
            worldrenderer.pos(this.right - 1.0, this.top + this.barLength - 1.0, 0.0).tex(1.0, 1.0).color(192, 192, 192, 255).endVertex();
            worldrenderer.pos(this.right - 1.0, this.top, 0.0).tex(1.0, 0.0).color(192, 192, 192, 255).endVertex();
            worldrenderer.pos(this.left, this.top, 0.0).tex(0.0, 0.0).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }

    public boolean isHoverSlider(int mouseX, int mouseY) {
        return (double)mouseX < this.right && (double)mouseX > this.left && (double)mouseY > this.top && (double)mouseY < this.top + this.barLength;
    }

    public boolean isHoverTotalScrollbar(int mouseX, int mouseY) {
        return (double)mouseX < this.right && (double)mouseX > this.left && mouseY > this.posTop && mouseY < this.posBottom;
    }

    public void mouseAction(int mouseX, int mouseY, EnumMouseAction mouseAction) {
        this.calc();
        double scale = this.backLength / ((double)this.listSize * this.entryHeight + (double)this.spaceBelow);
        double value = (double)((int)((double)(-mouseY) / scale));
        switch (mouseAction) {
            case CLICKED:
                if (this.hold) {
                    this.hold = false;
                } else if (this.isHoverSlider(mouseX, mouseY)) {
                    this.hold = true;
                    this.clickY = value - this.scrollY;
                }
                break;
            case DRAGGING:
                if (this.hold) {
                    this.scrollY = value - this.clickY;
                }
                break;
            case RELEASED:
                this.hold = false;
        }

        this.checkOutOfBorders();
    }

    public void mouseInput() {
        int wheel = Mouse.getEventDWheel();
        if (wheel > 0) {
            this.scrollY += (double)this.speed;
        } else if (wheel < 0) {
            this.scrollY -= (double)this.speed;
        }

        if (wheel != 0) {
            this.checkOutOfBorders();
        }

    }

    public void checkOutOfBorders() {
        if ((double)this.listSize * this.entryHeight + (double)this.spaceBelow + this.scrollY < (double)(this.posBottom - this.posTop)) {
            this.scrollY += (double)(this.posBottom - this.posTop) - ((double)this.listSize * this.entryHeight + (double)this.spaceBelow + this.scrollY);
        }

        if (this.scrollY > 0.0) {
            this.scrollY = 0.0;
        }

    }

    public void setPosition(double left, double top, double right, double bottom) {
        this.setPosition((int)left, (int)top, (int)right, (int)bottom);
    }

    public void requestBottom() {
        this.requestBottom = true;
    }

    public void scrollTo(int index) {
        this.scrollY += (double)(this.posBottom - this.posTop) - ((double)index * this.entryHeight + (double)this.spaceBelow + this.scrollY) - (this.entryHeight + (double)this.spaceBelow);
        this.checkOutOfBorders();
    }

    public int getListSize() {
        return this.listSize;
    }

    public double getEntryHeight() {
        return this.entryHeight;
    }

    public double getScrollY() {
        return this.scrollY;
    }

    public double getBarLength() {
        return this.barLength;
    }

    public double getBackLength() {
        return this.backLength;
    }

    public int getPosTop() {
        return this.posTop;
    }

    public int getPosBottom() {
        return this.posBottom;
    }

    public double getLeft() {
        return this.left;
    }

    public double getTop() {
        return this.top;
    }

    public double getRight() {
        return this.right;
    }

    public int getSpeed() {
        return this.speed;
    }

    public double getClickY() {
        return this.clickY;
    }

    public boolean isHold() {
        return this.hold;
    }

    public boolean isRequestBottom() {
        return this.requestBottom;
    }

    public int getSpaceBelow() {
        return this.spaceBelow;
    }

    public void setListSize(int listSize) {
        this.listSize = listSize;
    }

    public void setEntryHeight(double entryHeight) {
        this.entryHeight = entryHeight;
    }

    public void setScrollY(double scrollY) {
        this.scrollY = scrollY;
    }

    public void setBarLength(double barLength) {
        this.barLength = barLength;
    }

    public void setBackLength(double backLength) {
        this.backLength = backLength;
    }

    public void setPosTop(int posTop) {
        this.posTop = posTop;
    }

    public void setPosBottom(int posBottom) {
        this.posBottom = posBottom;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public void setRight(double right) {
        this.right = right;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setClickY(double clickY) {
        this.clickY = clickY;
    }

    public void setHold(boolean hold) {
        this.hold = hold;
    }

    public void setRequestBottom(boolean requestBottom) {
        this.requestBottom = requestBottom;
    }

    public void setSpaceBelow(int spaceBelow) {
        this.spaceBelow = spaceBelow;
    }

    public static enum EnumMouseAction {
        CLICKED,
        RELEASED,
        DRAGGING;

        private EnumMouseAction() {
        }
    }
}
