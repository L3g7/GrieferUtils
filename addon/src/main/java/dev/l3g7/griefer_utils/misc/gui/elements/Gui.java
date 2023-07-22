package dev.l3g7.griefer_utils.misc.gui.elements;

import dev.l3g7.griefer_utils.misc.gui.elements.SelectButtonGroup.Selectable;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

@SuppressWarnings("UnusedReturnValue")
public class Gui extends GuiScreen {

	protected static final DrawUtils drawUtils = drawUtils();
	private final List<Clickable> clickables = new ArrayList<>();
	private final List<Drawable> drawables = new ArrayList<>();
	private final List<TextField> textFields = new ArrayList<>();

	private <T> T create(T obj) {
		if (obj instanceof Clickable)
			clickables.add((Clickable) obj);
		if (obj instanceof Drawable)
			drawables.add((Drawable) obj);
		if (obj instanceof TextField)
			textFields.add((TextField) obj);
		return obj;
	}

	public Button createButton(String label) {
		return create(new Button(label));
	}

	public Text createCenteredText(String text, double size) {
		return create(new Text(text, size, true));
	}

	public TextField createTextField(String label) {
		return create(new TextField(label));
	}

	public <E extends Enum<E> & Selectable> SelectButtonGroup<E> createSelectGroup(E placeholder, String label) {
		return create(new SelectButtonGroup<>(placeholder, label, width));
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		for (Clickable button : clickables)
			button.mousePressed(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		for (TextField textField : textFields)
			textField.textboxKeyTyped(typedChar, keyCode);
	}

	public void draw(int mouseX, int mouseY, int layer) {
		for (Drawable drawable : drawables)
			drawable.draw(mouseX, mouseY, layer);
	}

	public void draw(int mouseX, int mouseY) {
		draw(mouseX, mouseY, 0);
	}

	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		clickables.clear();
		drawables.clear();
		super.setWorldAndResolution(mc, width, height);
	}
}