package dev.l3g7.griefer_utils.features.modules;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerJoinEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.misc.Constants.DECIMAL_FORMAT_3;
import static dev.l3g7.griefer_utils.misc.Constants.ORB_SELL_PATTERN;

@Singleton
public class OrbBalance extends Module {

	private static final Pattern SKULL_PATTERN = Pattern.compile("^§7Du besitzt aktuell §e(?<orbs>[\\d.]+) Orbs§7\\.$");
	private static final Pattern BUY_PATTERN = Pattern.compile("^\\[GrieferGames] Du hast erfolgreich das Produkt .+ für (?<orbs>[\\d.]+) Orbs gekauft\\.$");

	private static long balance = -1;

	public OrbBalance() {
		super("Orbguthaben", "Zeigt dir an, wie viele Orbs du hast.", "orb_balance", new ControlElement.IconData(Material.EXP_BOTTLE));
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (!ServerCheck.isOnCitybuild() || !(mc.currentScreen instanceof GuiChest))
			return;

		IInventory inv = Reflection.get(mc.currentScreen, "lowerChestInventory", "field_147015_w", "w");

		int skullSlot;
		if (inv.getName().equals("§6Händler"))
			skullSlot = 10;
		else if (inv.getName().equals("§6Verkäufer"))
			skullSlot = 13;
		else
			return;

		ItemStack skull = inv.getStackInSlot(skullSlot);
		if (skull == null
				|| !(skull.getItem() instanceof ItemSkull)
				|| !skull.getDisplayName().equals("§6Deine Orbs")
				|| !ItemUtil.hasLore(skull))
			return;

		Matcher matcher = SKULL_PATTERN.matcher(ItemUtil.getLastLore(skull));
		if (matcher.matches()) {
			balance = Long.parseLong(matcher.group("orbs").replace(".", ""));
			saveBalance();
		}
	}

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		if (!ServerCheck.isOnCitybuild())
			return;

		String msg = event.getUnformatted();

		Matcher sellMatcher = ORB_SELL_PATTERN.matcher(msg);
		if (sellMatcher.matches()) {
			balance += Long.parseLong(sellMatcher.group("orbs").replace(".", ""));
			saveBalance();
			return;
		}

		Matcher buyMatcher = BUY_PATTERN.matcher(msg);
		if (buyMatcher.matches()) {
			balance -= Long.parseLong(buyMatcher.group("orbs").replace(".", ""));
			saveBalance();
		}
	}

	@Override
	public String[] getValues() {
		return balance == -1 ? getDefaultValues() : new String[]{DECIMAL_FORMAT_3.format(balance)};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"Bitte öffne den Orb-Händler / Orb-Verkäufer."};
	}

	@EventListener
	public void loadBalance(ServerJoinEvent ignored) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		String path = "modules.orb_balance.balances." + PlayerUtil.getUUID();

		if (Config.has(path))
			balance = Config.get(path).getAsLong();
	}

	private void saveBalance() {
		// Save balance along with player uuid so no problems occur when using multiple accounts
		Config.set("modules.orb_balance.balances." + PlayerUtil.getUUID(), balance);
		Config.save();
	}

	public static long getBalance() {
		return balance;
	}

}