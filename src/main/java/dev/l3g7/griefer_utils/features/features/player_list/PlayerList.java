package dev.l3g7.griefer_utils.features.features.player_list;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.event_bus.EventPriority;
import dev.l3g7.griefer_utils.event.events.chat.MessageModifyEvent;
import dev.l3g7.griefer_utils.event.events.network.tablist.TabListNameUpdateEvent;
import dev.l3g7.griefer_utils.event.events.render.DisplayNameRenderEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.utils.ModColor;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PlayerList extends Feature {

    private static final Pattern PROFILE_TITLE_PATTERN = Pattern.compile(String.format("^§6Profil von §e%s§r$", Constants.FORMATTED_PLAYER_NAME_PATTERN));
    private static final ImmutableList<Pattern> ALL_PATTERNS = ImmutableList.of(Constants.GLOBAL_RECEIVE_PATTERN, Constants.PLOTCHAT_RECEIVE_PATTERN, Constants.MESSAGE_RECEIVE_PATTERN, Constants.MESSAGE_SEND_PATTERN);

    private final String paneName;
    private final ModColor titleColor;
    private final int paneMeta;
    private final String tag, icon;

    public PlayerList(String paneName, ModColor titleColor, int paneMeta, String tag, String icon) {
        super(Category.FEATURE);
        this.paneName = paneName;
        this.titleColor = titleColor;
        this.paneMeta = paneMeta;
        this.tag = tag;
        this.icon = icon;
    }

    abstract List<PlayerListProvider.PlayerListEntry> getEntries(String name, UUID uuid);

    List<PlayerListProvider.PlayerListEntry> getEntries(GameProfile profile) {
        return getEntries(profile.getName(), profile.getId());
    }

    abstract MarkAction getTabAction();

    abstract MarkAction getChatAction();

    abstract MarkAction getDisplayNameAction();

    abstract boolean showInProfile();

    private String actionToString(MarkAction action) {
        switch (action) {
            case DISABLED:
                return "";
            case ICON:
                return icon;
            case TAG:
                return tag;
        }
        throw new RuntimeException("wtf");
    }

    @EventListener(priority = EventPriority.LOW)
    public void onDisplayNameRender(DisplayNameRenderEvent event) {
        if (!isActive() || !isOnGrieferGames())
            return;

        // Update displayName
        if (!getEntries(event.getPlayer().getName(), event.getPlayer().getUniqueID()).isEmpty()) {
            String text = event.getDisplayName().getUnformattedTextForChat();

            // Check if tag already exists
            if (text.equals(this.tag) || text.equals(icon)) {
                // Update tag
                Reflection.set(event.getDisplayName(), actionToString(getDisplayNameAction()), "b", "field_150267_b", "text");
            } else {
                // Append tag
                event.setDisplayName(new ChatComponentText(actionToString(getDisplayNameAction())).appendSibling(event.getDisplayName()));
            }
        }
    }

    @EventListener
    public void onTabNameUpdate(TabListNameUpdateEvent event) {
        if (getTabAction() == MarkAction.DISABLED || !isActive() || !isOnGrieferGames())
            return;

        if (!getEntries(event.getProfile()).isEmpty()) {// Append tag
            event.setComponent(new ChatComponentText(actionToString(getTabAction())).appendSibling(event.getComponent()));
        }
    }

    @EventListener
    public void onMessageModify(MessageModifyEvent event) {
        if (getChatAction() == MarkAction.DISABLED || !isActive() || !isOnGrieferGames())
            return;

        // Check if message is GLOBAL_RECEIVE, PLOTCHAT_RECEIVE, MESSAGE_RECEIVE, MESSAGE_SEND
        ALL_PATTERNS.stream().map(p -> p.matcher(event.getOriginal().getFormattedText())).filter(Matcher::matches).findFirst().ifPresent(matcher -> {

            String name = matcher.group("name").replaceAll("§.", "");
            List<PlayerListProvider.PlayerListEntry> entries = getEntries(PlayerUtil.unnick(name), PlayerUtil.getUUID(name));

            if (!entries.isEmpty()) {
                ChatStyle style = new ChatStyle();

                // Add /profil command on click
                style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profil " + entries.get(0).getName()));

                // Add description
                style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(String.join("\n", createDescription(entries)))));

                // Update message
                event.setMessage(new ChatComponentText(actionToString(getChatAction()).trim()).setChatStyle(style).appendText(" ").appendSibling(event.getMessage()));
            }
        });
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event) {
        if (!showInProfile() || !isActive() || !isOnGrieferGames())
            return;

        // Check if profile is open
        if (mc().currentScreen instanceof GuiChest) {
            IInventory inventory = Reflection.get(mc().currentScreen, "lowerChestInventory", "field_147015_w", "w");

            // Check if title matches
            Matcher matcher = PROFILE_TITLE_PATTERN.matcher(inventory.getDisplayName().getFormattedText());
            if (!matcher.matches())
                return;

            List<PlayerListProvider.PlayerListEntry> entries = getEntries(matcher.group("name"), PlayerUtil.getUUID(matcher.group("name")));
            if (!entries.isEmpty()) {
                // Construct item
                ItemStack indicatorPane = new ItemBuilder(new ItemStack(Blocks.stained_glass_pane, 1, paneMeta))
                        .name("§" + titleColor.getColorChar() + "§l" + paneName)
                        .lore(createDescription(entries))
                        .build();

                // Replace every glass pane with indicatorPane
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    ItemStack slot = inventory.getStackInSlot(i);
                    if (slot != null && slot.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane))
                        inventory.setInventorySlotContents(i, indicatorPane);
                }
            }
        }
    }

    private List<String> createDescription(List<PlayerListProvider.PlayerListEntry> entries) {
        // Create description based on providers

        List<String> description = new ArrayList<>();
        description.add("§" + titleColor.getColorChar() + "§l" + paneName);
        if (entries.size() == 1) {
            // Single provider
            description.add(String.format("§%cLaut %s", titleColor.getColorChar(), entries.get(0).getProvider().getName()));
        } else {
            // Multiple providers, create list
            description.add(String.format("§%cLaut", titleColor.getColorChar()));
            entries.stream()
                    .map(e -> String.format("§%c- %s", titleColor.getColorChar(), e.getProvider().getName()))
                    .forEach(description::add);
        }
        return description;
    }

    enum MarkAction {

        DISABLED("Aus"), ICON("Als Icon"), TAG("Als Text");

        private final String name;

        MarkAction(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }
}