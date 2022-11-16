package dev.l3g7.griefer_utils.features.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.event_bus.EventPriority;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.server.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerJoinEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;

@Singleton
public class CooldownNotifications extends Feature {

    private static final String TITLE = "§8§m------------§r§8[ §r§6Cooldowns §r§8]§r§8§m------------§r";
    private final Set<Cooldown> endDates = new HashSet<>();
    private boolean waitingForCooldownGUI = false;
	private boolean sendCooldowns = false;

    private final BooleanSetting enabled = new BooleanSetting()
            .name("Cooldown-Benachrichtigungen")
            .icon(Material.WATCH)
            .defaultValue(false)
            .config("features.cooldown_notifications.active")
            .callback(v -> {
                // If no data is found, open and close /cooldowns automatically
                if (v && endDates.isEmpty() && ServerCheck.isOnCitybuild() && !waitingForCooldownGUI) {
                    sendQueued("/cooldowns");
                    waitingForCooldownGUI = true;
                }
            });

    public CooldownNotifications() {
        super(Category.FEATURE);
    }

    @Override
    public SettingsElement getMainElement() {
        return enabled;
    }

    @EventListener
    public void onMessageReceive(MessageReceiveEvent event) {
        // Check if /grieferboost, /kopf or /premium was used (other cooldowns are currently not supported)
        if (event.getFormatted().matches("^§r§aDu hast §r§2.+-Booster §r§aerhalten\\. Danke für deine Unterstützung von GrieferGames!§r$"))
            endDates.add(new Cooldown("/grieferboost", System.currentTimeMillis() + 1000 * 3600 * 24 * 14));
        else if (event.getFormatted().matches("^§r§8\\[§r§6Kopf§r§8] §r§aDu hast einen §r§2.+§r§a-Kopf erhalten!§r$"))
            endDates.add(new Cooldown("/kopf", System.currentTimeMillis() + 1000 * 3600 * 24 * (PlayerUtil.getRank(player().getName()).equals("Titan") ? 14 : 7)));
        else if (event.getFormatted().matches("^§r§fDu hast §r.+§r §r§fden §r§6Premium Rang §r§faktiviert.§r$"))
            endDates.add(new Cooldown("/premium", System.currentTimeMillis() + 1000 * 3600 * 24 * 7));
    }

    @EventListener
    public void onCBJoin(CityBuildJoinEvent event) {
        if (!isActive() || !isOnGrieferGames())
            return;

        // If no data is found, open and close /cooldowns automatically
        if (endDates.isEmpty()) {
            sendQueued("/cooldowns");
            waitingForCooldownGUI = true;
        }
    }

    @EventListener(priority = EventPriority.LOWEST) // Make sure loadCooldowns is triggered before
    public void onServerJoin(ServerJoinEvent event) {
	    if (!isOnGrieferGames())
		    return;

	    sendCooldowns = true;
    }

	@EventListener
	public void onCityBuildJoins(CityBuildJoinEvent event) {
		if (!isActive() || !isOnGrieferGames() || !sendCooldowns)
			return;

		sendCooldowns = false;

		if (endDates.size() == 0) {
			display(TITLE);
			display("§c");
			display("§cEs liegen noch keine Daten vor. Bitte gehe auf einen Citybuild!");
			display("§c");
			display(TITLE);
			return;
		}

		endDates.forEach(Cooldown::checkEndTime);

		// Display cooldown information on server join
		display(TITLE);

		for (Cooldown key : endDates)
			if (key.endTime == 0)
				display("§8» §e%s§7:§r %s", key.name, "§aVerfügbar");
		for (Cooldown key : endDates)
			if (key.endTime > 0)
				display("§8» §e%s§7:§r %s", key.name, "§6Verfügbar am " + Constants.DATE_FORMAT.format(new Date(key.endTime)));
		for (Cooldown key : endDates)
			if (key.endTime < 0)
				display("§8» §e%s§7:§r %s", key.name, "§cNicht freigeschaltet");

		display(TITLE);
	}

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isActive())
            return;

        // Display in chat if in game, if not display as achievement
        Consumer<String> displayFunc = LabyMod.getInstance().isInGame()
                ? s -> display(Constants.ADDON_PREFIX + "§e%s ist nun §averfügbar§e!", s)
                : s -> displayAchievement(Constants.ADDON_NAME, "§e%s ist nun §averfügbar§e!", s);

        // Check if cooldown has become available
        for (Cooldown key : endDates) {
            if (key.checkEndTime()) {
                displayFunc.accept(key.name);
                saveCooldowns();
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event) {
        if (!isOnGrieferGames())
            return;

        // Check if cooldown gui is open
        if (mc().currentScreen instanceof GuiChest) {
            IInventory inventory = Reflection.get(mc().currentScreen, "lowerChestInventory", "field_147015_w", "w");
            if (inventory.getDisplayName().getFormattedText().equals("§6Cooldowns§r")) {
                if (inventory.getSizeInventory() != 45 || inventory.getStackInSlot(11) == null || inventory.getStackInSlot(11).getItem() != Items.gold_ingot)
                    return;

                // Iterate through slots
                boolean foundAny = false;
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    ItemStack s = inventory.getStackInSlot(i);
                    if(s == null || s.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane))
                        continue;

                    // Load cooldown time from item
                    try {
                        endDates.add(new Cooldown(s.getDisplayName().replaceAll("§.", "").replace("-Befehl", ""), getAvailability(s)));
                    } catch (ParseException e) {
                        // Ignore item
                        e.printStackTrace();
                        continue;
                    }
                    foundAny = true;
                }

                if (foundAny) {
                    saveCooldowns();
                    // Close cooldowns if waitingForCooldownGUI (was automatically opened)
                    if (waitingForCooldownGUI && isActive()) {
                        mc().displayGuiScreen(null);
                        waitingForCooldownGUI = false;
                    }
                }
            }
        }
    }

    private void saveCooldowns() {
        JsonObject o = new JsonObject();
        for (Cooldown key : endDates)
            o.addProperty(key.name, key.endTime);

        // Save end dates along with player uuid so no problems occur when using multiple accounts
        Config.set("features.cooldown_notifications.end_dates." + uuid(), o);
        Config.save();
    }

    @EventListener
    public void loadCooldowns(ServerJoinEvent ignored) {
        if (!isActive() || !isOnGrieferGames())
            return;

        String path = "features.cooldown_notifications.end_dates." + uuid();

        if (Config.has(path)) {
            endDates.clear();
            for (Map.Entry<String, JsonElement> e : Config.get(path).getAsJsonObject().entrySet())
                endDates.add(new Cooldown(e.getKey(), e.getValue().getAsLong()));
        }
    }

    /**
     * -2: Invalid item
     * -1: not available
     *  0: available
     * >0: unix time when available
     */
    private static long getAvailability(ItemStack i) throws ParseException {
        NBTTagList lore = i.serializeNBT().getCompoundTag("tag").getCompoundTag("display").getTagList("Lore", NBT.TAG_STRING);
        if (lore.tagCount() == 1) {
            if (lore.getStringTagAt(0).equals("§aVerfügbar"))
                return 0;

            return -1;
        } else if (lore.tagCount() == 2) {
            String dateStr = lore.getStringTagAt(1)
                    .replace("§7am §e§e", "")
                    .replace(" §7um§e ", " ")
                    .replace(" §7frei.", "");
            return Constants.DATE_FORMAT.parse(dateStr).getTime();
        }
        return -2;
    }

    private static class Cooldown {

        private final String name;
        private long endTime;

        private Cooldown(String name, long endTime) {
            this.name = name;
            this.endTime = endTime;
        }

        /**
         * @return true if end time was updated
         */
        public boolean checkEndTime() {
            if(endTime > 0 && endTime < System.currentTimeMillis()) {
                endTime = 0;
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (int) (31 * name.hashCode() + endTime);
        }
    }
}
