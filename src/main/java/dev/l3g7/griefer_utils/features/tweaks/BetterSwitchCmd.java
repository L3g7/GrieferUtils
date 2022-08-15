package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.server.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Singleton
public class BetterSwitchCmd extends Feature {

	private static final String ALL = getRegex("\\S+");
	private static final String NATURE = getRegex("n|nature");
	private static final String EXTREME = getRegex("x|extreme");
	private static final String EVIL = getRegex("e|evil");
	private static final String NUMBER = getRegex("\\d+");

	private static String getRegex(String cityBuild) {
		return "^/(?:cb ?|switch )(" + cityBuild + ")(?: (.*))?$";
	}

	private Integer slot = null;
	private String command = "";

	private final BooleanSetting enabled = new BooleanSetting()
			.name("BetterSwitchCmd")
			.config("tweaks.better_switch_cmd.active")
			.description("Ermöglicht das direkte Joinen auf einen Citybuild. Siehe '/cb'.")
			.icon(Material.COMPASS)
			.defaultValue(true);

	public BetterSwitchCmd() {
		super(Category.TWEAK);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@EventListener
	public void onMessageSend(MessageSendEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		String msg = event.getMsg().toLowerCase();

		if (msg.equals("/cb")) {
			display(Constants.ADDON_PREFIX + "Syntax: '/switch <CB>', '/cb <CB>' oder '/cb<CB>'.");
			display(Constants.ADDON_PREFIX + "§7§nShortcuts:");
			display(Constants.ADDON_PREFIX + "§7Nature: 'n'");
			display(Constants.ADDON_PREFIX + "§7Extreme: 'x'");
			display(Constants.ADDON_PREFIX + "§7Evil: 'e'");
			event.setCanceled(true);
			return;
		}

		if (msg.matches(NATURE))
			join(23);
		else if (msg.matches(EXTREME))
			join(24);
		else if (msg.matches(EVIL))
			join(25);
		else if (msg.matches(NUMBER))
			join(Integer.parseInt(msg.replaceAll(NUMBER, "$1")));
		else
			return;

		command = msg.replaceAll(ALL, "$2");

		event.setCanceled(true);
	}

	@SubscribeEvent
	public void onTick(TickEvent ignored) {
		if (slot == null || !(mc().currentScreen instanceof GuiChest))
			return;

		IInventory inv = Reflection.get(mc().currentScreen, "lowerChestInventory", "field_147015_w", "w");
		if (!inv.getDisplayName().getUnformattedText().equals("§6§lServerwechsel")
				|| inv.getStackInSlot(10) == null
				|| !inv.getStackInSlot(10).getItem().equals(Items.iron_axe))
			return;

		mc().playerController.windowClick(player().openContainer.windowId, slot, 0, 0, player());
		slot = null;
	}

	public void join(int cb) {
		if (cb < 1 || cb > 25) {
			display(Constants.ADDON_PREFIX + "Citybuild %d existiert nicht!", cb);
			return;
		}

		cb--;

		int row = cb / 7;
		int column = cb % 7;
		slot = (row * 9 + column) + 10;

		send("/switch");
	}

	@EventListener
	public void onCityBuild(CityBuildJoinEvent event) {
		if (command.isEmpty())
			return;

		send(command);
		command = "";
	}

}
