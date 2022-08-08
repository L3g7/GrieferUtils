package dev.l3g7.griefer_utils.features.tweaks.webhooks;

import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.ingamechat.tools.filter.FilterChatManager;
import net.labymod.ingamechat.tools.filter.Filters.Filter;
import net.labymod.main.LabyMod;
import net.labymod.main.lang.LanguageManager;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Copied and modified from net.minecraft.client.gui.inventory.GuiEditSign
 * TODO: beautify (ASM?)
 */
@SuppressWarnings("unchecked")
public class CustomGuiChatFilter extends GuiChatCustom {

    private static final Pattern HOOK_URL_PATTERN = Pattern.compile("^https://(?:\\w+\\.)?discord\\.com/api/webhooks/(\\d{18}\\d?/[\\w-]{68})$");

    private final Scrollbar scrollbar = new Scrollbar(15);
    private Filter selectedFilter;
    private GuiTextField textFieldFilterName;
    private GuiTextField textFieldFilterContains;
    private GuiTextField textFieldFilterContainsNot;
    private GuiTextField textFieldFilterSoundfile;
    private GuiTextField textFieldFilterRoom;
    private GuiTextField textFieldFilterWebhook;
    private int sliderDrag = -1;
    private boolean markFilterNameRed = false;
    private boolean markContainsRed = false;
    private boolean markSoundNameRed = false;
    private boolean markWebhookRed = false;
    private String editStartName = "";
    private boolean canScroll;
    private static final List<String> soundNames = new ArrayList<>();

    public CustomGuiChatFilter(String defaultText) {
        super(defaultText);
    }

    @Override
    public void initGui() {
        super.initGui();
        scrollbar.setPosition(width - 6, height - 196, width - 5, height - 20);
        scrollbar.update(LabyMod.getInstance().getChatToolManager().getFilters().size());
        scrollbar.setSpeed(10);
        scrollbar.setEntryHeight(10.0);
        textFieldFilterName = new GuiTextField(0, LabyModCore.getMinecraft().getFontRenderer(), 0, 0, 110, 10);
        textFieldFilterContains = new GuiTextField(0, LabyModCore.getMinecraft().getFontRenderer(), 0, 0, 110, 10);
        textFieldFilterContainsNot = new GuiTextField(0, LabyModCore.getMinecraft().getFontRenderer(), 0, 0, 110, 10);
        textFieldFilterSoundfile = new GuiTextField(0, LabyModCore.getMinecraft().getFontRenderer(), 0, 0, 110, 10);
        textFieldFilterRoom = new GuiTextField(0, LabyModCore.getMinecraft().getFontRenderer(), 0, 0, 110, 10);
        textFieldFilterWebhook = new GuiTextField(0, LabyModCore.getMinecraft().getFontRenderer(), 0, 0, 110, 10);
        textFieldFilterWebhook.setMaxStringLength(999);
        markContainsRed = false;
        markFilterNameRed = false;
        markSoundNameRed = false;
        markWebhookRed = false;
        selectedFilter = null;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (canScroll) {
            scrollbar.mouseInput();
            int i = Mouse.getEventDWheel();
            if (i != 0) {
                mc.ingameGUI.getChatGUI().resetScroll();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        scrollbar.calc();
        CustomGuiChatFilter.drawRect(width - 150, height - 235, width - 2, height - 16, Integer.MIN_VALUE);
        canScroll = mouseX > width - 150 && mouseX < width - 2 && mouseY > height - 150 && mouseY < height - 16;
        int row = 0;
        for (Filter component : LabyMod.getInstance().getChatToolManager().getFilters()) {
            double posY = (double) (height - 230 + row * 10) + scrollbar.getScrollY();
            ++row;
            if (posY < (double) (height - 235) || posY > (double) (height - 25))
                continue;
            boolean hover = selectedFilter == null && mouseX > width - 150 + 1 && mouseX < width - 2 - 1 && (double) mouseY > posY - 1.0 && (double) mouseY < posY + 9.0;
            if (hover || selectedFilter != null && (selectedFilter.getFilterName().equalsIgnoreCase(component.getFilterName()) || component.getFilterName().equalsIgnoreCase(editStartName))) {
                CustomGuiChatFilter.drawRect(width - 150 + 1, (int) posY - 1, width - 2 - 1, (int) posY + 9, hover ? ModColor.toRGB(100, 200, 200, 100) : Integer.MAX_VALUE);
            }
            drawString(LabyModCore.getMinecraft().getFontRenderer(), LabyMod.getInstance().getDrawUtils().trimStringToWidth(component.getFilterName(), 130), width - 145, (int) posY, Integer.MAX_VALUE);
            if (selectedFilter != null)
                continue;
            boolean hoverX = mouseX > width - 12 - 1 && mouseX < width - 12 + 7 && (double) mouseY > posY && (double) mouseY < posY + 8.0;
            drawString(LabyModCore.getMinecraft().getFontRenderer(), ModColor.cl(hoverX ? "c" : "4") + "✘", width - 12, (int) posY, Integer.MAX_VALUE);
        }
        if (!scrollbar.isHidden()) {
            CustomGuiChatFilter.drawRect(width - 6, height - 145, width - 5, height - 20, Integer.MIN_VALUE);
            CustomGuiChatFilter.drawRect(width - 7, (int) scrollbar.getTop(), width - 4, (int) (scrollbar.getTop() + scrollbar.getBarLength()), Integer.MAX_VALUE);
        }
        if (selectedFilter == null) {
            boolean hover = mouseX > width - 165 && mouseX < width - 152 && mouseY > height - 235 && mouseY < height - 222;
            CustomGuiChatFilter.drawRect(width - 165, height - 235, width - 152, height - 222, hover ? Integer.MAX_VALUE : Integer.MIN_VALUE);
            drawCenteredString(LabyModCore.getMinecraft().getFontRenderer(), "+", width - 158, height - 217 - 15, hover ? ModColor.toRGB(50, 220, 120, 210) : Integer.MAX_VALUE);
        } else {
            StringBuilder hint;
            int count;
            int relYAtSoundHint = 0;
            int relYAtRoomHint;
            CustomGuiChatFilter.drawRect(width - 270, height - 235, width - 152, height - 16, Integer.MIN_VALUE);
            int relX = width - 270;
            int relY = height - 235;
            drawElementTextField("name", textFieldFilterName, relX, relY);
            drawElementTextField("contains", textFieldFilterContains, relX, relY + 23);
            drawElementTextField("contains_not", textFieldFilterContainsNot, relX, relY + 46);
            drawElementTextField("room", textFieldFilterRoom, relX, relY + 69);
            drawElementTextField("DC Webhook:", textFieldFilterWebhook, relX, (relY += 23) + 69);
            relYAtRoomHint = relY + 69;
            drawElementCheckBox("play_sound", selectedFilter.isPlaySound(), relX, (relY += 23) + 69, mouseX, mouseY);
            if (selectedFilter.isPlaySound()) {
                drawElementTextField("", textFieldFilterSoundfile, relX, relY + 69);
                relYAtSoundHint = relY + 69;
            } else {
                relY -= 10;
            }
            drawElementCheckBox("highlight", selectedFilter.isHighlightMessage(), relX, relY + 92, mouseX, mouseY);
            if (selectedFilter.isHighlightMessage() && selectedFilter.getHighlightColor() != null) {
                drawElementSlider(selectedFilter.getHighlightColor().getRed(), relX, relY + 92 + 15, 0);
                drawElementSlider(selectedFilter.getHighlightColor().getGreen(), relX, relY + 92 + 15 + 9, 1);
                drawElementSlider(selectedFilter.getHighlightColor().getBlue(), relX, relY + 92 + 15 + 18, 2);
                CustomGuiChatFilter.drawRect(relX + 85, relY + 92 + 1, relX + 85 + 9, relY + 92 + 1 + 9, selectedFilter.getHighlightColor().getRGB());
            } else {
                relY -= 26;
            }
            drawElementCheckBox("hide", selectedFilter.isHideMessage(), relX, relY + 115 + 15, mouseX, mouseY);
            drawElementCheckBox("second_chat", selectedFilter.isDisplayInSecondChat(), relX, relY + 115 + 15 + 12, mouseX, mouseY);
            drawElementCheckBox("tooltip", selectedFilter.isFilterTooltips(), relX, relY + 115 + 15 + 24, mouseX, mouseY);
            boolean hoverCancel = mouseX > width - 268 && mouseX < width - 213 && mouseY > height - 30 && mouseY < height - 18;
            boolean hoverSave = mouseX > width - 210 && mouseX < width - 154 && mouseY > height - 30 && mouseY < height - 18;
            CustomGuiChatFilter.drawRect(width - 268, height - 30, width - 213, height - 18, hoverCancel ? ModColor.toRGB(200, 100, 100, 200) : Integer.MAX_VALUE);
            CustomGuiChatFilter.drawRect(width - 210, height - 30, width - 154, height - 18, hoverSave ? ModColor.toRGB(100, 200, 100, 200) : Integer.MAX_VALUE);
            drawCenteredString(LabyModCore.getMinecraft().getFontRenderer(), LanguageManager.translate("button_cancel"), width - 262 + 22, height - 30 + 2, Integer.MAX_VALUE);
            drawCenteredString(LabyModCore.getMinecraft().getFontRenderer(), LanguageManager.translate("button_save"), width - 205 + 23, height - 30 + 2, Integer.MAX_VALUE);
            textFieldFilterName.drawTextBox();
            textFieldFilterContains.drawTextBox();
            textFieldFilterContainsNot.drawTextBox();
            textFieldFilterRoom.drawTextBox();
            textFieldFilterWebhook.drawTextBox();
            if (selectedFilter.isPlaySound()) {
                textFieldFilterSoundfile.drawTextBox();
            }
            if (textFieldFilterSoundfile.isFocused() && selectedFilter.isPlaySound() && textFieldFilterSoundfile != null && !textFieldFilterSoundfile.getText().isEmpty()) {
                String lowerCase;
                count = 0;
                hint = new StringBuilder();
                for (String path : soundNames) {
                    if (!path.startsWith(lowerCase = textFieldFilterSoundfile.getText().toLowerCase()) || path.equals(lowerCase))
                        continue;
                    hint.append(path).append("\n");
                    if (++count <= 5)
                        continue;
                    break;
                }
                if (count == 0) {
                    for (String path : soundNames) {
                        if (!path.contains(lowerCase = textFieldFilterSoundfile.getText().toLowerCase()) || path.equals(lowerCase))
                            continue;
                        hint.append(path).append("\n");
                        if (++count <= 5)
                            continue;
                        break;
                    }
                }
                if (count != 0) {
                    LabyMod.getInstance().getDrawUtils().drawHoveringText(relX, relYAtSoundHint + 40, hint.toString().split("\n"));
                }
            }
            if (textFieldFilterRoom.isFocused()) {
                count = 0;
                hint = new StringBuilder();
                if (textFieldFilterRoom.getText().isEmpty() || "Global".contains(textFieldFilterRoom.getText().toUpperCase())) {
                    ++count;
                    hint.append("Global");
                }
                for (Filter filterComponent : LabyMod.getInstance().getChatToolManager().getFilters()) {
                    if (filterComponent.getRoom() == null || !filterComponent.getRoom().toLowerCase().contains(textFieldFilterRoom.getText().toLowerCase()) || hint.toString().contains(filterComponent.getRoom()))
                        continue;
                    hint.append(filterComponent.getRoom()).append("\n");
                    ++count;
                }
                if (count != 0) {
                    LabyMod.getInstance().getDrawUtils().drawHoveringText(relX, relYAtRoomHint + 40, hint.toString().split("\n"));
                }
            }
        }
        drawString(LabyModCore.getMinecraft().getFontRenderer(), LanguageManager.translate("ingame_chat_tab_filter"), width - 150, height - 245, -1);
        if (sliderDrag != -1) {
            mouseClickMove(mouseX, mouseY, 0, 0L);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.CLICKED);
        if (selectedFilter == null && mouseX > width - 165 && mouseX < width - 152 && mouseY > height - 235 && mouseY < height - 222) {
            loadFilter(new Filter("", new String[0], new String[0], false, "note.harp", true, (short) 200, (short) 200, (short) 50, false, false, false, "Global"));
        }
        if (selectedFilter == null) {
            int row = 0;
            Filter todoDelete = null;
            for (Filter component : LabyMod.getInstance().getChatToolManager().getFilters()) {
                double posY = (double) (height - 230 + row * 10) + scrollbar.getScrollY();
                ++row;
                if (posY < (double) (height - 235) || posY > (double) (height - 25))
                    continue;
                if (mouseX > width - 12 - 1 && mouseX < width - 12 + 7 && (double) mouseY > posY && (double) mouseY < posY + 8.0) {
                    todoDelete = component;
                    continue;
                }
                if (mouseX <= width - 150 + 1 || mouseX >= width - 2 - 1 || !((double) mouseY > posY - 1.0) || !((double) mouseY < posY + 9.0))
                    continue;
                loadFilter(new Filter(component));
                editStartName = component.getFilterName();
            }
            if (todoDelete != null) {
                LabyMod.getInstance().getChatToolManager().getFilters().remove(todoDelete);
                FilterChatManager.removeFilterComponent(todoDelete);
                LabyMod.getInstance().getChatToolManager().saveTools();
            }
        } else {
            if (textFieldFilterRoom.getText().contains(" ") || selectedFilter.getRoom() == null || selectedFilter.getRoom().contains(" ")) {
                textFieldFilterRoom.setText(textFieldFilterRoom.getText().replaceAll(" ", ""));
                selectedFilter.setRoom(textFieldFilterRoom.getText());
            }
            if (textFieldFilterRoom.getText().isEmpty() || selectedFilter.getRoom() == null || selectedFilter.getRoom().isEmpty()) {
                textFieldFilterRoom.setText("Global");
                selectedFilter.setRoom(textFieldFilterRoom.getText());
            }
            int relX = width - 270;
            int relY = height - 220;
            relY += 7;
            if (isHoverElementCheckbox("play_sound", relX, (relY += 23) + 69, mouseX, mouseY)) {
                selectedFilter.setPlaySound(!selectedFilter.isPlaySound());
            }
            if (!selectedFilter.isPlaySound()) {
                relY -= 10;
            }
            if (isHoverElementCheckbox("highlight", relX, relY + 92, mouseX, mouseY)) {
                selectedFilter.setHighlightMessage(!selectedFilter.isHighlightMessage());
            }
            if (selectedFilter.isHighlightMessage() && selectedFilter.getHighlightColor() != null) {
                dragElementSlider(relX, relY + 92 + 15, 0, mouseX, mouseY);
                dragElementSlider(relX, relY + 92 + 15 + 9, 1, mouseX, mouseY);
                dragElementSlider(relX, relY + 92 + 15 + 18, 2, mouseX, mouseY);
            } else {
                relY -= 26;
            }
            if (isHoverElementCheckbox("hide", relX, relY + 115 + 15, mouseX, mouseY)) {
                selectedFilter.setHideMessage(!selectedFilter.isHideMessage());
            }
            if (isHoverElementCheckbox("second_chat", relX, relY + 115 + 15 + 12, mouseX, mouseY)) {
                selectedFilter.setDisplayInSecondChat(!selectedFilter.isDisplayInSecondChat());
            }
            if (isHoverElementCheckbox("tooltip", relX, relY + 115 + 15 + 24, mouseX, mouseY)) {
                selectedFilter.setFilterTooltips(!selectedFilter.isFilterTooltips());
            }
            boolean hoverCancel = mouseX > width - 268 && mouseX < width - 213 && mouseY > height - 30 && mouseY < height - 18;
            boolean hoverSave = mouseX > width - 210 && mouseX < width - 154 && mouseY > height - 30 && mouseY < height - 18;
            if (hoverCancel) {
                selectedFilter = null;
            }
            if (hoverSave && selectedFilter != null) {
                if (selectedFilter.getFilterName().replaceAll(" ", "").isEmpty()) {
                    markFilterNameRed = true;
                }
                markContainsRed = selectedFilter.getWordsContains().length == 0;
                markSoundNameRed = selectedFilter.isPlaySound() && !soundNames.contains(textFieldFilterSoundfile.getText().toLowerCase());
                markWebhookRed = !(textFieldFilterWebhook.getText().isEmpty() || (HOOK_URL_PATTERN.matcher(textFieldFilterWebhook.getText()).matches() && IOUtil.request(textFieldFilterWebhook.getText()).getResponseCode() == 200));
                if (!(markFilterNameRed || markSoundNameRed || markContainsRed || markWebhookRed)) {
                    List<Filter> filters = LabyMod.getInstance().getChatToolManager().getFilters();
                    Iterator<Filter> it = filters.iterator();
                    while (it.hasNext()) {
                        Filter next = it.next();
                        if (editStartName == null) {
                            if (!next.getFilterName().equalsIgnoreCase(selectedFilter.getFilterName()))
                                continue;
                            it.remove();
                            continue;
                        }
                        if (!next.getFilterName().equalsIgnoreCase(editStartName))
                            continue;
                        it.remove();
                    }
                    if (!LabyMod.getInstance().getChatToolManager().getFilters().contains(selectedFilter)) {
                        LabyMod.getInstance().getChatToolManager().getFilters().add(selectedFilter);
                    }
                    FilterWebhooks.webhooks.put(selectedFilter.getFilterName(), textFieldFilterWebhook.getText().replaceAll(HOOK_URL_PATTERN.pattern(), "https://discord.com/api/webhooks/$1"));
                    FilterWebhooks.saveWebhooks();
                    LabyMod.getInstance().getChatToolManager().saveTools();
                    FilterChatManager.getFilterResults().clear();
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().refreshChat();
                    selectedFilter = null;
                    initGui();
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (selectedFilter != null && (textFieldFilterName.isFocused() || textFieldFilterContains.isFocused() || textFieldFilterContainsNot.isFocused() || textFieldFilterRoom.isFocused() || textFieldFilterWebhook.isFocused() || textFieldFilterSoundfile.isFocused())) {
            if (keyCode == 15) {
                if (textFieldFilterName.isFocused()) {
                    textFieldFilterName.setFocused(false);
                    textFieldFilterContains.setFocused(true);
                    return;
                }
                if (textFieldFilterContains.isFocused()) {
                    textFieldFilterContains.setFocused(false);
                    textFieldFilterContainsNot.setFocused(true);
                    return;
                }
                if (textFieldFilterContainsNot.isFocused()) {
                    textFieldFilterContainsNot.setFocused(false);
                    textFieldFilterRoom.setFocused(true);
                    return;
                }
                if (textFieldFilterRoom.isFocused()) {
                    textFieldFilterRoom.setFocused(false);
                    textFieldFilterWebhook.setFocused(true);
                    return;
                }
                if (textFieldFilterWebhook.isFocused()) {
                    textFieldFilterWebhook.setFocused(false);
                    if (selectedFilter.isPlaySound()) {
                        textFieldFilterSoundfile.setFocused(true);
                    } else {
                        textFieldFilterName.setFocused(true);
                    }
                }
                if (textFieldFilterSoundfile.isFocused()) {
                    textFieldFilterSoundfile.setFocused(false);
                    textFieldFilterName.setFocused(true);
                    return;
                }
            }
            if (textFieldFilterName.textboxKeyTyped(typedChar, keyCode)) {
                String newText = textFieldFilterName.getText().replaceAll(" ", "");
                boolean equals = false;
                for (Filter filter : LabyMod.getInstance().getChatToolManager().getFilters()) {
                    if (!filter.getFilterName().equalsIgnoreCase(newText) || filter.getFilterName().equalsIgnoreCase(editStartName))
                        continue;
                    equals = true;
                    break;
                }
                if (equals) {
                    markFilterNameRed = true;
                    return;
                }
                selectedFilter.setFilterName(newText);
                markFilterNameRed = false;
            }
            if (textFieldFilterContains.textboxKeyTyped(typedChar, keyCode)) {
                selectedFilter.setWordsContains(splitWords(textFieldFilterContains.getText()));
                markContainsRed = false;
            }
            if (textFieldFilterContainsNot.textboxKeyTyped(typedChar, keyCode)) {
                selectedFilter.setWordsContainsNot(splitWords(textFieldFilterContainsNot.getText()));
            }
            if (textFieldFilterRoom.textboxKeyTyped(typedChar, keyCode)) {
                selectedFilter.setRoom(textFieldFilterRoom.getText());
            }
            if (textFieldFilterWebhook.textboxKeyTyped(typedChar, keyCode)) {
                markWebhookRed = !(textFieldFilterWebhook.getText().isEmpty() || HOOK_URL_PATTERN.matcher(textFieldFilterWebhook.getText()).matches());
            }
            if (textFieldFilterSoundfile.textboxKeyTyped(typedChar, keyCode)) {
                selectedFilter.setSoundPath(textFieldFilterSoundfile.getText().toLowerCase());
                markSoundNameRed = false;
            }
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.DRAGGING);
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (selectedFilter != null) {
            int relX = width - 270;
            int relY = height - 200;
            if (!selectedFilter.isPlaySound()) {
                relY -= 10;
            }
            if (selectedFilter.isHighlightMessage() && selectedFilter.getHighlightColor() != null) {
                if (sliderDrag == 0) {
                    dragElementSlider(relX, relY + 92 + 15, 0, mouseX, mouseY);
                }
                if (sliderDrag == 1) {
                    dragElementSlider(relX, relY + 92 + 15 + 9, 1, mouseX, mouseY);
                }
                if (sliderDrag == 2) {
                    dragElementSlider(relX, relY + 92 + 15 + 18, 2, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        sliderDrag = -1;
        scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.RELEASED);
        textFieldFilterName.mouseClicked(mouseX, mouseY, mouseButton);
        textFieldFilterContains.mouseClicked(mouseX, mouseY, mouseButton);
        textFieldFilterContainsNot.mouseClicked(mouseX, mouseY, mouseButton);
        textFieldFilterRoom.mouseClicked(mouseX, mouseY, mouseButton);
        textFieldFilterWebhook.mouseClicked(mouseX, mouseY, mouseButton);
        textFieldFilterSoundfile.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        textFieldFilterName.updateCursorCounter();
        textFieldFilterContains.updateCursorCounter();
        textFieldFilterContainsNot.updateCursorCounter();
        textFieldFilterRoom.updateCursorCounter();
        textFieldFilterWebhook.updateCursorCounter();
        textFieldFilterSoundfile.updateCursorCounter();
    }

    private void drawElementTextField(String prefix, GuiTextField textField, int x, int y) {
        if (!prefix.isEmpty() && !prefix.equals("DC Webhook:")) {
            prefix = LanguageManager.translate("chat_filter_" + prefix) + ":";
        }
        drawString(LabyModCore.getMinecraft().getFontRenderer(), prefix, x + 2, y + 2, Integer.MAX_VALUE);
        CustomGuiChatFilter.drawRect(x + 2, y + 12, x + 2 + 114, y + 12 + 10,
                markContainsRed && textField != null && textField.equals(textFieldFilterContains)
                        || markFilterNameRed && textField != null && textField.equals(textFieldFilterName)
                        || markSoundNameRed && textField != null && textField.equals(textFieldFilterSoundfile)
                        || markWebhookRed && textField != null && textField.equals(textFieldFilterWebhook) ? ModColor.toRGB(200, 100, 100, 200) : Integer.MAX_VALUE);
        if (textField == null) {
            return;
        }
        LabyModCore.getMinecraft().setTextFieldXPosition(textField, x + 2);
        LabyModCore.getMinecraft().setTextFieldYPosition(textField, y + 13);
        textField.setEnableBackgroundDrawing(false);
        textField.setMaxStringLength(999);
    }

    private void drawElementCheckBox(String text, boolean check, int x, int y, int mouseX, int mouseY) {
        boolean hover = isHoverElementCheckbox(text, x, y, mouseX, mouseY);
        text = LanguageManager.translate("chat_filter_" + text);
        drawString(LabyModCore.getMinecraft().getFontRenderer(), text, x + 2, y + 2, Integer.MAX_VALUE);
        CustomGuiChatFilter.drawRect((x += LabyModCore.getMinecraft().getFontRenderer().getStringWidth(text) + 2) + 3, y + 1, x + 12, y + 10, hover ? 2147483547 : Integer.MAX_VALUE);
        if (!check) {
            return;
        }
        drawCenteredString(LabyModCore.getMinecraft().getFontRenderer(), ModColor.cl("a") + "✔", x + 8, y + 1, Integer.MAX_VALUE);
    }

    private void drawElementSlider(int value, int x, int y, int id) {
        CustomGuiChatFilter.drawRect(x + 2, y, x + 2 + 94, y + 1, Integer.MAX_VALUE);
        double percent = (double) value / 255.0 * 94.0;
        int pos = (int) percent;
        CustomGuiChatFilter.drawRect(x + 2 + pos, y - 3, x + 2 + pos + 2, y + 5, ModColor.toRGB(id == 0 ? value : 0, id == 1 ? value : 0, id == 2 ? value : 0, 255));
    }

    private boolean isHoverElementCheckbox(String text, int x, int y, int mouseX, int mouseY) {
        text = LanguageManager.translate("chat_filter_" + text);
        return mouseX > (x += LabyModCore.getMinecraft().getFontRenderer().getStringWidth(text) + 2) + 3 && mouseX < x + 12 && mouseY > y + 1 && mouseY < y + 10;
    }

    private void dragElementSlider(int x, int y, int id, int mouseX, int mouseY) {
        if (mouseX <= x || mouseX >= x + 94 || (mouseY <= y - 5 || mouseY >= y + 5) && sliderDrag != id) {
            return;
        }
        if (mouseX > x + 94) {
            mouseX = x + 94;
        }
        double pos = mouseX - x;
        double value = pos * 255.0 / 94.0;
        int colorValue = (int) value;
        Color highlightColor = selectedFilter.getHighlightColor();
        int r = highlightColor.getRed();
        int g = highlightColor.getGreen();
        int b = highlightColor.getBlue();
        switch (id) {
        case 0: {
            r = colorValue;
            break;
        }
        case 1: {
            g = colorValue;
            break;
        }
        case 2: {
            b = colorValue;
        }
        }
        selectedFilter.setHighlightColorR((short) r);
        selectedFilter.setHighlightColorG((short) g);
        selectedFilter.setHighlightColorB((short) b);
        sliderDrag = id;
    }

    private void loadFilter(Filter filter) {
        editStartName = null;
        selectedFilter = filter;
        markFilterNameRed = false;
        textFieldFilterName.setText(filter.getFilterName());
        textFieldFilterContains.setText(wordsToString(filter.getWordsContains()));
        textFieldFilterContainsNot.setText(wordsToString(filter.getWordsContainsNot()));
        textFieldFilterRoom.setText(filter.getRoom() == null || filter.getRoom().isEmpty() ? "Global" : filter.getRoom());
        textFieldFilterSoundfile.setText(filter.getSoundPath());
        textFieldFilterWebhook.setText(FilterWebhooks.webhooks.getOrDefault(filter.getFilterName(), ""));
    }

    private String[] splitWords(String text) {
        String[] stringArray;
        if (text.contains(",")) {
            stringArray = text.split(",");
        } else if (text.isEmpty()) {
            stringArray = new String[] {};
        } else {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = text;
        }
        return stringArray;
    }

    private String wordsToString(String[] words) {
        StringBuilder output = new StringBuilder();
        for (String word : words) {
            if (output.length() > 0) {
                output.append(",");
            }
            output.append(word);
        }
        return output.toString();
    }

    static {
        try {
            Field soundRegistryInSoundHandlerField = ReflectionHelper.findField(SoundHandler.class, LabyModCore.getMappingAdapter().getSoundRegistryInSoundHandlerMappings());
            Field soundRegistryInSoundRegistryField = ReflectionHelper.findField(SoundRegistry.class, LabyModCore.getMappingAdapter().getSoundRegistryInSoundRegistryMappings());
            SoundRegistry soundRegistry = (SoundRegistry) soundRegistryInSoundHandlerField.get(Minecraft.getMinecraft().getSoundHandler());
            Map<ResourceLocation, SoundEventAccessorComposite> sounds = (Map<ResourceLocation, SoundEventAccessorComposite>) soundRegistryInSoundRegistryField.get(soundRegistry);
            for (ResourceLocation resourceObject : sounds.keySet()) {
                soundNames.add(resourceObject.getResourcePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
