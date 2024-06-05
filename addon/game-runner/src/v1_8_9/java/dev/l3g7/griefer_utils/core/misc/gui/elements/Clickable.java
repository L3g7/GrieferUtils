package dev.l3g7.griefer_utils.core.misc.gui.elements;

public interface Clickable {

	void mousePressed(int mouseX, int mouseY, int mouseButton);
	default void mouseRelease(int mouseX, int mouseY, int mouseButton) {}
	default void mouseClickMove(int mouseX, int mouseY, int mouseButton) {}

}