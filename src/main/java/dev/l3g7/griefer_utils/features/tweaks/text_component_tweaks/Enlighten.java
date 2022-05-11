package dev.l3g7.griefer_utils.features.tweaks.text_component_tweaks;

import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.RadioSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import static net.minecraft.util.EnumChatFormatting.*;


@Singleton
public class Enlighten extends TextComponentTweak {

    private final BooleanSetting enlightenLightGray = new BooleanSetting()
            .name("Hellgrau aufhellen")
            .config("tweaks.enlighten.light_gray")
            .icon(new ItemStack(Blocks.wool, 1, 8))
            .defaultValue(false)
            .callback(c -> updatePlayerInfoList());

    private final RadioSetting<GrayMode> enlightenGray = new RadioSetting<>(GrayMode.class)
            .name("Grau zu ...")
            .icon(new ItemStack(Blocks.wool, 1, 7))
            .config("tweaks.enlighten.gray")
            .defaultValue(GrayMode.GRAY)
            .stringProvider(GrayMode::getName)
            .callback(c -> updatePlayerInfoList());

    private final RadioSetting<BlackMode> enlightenBlack = new RadioSetting<>(BlackMode.class)
            .name("Schwarz zu ...")
            .icon(new ItemStack(Blocks.wool, 1, 15))
            .config("tweaks.enlighten.black")
            .defaultValue(BlackMode.BLACK)
            .stringProvider(BlackMode::getName)
            .callback(c -> updatePlayerInfoList());

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Aufhellen")
            .config("tweaks.enlighten.active")
            .icon("light_bulb")
            .defaultValue(false)
            .callback(c -> updatePlayerInfoList())
            .subSettingsWithHeader("Aufhellen", enlightenLightGray, enlightenGray, enlightenBlack,
                    new HeaderSetting("§r").scale(.4).entryHeight(10),
                    chat, tab, item);

    public Enlighten() {
        super("enlighten");
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @Override
    void modify(IChatComponent component) {
        // Enlighten IChatComponent
        if (!component.getUnformattedTextForChat().equals("\u2503 ")) {
            if (component.getChatStyle().getColor() == BLACK)
                component.setChatStyle(component.getChatStyle().setColor(enlightenBlack.get().getFormatting()));
            else if (component.getChatStyle().getColor() == DARK_GRAY)
                component.setChatStyle(component.getChatStyle().setColor(enlightenGray.get().getFormatting()));
            else if (component.getChatStyle().getColor() == GRAY)
                component.setChatStyle(component.getChatStyle().setColor(enlightenLightGray.get() ? WHITE : GRAY));
        }
        component.getSiblings().forEach(this::modify);
    }

    @Override
    String modify(String message) {
        // Enlighten String
        // Can't use replace, so I have to iterate through all
        char[] result = new char[message.length()];
        boolean isColor = false;
        int index = -1;
        for (char c : message.toCharArray()) {
            index++;
            result[index] = c;
            if (c == '§') {
                isColor = true;
                continue;
            }
            if (isColor) {
                if (c == '0') {
                    result[index] = enlightenBlack.get().getFormatting().toString().charAt(1);
                } else if (c == '8') {
                    result[index] = enlightenGray.get().getFormatting().toString().charAt(1);
                } else if (c == '7') {
                    result[index] = enlightenLightGray.get() ? 'f' : '7';
                }
                isColor = false;
            }
        }
        return new String(result).replaceAll("§[f70](§l)?\u2503", "§8$1\u2503"); // Don't enlighten delimiter
    }

    private enum GrayMode {

        WHITE("Weiß", EnumChatFormatting.WHITE), LIGHT("Hellgrau", EnumChatFormatting.GRAY), GRAY("Grau", EnumChatFormatting.DARK_GRAY);

        private final String name;
        private final EnumChatFormatting formatting;

        GrayMode(String name, EnumChatFormatting formatting) {
            this.name = name;
            this.formatting = formatting;
        }

        public String getName() {
            return name;
        }

        public EnumChatFormatting getFormatting() {
            return formatting;
        }
    }

    private enum BlackMode {

        WHITE("Weiß", EnumChatFormatting.WHITE), LIGHT("Hellgrau", EnumChatFormatting.GRAY), GRAY("Grau", EnumChatFormatting.DARK_GRAY), BLACK("Schwarz", EnumChatFormatting.BLACK);

        private final String name;
        private final EnumChatFormatting formatting;

        BlackMode(String name, EnumChatFormatting formatting) {
            this.name = name;
            this.formatting = formatting;
        }

        public String getName() {
            return name;
        }

        public EnumChatFormatting getFormatting() {
            return formatting;
        }
    }

}